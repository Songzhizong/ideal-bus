package com.zzsong.bus.core.processor;

import com.zzsong.bus.abs.core.RouteTransfer;
import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.domain.RouteInfo;
import com.zzsong.bus.abs.storage.RouteInfoStorage;
import com.zzsong.bus.common.message.EventHeaders;
import com.zzsong.bus.common.message.PublishResult;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.abs.storage.EventInstanceStorage;
import com.zzsong.bus.common.util.ConditionMatcher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Component
public class PublishService {
  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final RouteTransfer routeTransfer;
  @Nonnull
  private final RouteInfoStorage routeInfoStorage;
  @Nonnull
  private final EventInstanceStorage eventInstanceStorage;

  public PublishService(@Nonnull LocalCache localCache,
                        @Nonnull RouteTransfer routeTransfer,
                        @Nonnull RouteInfoStorage routeInfoStorage,
                        @Nonnull EventInstanceStorage eventInstanceStorage) {
    this.localCache = localCache;
    this.routeTransfer = routeTransfer;
    this.routeInfoStorage = routeInfoStorage;
    this.eventInstanceStorage = eventInstanceStorage;
  }

  @Nonnull
  public Mono<PublishResult> publish(@Nonnull EventInstance event) {
    PublishResult.PublishResultBuilder builder = PublishResult.builder()
        .eventId(event.getEventId())
        .bizId(event.getBizId())
        .topic(event.getTopic())
        .success(true);
    // 路由, 获取满足订阅条件的订阅者列表
    Mono<List<RouteInfo>> route = route(event);
    return route.flatMap(routeInfos -> {
      if (routeInfos.isEmpty()) {
        PublishResult publishResult = builder.message("该事件没有订阅者").build();
        return Mono.just(publishResult);
      }
      List<RouteInfo> collect = routeInfos.stream()
          .filter(routeInfo -> routeInfo.getNextPushTime() > 0)
          .collect(Collectors.toList());
      return routeTransfer.submit(collect).map(b -> builder.build());
    });
  }

  private Mono<List<RouteInfo>> route(@Nonnull EventInstance event) {
    return eventInstanceStorage.save(event)
        .flatMap(ins -> {
          String topic = event.getTopic();
          List<SubscriptionDetails> subscription = localCache.getTopicSubscription(topic);
          if (subscription.isEmpty()) {
            return Mono.just(Collections.emptyList());
          }
          EventHeaders headers = event.getHeaders();

          List<RouteInfo> routeInfoList = new ArrayList<>();
          for (SubscriptionDetails details : subscription) {
            List<Set<String>> group = details.getConditionGroup();
            if (!ConditionMatcher.match(group, headers)) {
              continue;
            }
            RouteInfo instance = createRouteInfo(event, details);
            routeInfoList.add(instance);
          }
          if (routeInfoList.isEmpty()) {
            return Mono.just(Collections.emptyList());
          }
          return routeInfoStorage.saveAll(routeInfoList);
        });
  }

  @Nonnull
  private RouteInfo createRouteInfo(@Nonnull EventInstance event,
                                    @Nonnull SubscriptionDetails details) {
    RouteInfo instance = new RouteInfo();
    instance.setEventId(event.getEventId());
    instance.setKey(event.getKey());
    instance.setSubscriptionId(details.getSubscriptionId());
    instance.setApplicationId(details.getApplicationId());
    instance.setTopic(event.getTopic());
    Duration delay = event.getDelay();
    if (delay != null) {
      long nextPushTime = System.currentTimeMillis() + delay.toMillis();
      instance.setNextPushTime(nextPushTime);
      instance.setWait(RouteInfo.WAITING);
    }
    return instance;
  }
}
