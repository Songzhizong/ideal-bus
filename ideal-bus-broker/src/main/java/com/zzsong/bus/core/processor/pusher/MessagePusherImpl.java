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
import com.zzsong.bus.core.admin.service.RouteInstanceService;
import com.zzsong.bus.core.processor.LocalCache;
import com.zzsong.common.loadbalancer.LbFactory;
import com.zzsong.common.loadbalancer.LbStrategyEnum;
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
  private static final long retryInterval = 10 * 1000L;
  @Autowired
  private RouteTransfer routeTransfer;
  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final RouteInstanceService routeInstanceService;
  @Nonnull
  private final LbFactory<DelivererChannel> lbFactory;
  @Nonnull
  private final ExternalDelivererChannel externalDelivererChannel;

  public MessagePusherImpl(@Nonnull LocalCache localCache,
                           @Nonnull RouteInstanceService routeInstanceService,
                           @Nonnull LbFactory<DelivererChannel> lbFactory,
                           @Nonnull ExternalDelivererChannel externalDelivererChannel) {
    this.localCache = localCache;
    this.routeInstanceService = routeInstanceService;
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
      final Map<String, Boolean> ackMap = deliveredResult.getAckMap();
      List<String> unAckList = new ArrayList<>();
      if (ackMap != null) {
        ackMap.forEach((listener, ack) -> {
          if (!ack) {
            unAckList.add(listener);
          }
        });
      }
      int currentRetryCount = routeInstance.getRetryCount() + 1;
      routeInstance.setRetryCount(currentRetryCount);
      routeInstance.setSuccess(RouteInstance.SUCCESS);
      routeInstance.setNextPushTime(-1L);
      if (!unAckList.isEmpty()) {
        routeInstance.setSuccess(RouteInstance.FAILURE);
        // 还有没ack的, 尝试重试
        routeInstance.setUnAckListeners(unAckList);
        int maxRetryCount = subscription.getRetryCount();
        if (currentRetryCount < maxRetryCount) {
          routeInstance.setStatus(RouteInstance.STATUS_WAITING);
          routeInstance.setNextPushTime(System.currentTimeMillis() + retryInterval);
        } else {
          routeInstance.setStatus(RouteInstance.STATUS_DISCARD);
        }
      } else {
        localCache.removeEventCache(routeInstance.getEventId());
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
            return externalDelivererChannel.deliver(deliveredEvent);
          } else {
            boolean broadcast = subscription.isBroadcast();
            if (broadcast) {
              List<DelivererChannel> channels = lbFactory
                  .getReachableServers(applicationId + "");
              if (channels.isEmpty()) {
                log.warn("应用: {} 可用通道列表为空", applicationId);
                return Mono.error(new VisibleException("可用channel列表为空"));
              }
              return Flux.fromIterable(channels)
                  .flatMap(channel -> channel.deliver(deliveredEvent))
                  .collectList()
                  .map(list -> {
                    DeliveredResult deliveredResult = new DeliveredResult();
                    deliveredEvent.setEventId(deliveredEvent.getEventId());
                    return deliveredResult;
                  });
            } else {
              String key = routeInstance.getKey();
              String topic = routeInstance.getTopic();
              DelivererChannel channel;
              if (StringUtils.isNotBlank(key)) {
                channel = lbFactory.chooseServer(applicationId + "",
                    key, LbStrategyEnum.CONSISTENT_HASH);
              } else {
                channel = lbFactory.chooseServer(applicationId + "", topic, LbStrategyEnum.ROUND_ROBIN);
              }
              if (channel == null) {
//                return Mono.error(new VisibleException("选取DelivererChannel为空"));
                return routeTransfer.giveBack(routeInstance).then(Mono.empty());
              }
              return channel.deliver(deliveredEvent);
            }
          }
        });
  }


  @Nonnull
  private Mono<DeliveredEvent> loadDeliveredEvent(@Nonnull RouteInstance routeInstance) {
    return localCache.loadEventInstance(routeInstance.getEventId())
        .map(opt -> {
          if (!opt.isPresent()) {
            throw new VisibleException("event实例不存在");
          }
          EventInstance instance = opt.get();
          DeliveredEvent deliveredEvent = new DeliveredEvent();
          deliveredEvent.setEventId(instance.getEventId());
          deliveredEvent.setBizId(instance.getBizId());
          deliveredEvent.setTopic(instance.getTopic());
          deliveredEvent.setHeaders(instance.getHeaders());
          deliveredEvent.setPayload(instance.getPayload());
          deliveredEvent.setTimestamp(instance.getTimestamp());
          deliveredEvent.setListeners(routeInstance.getUnAckListeners());
          return deliveredEvent;
        });
  }
}
