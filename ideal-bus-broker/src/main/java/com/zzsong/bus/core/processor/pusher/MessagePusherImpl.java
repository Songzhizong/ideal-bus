package com.zzsong.bus.core.processor.pusher;

import com.zzsong.bus.abs.constants.ApplicationTypeEnum;
import com.zzsong.bus.abs.core.MessagePusher;
import com.zzsong.bus.abs.core.RouteTransfer;
import com.zzsong.bus.abs.domain.Application;
import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.abs.share.VisibleException;
import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import com.zzsong.bus.core.admin.service.EventInstanceService;
import com.zzsong.bus.core.admin.service.RouteInstanceService;
import com.zzsong.bus.core.processor.LocalCache;
import com.zzsong.bus.common.share.loadbalancer.LbFactory;
import com.zzsong.bus.common.share.loadbalancer.LbStrategyEnum;
import com.zzsong.bus.common.share.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 宋志宗 on 2020/9/19 9:44 下午
 */
@Slf4j
@Component
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class MessagePusherImpl implements MessagePusher {
  private static final long RETRY_INTERVAL = 10 * 1000L;
  @Autowired
  private RouteTransfer routeTransfer;
  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final RouteInstanceService routeInstanceService;
  @Nonnull
  private final EventInstanceService eventInstanceService;
  @Nonnull
  private final LbFactory<DelivererChannel> lbFactory;
  @Nonnull
  private final ExternalDelivererChannel externalDelivererChannel;

  public MessagePusherImpl(@Nonnull LocalCache localCache,
                           @Nonnull RouteInstanceService routeInstanceService,
                           @Nonnull EventInstanceService eventInstanceService,
                           @Nonnull LbFactory<DelivererChannel> lbFactory,
                           @Nonnull ExternalDelivererChannel externalDelivererChannel) {
    this.localCache = localCache;
    this.routeInstanceService = routeInstanceService;
    this.eventInstanceService = eventInstanceService;
    this.lbFactory = lbFactory;
    this.externalDelivererChannel = externalDelivererChannel;
  }

  @Override
  public Mono<RouteInstance> push(@Nonnull RouteInstance routeInstance) {
    SubscriptionDetails subscription = localCache
        .getSubscription(routeInstance.getSubscriptionId());
    if (subscription == null) {
      throw new VisibleException("订阅关系不存在");
    }
    final Mono<DeliveredResult> resultMono = deliverEvent(routeInstance);
    return resultMono.flatMap(deliveredResult -> {
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
        int maxRetryCount = subscription.getRetryCount();
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
      return routeInstanceService.save(routeInstance);
    });
  }

  @Nonnull
  private Mono<DeliveredResult> deliverEvent(@Nonnull RouteInstance routeInstance) {
    long applicationId = routeInstance.getApplicationId();
    Application application = localCache.getApplication(applicationId);
    Long subscriptionId = routeInstance.getSubscriptionId();
    SubscriptionDetails subscription = localCache.getSubscription(subscriptionId);
    if (application == null) {
      log.warn("应用: {} 不存在", applicationId);
      throw new VisibleException("应用不存在");
    }
    if (subscription == null) {
      log.warn("订阅关系: {} 不存在", subscriptionId);
      throw new VisibleException("订阅关系不存在");
    }
    return loadDeliveredEvent(routeInstance)
        .flatMap(deliveredEvent -> {
          ApplicationTypeEnum applicationType = application.getApplicationType();
          if (applicationType == ApplicationTypeEnum.EXTERNAL) {
            return Mono.just(deliveredEvent)
                .flatMap(d -> {
                  // 交付前将状态修改为RUNNING
                  Long instanceId = routeInstance.getInstanceId();
                  int status = RouteInstance.STATUS_RUNNING;
                  return routeInstanceService
                      .updateStatus(instanceId, status, "running").map(l -> d);
                })
                .flatMap(externalDelivererChannel::deliver);
          } else {
            boolean broadcast = subscription.isBroadcast();
            if (broadcast) {
              List<DelivererChannel> channels = lbFactory
                  .getReachableServers(applicationId + "");
              if (channels.isEmpty()) {
                log.warn("应用: {} 可用通道列表为空", applicationId);
                return Mono.error(new VisibleException("可用channel列表为空"));
              }
              return Mono.just(deliveredEvent)
                  .flatMap(d -> {
                    // 交付前将状态修改为RUNNING
                    Long instanceId = routeInstance.getInstanceId();
                    int status = RouteInstance.STATUS_RUNNING;
                    return routeInstanceService
                        .updateStatus(instanceId, status, "running").map(l -> d);
                  })
                  .flatMap(d -> Flux.fromIterable(channels)
                      .flatMap(channel -> channel.deliver(deliveredEvent))
                      .collectList()
                      .map(list -> {
                        DeliveredResult deliveredResult = new DeliveredResult();
                        deliveredResult.setEventId(deliveredEvent.getEventId());
                        return deliveredResult;
                      }));
            } else {
              String key = routeInstance.getKey();
              String topic = routeInstance.getTopic();
              DelivererChannel channel;
              if (StringUtils.isNotBlank(key)) {
                channel = lbFactory.chooseServer(applicationId + "",
                    key, LbStrategyEnum.CONSISTENT_HASH);
              } else {
                channel = lbFactory.chooseServer(applicationId + "",
                    topic, LbStrategyEnum.ROUND_ROBIN);
              }
              if (channel == null) {
//                return Mono.error(new VisibleException("选取DelivererChannel为空"));
                return routeTransfer.giveBack(routeInstance).then(Mono.empty());
              }
              return Mono.just(deliveredEvent)
                  .flatMap(d -> {
                    // 交付前将状态修改为RUNNING
                    long instanceId = routeInstance.getInstanceId();
                    int status = RouteInstance.STATUS_RUNNING;
                    return routeInstanceService
                        .updateStatus(instanceId, status, "running").map(l -> d);
                  }).flatMap(channel::deliver);
            }
          }
        });
  }


  @Nonnull
  private Mono<DeliveredEvent> loadDeliveredEvent(@Nonnull RouteInstance routeInstance) {
    return eventInstanceService.loadByEventId(routeInstance.getEventId())
        .map(opt -> {
          if (!opt.isPresent()) {
            throw new VisibleException("event实例不存在");
          }
          EventInstance instance = opt.get();
          DeliveredEvent deliveredEvent = new DeliveredEvent();
          deliveredEvent.setRouteInstanceId(routeInstance.getInstanceId());
          deliveredEvent.setEventId(instance.getEventId());
          deliveredEvent.setBizId(instance.getBizId());
          deliveredEvent.setTopic(instance.getTopic());
          deliveredEvent.setHeaders(instance.getHeaders());
          deliveredEvent.setPayload(instance.getPayload());
          deliveredEvent.setTimestamp(instance.getTimestamp());
          if (routeInstance.getRetryCount() == -1) {
            deliveredEvent.setListeners(routeInstance.getListeners());
          } else {
            deliveredEvent.setListeners(routeInstance.getUnAckListeners());
          }
          return deliveredEvent;
        });
  }
}
