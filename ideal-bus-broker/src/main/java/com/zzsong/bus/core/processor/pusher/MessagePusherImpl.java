package com.zzsong.bus.core.processor.pusher;

import com.zzsong.bus.abs.constants.ApplicationTypeEnum;
import com.zzsong.bus.abs.core.MessagePusher;
import com.zzsong.bus.abs.domain.Application;
import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.domain.RouteInfo;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.abs.share.VisibleException;
import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import com.zzsong.bus.core.admin.service.RouteInfoService;
import com.zzsong.bus.core.processor.LocalCache;
import com.zzsong.common.loadbalancer.LbFactory;
import com.zzsong.common.loadbalancer.LbStrategyEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 宋志宗 on 2020/9/19 9:44 下午
 */
@Component
public class MessagePusherImpl implements MessagePusher {
  private static final long retryInterval = 10 * 1000L;
  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final RouteInfoService routeInfoService;
  @Nonnull
  private final LbFactory<DelivereChannel> lbFactory;
  @Nonnull
  private final ExternalDelivereChannel externalDelivereChannel;

  public MessagePusherImpl(@Nonnull LocalCache localCache,
                           @Nonnull RouteInfoService routeInfoService,
                           @Nonnull LbFactory<DelivereChannel> lbFactory,
                           @Nonnull ExternalDelivereChannel externalDelivereChannel) {
    this.localCache = localCache;
    this.routeInfoService = routeInfoService;
    this.lbFactory = lbFactory;
    this.externalDelivereChannel = externalDelivereChannel;
  }

  @Override
  public Mono<RouteInfo> push(@Nonnull RouteInfo routeInfo) {
    SubscriptionDetails subscription = localCache.getSubscription(routeInfo.getSubscriptionId());
    if (subscription == null) {
      throw new VisibleException("订阅关系不存在");
    }
    final Mono<DeliveredResult> resultMono = deliverEvent(routeInfo);
    return resultMono.flatMap(deliveredResult -> {
      final Map<String, Boolean> ackMap = deliveredResult.getAckMap();
      List<String> unackList = new ArrayList<>();
      if (ackMap != null) {
        ackMap.forEach((listener, ack) -> {
          if (!ack) {
            unackList.add(listener);
          }
        });
      }
      int currentRetryCount = routeInfo.getRetryCount() + 1;
      routeInfo.setRetryCount(currentRetryCount);
      routeInfo.setWait(RouteInfo.UN_WAITING);
      if (unackList.isEmpty()) {
        // 全部ack, 标记为成功状态
        routeInfo.setSuccess(RouteInfo.STATUS_SUCCESS);
        routeInfo.setNextPushTime(-1L);
      } else {
        routeInfo.setSuccess(RouteInfo.STATUS_FAILURE);
        // 还有没ack的, 尝试重试
        routeInfo.setUnackListeners(unackList);
        int maxRetryCount = subscription.getRetryCount();
        if (currentRetryCount < maxRetryCount) {
          routeInfo.setNextPushTime(System.currentTimeMillis() + retryInterval);
          routeInfo.setWait(RouteInfo.WAITING);
        }
      }
      return routeInfoService.save(routeInfo);
    });
  }

  @Nonnull
  private Mono<DeliveredResult> deliverEvent(@Nonnull RouteInfo routeInfo) {
    long applicationId = routeInfo.getApplicationId();
    Application application = localCache.getApplication(applicationId);
    if (application == null) {
      throw new VisibleException("应用不存在");
    }
    return loadDeliveredEvent(routeInfo)
        .flatMap(deliveredEvent -> {
          ApplicationTypeEnum applicationType = application.getApplicationType();
          if (applicationType == ApplicationTypeEnum.EXTERNAL) {
            return externalDelivereChannel.deliver(deliveredEvent);
          } else {
            String key = routeInfo.getKey();
            String topic = routeInfo.getTopic();
            DelivereChannel channel;
            if (StringUtils.isNotBlank(key)) {
              channel = lbFactory.chooseServer(applicationId + "",
                  key, LbStrategyEnum.CONSISTENT_HASH);
            } else {
              channel = lbFactory.chooseServer(applicationId + "", topic);
            }
            if (channel == null) {
              return Mono.error(new VisibleException("选取DelivereChannel为空"));
            }
            return channel.deliver(deliveredEvent);
          }
        });
  }


  @Nonnull
  private Mono<DeliveredEvent> loadDeliveredEvent(@Nonnull RouteInfo routeInfo) {
    return localCache.loadEventInstance(routeInfo.getEventId())
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
          deliveredEvent.setListeners(routeInfo.getUnackListeners());
          return deliveredEvent;
        });
  }
}
