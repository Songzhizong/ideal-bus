package com.zzsong.bus.broker.connect;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.broker.constants.DeliverResult;
import com.zzsong.bus.broker.admin.service.RouteInstanceService;
import com.zzsong.bus.broker.connect.exception.AppOfflineException;
import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import com.zzsong.bus.common.share.loadbalancer.LbFactory;
import com.zzsong.bus.common.share.loadbalancer.LbStrategyEnum;
import com.zzsong.bus.common.share.loadbalancer.SimpleLbFactory;
import com.zzsong.bus.common.share.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 宋志宗 on 2020/11/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionManagerImpl implements ConnectionManager {
  private static final long RETRY_INTERVAL = 10 * 1000L;
  @Nonnull
  private final LbFactory<DelivererChannel> lbFactory = new SimpleLbFactory<>();

  @Nonnull
  private final RouteInstanceService routeInstanceService;

  @Override
  public boolean isApplicationAvailable(@Nonnull String appName) {
    return lbFactory.getReachableServers(appName).size() > 0;
  }

  @Override
  public void registerChannel(@Nonnull String appName, @Nonnull DelivererChannel channel) {
    lbFactory.addServers(appName, List.of(channel));
  }

  @Override
  public void markChannelBusy(@Nonnull String appName, @Nonnull DelivererChannel channel) {
    lbFactory.markServerDown(appName, channel);
  }

  @Override
  public void markChannelDown(@Nonnull String appName, @Nonnull DelivererChannel channel) {
    lbFactory.markServerDown(appName, channel);
  }

  @Override
  public void markChannelReachable(@Nonnull String appName, @Nonnull DelivererChannel channel) {
    lbFactory.markServerReachable(appName, channel);
  }

  @Nonnull
  @Override
  public Mono<DeliverResult> deliver(@Nonnull RouteInstance routeInstance) {
    // 执行交付
    return doDeliver(routeInstance)
        .flatMap(deliveredResult -> {
          // 根据交付结果更新路由实例信息
          RouteInstance instance = handleRouteInstance(routeInstance, deliveredResult);
          return routeInstanceService.save(instance)
              // 返回成功状态
              .thenReturn(DeliverResult.SUCCESS);
        })
        // 异常处理
        .onErrorResume(this::handleException);
  }

  @Nonnull
  private Mono<DeliverResult> handleException(@Nonnull Throwable throwable) {
    if (throwable instanceof AppOfflineException) {
      return Mono.just(DeliverResult.APP_OFFLINE);
    }
    if (throwable instanceof ClosedChannelException) {
      return Mono.just(DeliverResult.CHANNEL_CLOSED);
    }
    log.error("交付异常: ", throwable);
    return Mono.just(DeliverResult.UNKNOWN_EXCEPTION);
  }

  @Nonnull
  private RouteInstance handleRouteInstance(@Nonnull RouteInstance routeInstance,
                                            @Nonnull DeliveredResult deliveredResult) {
    // 起始值为-1, 每次交付+1
    int currentRetryCount = routeInstance.getRetryCount() + 1;
    routeInstance.setRetryCount(currentRetryCount);
    routeInstance.setStatus(RouteInstance.STATUS_SUCCESS);
    routeInstance.setMessage("success");
    routeInstance.setNextPushTime(-1L);

    // 判断是否执行成功
    final Map<String, Boolean> ackMap = deliveredResult.getAckMap();
    if (!ackMap.isEmpty()) {
      List<String> unAckList = new ArrayList<>();
      ackMap.forEach((listener, ack) -> {
        if (!ack) {
          unAckList.add(listener);
        }
      });
      // 存在未ack的, 说明没有执行成功
      if (!unAckList.isEmpty()) {
        if (log.isDebugEnabled()) {
          log.debug("消费端未执行成功: {}", JsonUtils.toJsonString(unAckList));
        }
        routeInstance.setUnAckListeners(unAckList);
      }
    }
    if (!deliveredResult.isSuccess() || !routeInstance.getUnAckListeners().isEmpty()) {
      int maxRetryCount = routeInstance.getRetryLimit();
      if (currentRetryCount < maxRetryCount) {
        // 没有达到重试上限, 标记为等待状态并计算下次执行时间
        routeInstance.setStatus(RouteInstance.STATUS_WAITING);
        routeInstance.setMessage("waiting");
        routeInstance.setNextPushTime(System.currentTimeMillis() + RETRY_INTERVAL);
      } else {
        // 达到重试上限, 标记为失败状态
        routeInstance.setStatus(RouteInstance.STATUS_FAILURE);
        routeInstance.setMessage("Reach the retry limit");
      }
    }
    return routeInstance;
  }

  @Nonnull
  private Mono<DeliveredResult> doDeliver(@Nonnull RouteInstance routeInstance) {
    Long applicationId = routeInstance.getApplicationId();
    long instanceId = routeInstance.getInstanceId();
    DeliveredEvent deliveredEvent = createDeliveredEvent(routeInstance);
    boolean broadcast = routeInstance.isBroadcast();
    if (broadcast) {
      List<DelivererChannel> channels = lbFactory
          .getReachableServers(applicationId + "");
      if (channels.isEmpty()) {
        log.warn("应用: {} 可用通道列表为空", applicationId);
        return Mono.error(new AppOfflineException());
      }
      // 交付前将状态修改为RUNNING
      int status = RouteInstance.STATUS_RUNNING;
      return routeInstanceService.updateStatus(instanceId, status, "running")
          .flatMap(l ->
              Flux.fromIterable(channels)
                  .flatMap(channel -> channel.deliver(deliveredEvent))
                  .collectList()
                  .map(list -> {
                    DeliveredResult deliveredResult = new DeliveredResult();
                    deliveredResult.setEventId(deliveredEvent.getEventId());
                    return deliveredResult;
                  })
          );
    }

    String aggregate = routeInstance.getAggregate();
    String topic = routeInstance.getTopic();
    DelivererChannel channel;
    if (StringUtils.isNotBlank(aggregate)) {
      channel = lbFactory.chooseServer(applicationId + "",
          aggregate, LbStrategyEnum.CONSISTENT_HASH);
    } else {
      channel = lbFactory.chooseServer(applicationId + "",
          topic, LbStrategyEnum.ROUND_ROBIN);
    }
    if (channel == null) {
      log.warn("应用: {} 可用通道列表为空", applicationId);
      return Mono.error(new AppOfflineException());
    }
    // 交付前将状态修改为RUNNING
    int status = RouteInstance.STATUS_RUNNING;
    return routeInstanceService.updateStatus(instanceId, status, "running")
        .flatMap(l -> channel.deliver(deliveredEvent));
  }


  @Nonnull
  private DeliveredEvent createDeliveredEvent(@Nonnull RouteInstance instance) {
    DeliveredEvent deliveredEvent = new DeliveredEvent();
    deliveredEvent.setRouteInstanceId(instance.getInstanceId());
    deliveredEvent.setSubscriptionId(instance.getSubscriptionId());
    deliveredEvent.setEventId(instance.getEventId());
    deliveredEvent.setTransactionId(instance.getTransactionId());
    deliveredEvent.setTopic(instance.getTopic());
    deliveredEvent.setHeaders(instance.getHeaders());
    deliveredEvent.setPayload(instance.getPayload());
    deliveredEvent.setTimestamp(instance.getTimestamp());
    if (instance.getRetryCount() == -1) {
      deliveredEvent.setListeners(instance.getListeners());
    } else {
      deliveredEvent.setListeners(instance.getUnAckListeners());
    }
    return deliveredEvent;
  }
}
