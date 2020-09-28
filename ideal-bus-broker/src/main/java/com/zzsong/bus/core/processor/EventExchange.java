package com.zzsong.bus.core.processor;

import com.zzsong.bus.abs.core.RouteTransfer;
import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.common.message.EventHeaders;
import com.zzsong.bus.common.message.PublishResult;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.common.util.ConditionMatcher;
import com.zzsong.bus.core.admin.service.EventInstanceService;
import com.zzsong.bus.core.admin.service.RouteInstanceService;
import com.zzsong.bus.core.config.BusProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Component
public class EventExchange {
  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final BusProperties properties;
  @Nonnull
  private final RouteTransfer routeTransfer;
  @Nonnull
  private final RouteInstanceService routeInstanceService;
  @Nonnull
  private final EventInstanceService eventInstanceService;

  public EventExchange(@Nonnull LocalCache localCache,
                       @Nonnull BusProperties properties,
                       @Nonnull RouteTransfer routeTransfer,
                       @Nonnull RouteInstanceService routeInstanceService,
                       @Nonnull EventInstanceService eventInstanceService) {
    this.localCache = localCache;
    this.properties = properties;
    this.routeTransfer = routeTransfer;
    this.routeInstanceService = routeInstanceService;
    this.eventInstanceService = eventInstanceService;
  }

  @Nonnull
  public Mono<PublishResult> publish(@Nonnull EventInstance event) {
    PublishResult.PublishResultBuilder builder = PublishResult.builder()
        .eventId(event.getEventId())
        .bizId(event.getBizId())
        .topic(event.getTopic())
        .success(true);
    // 路由, 获取满足订阅条件的订阅者列表
    Mono<List<RouteInstance>> route = route(event)
        .flatMap(list -> {
          if (list.isEmpty()) {
            event.setStatus(EventInstance.NOT_ROUTED);
          } else {
            event.setStatus(EventInstance.ROUTED);
          }
          return eventInstanceService.save(event).map(ins -> list);
        });
    return route.flatMap(instanceList -> {
      if (instanceList.isEmpty()) {
        PublishResult publishResult = builder.message("该事件没有订阅者").build();
        return Mono.just(publishResult);
      }
      List<RouteInstance> collect = instanceList.stream()
          .filter(instance -> instance.getNextPushTime() < 1L)
          .collect(Collectors.toList());
      return routeTransfer.submit(collect, false).map(b -> builder.message("success").build());
    });
  }

  @Nonnull
  private Mono<List<RouteInstance>> route(@Nonnull EventInstance event) {
    // 保存事件实例
    String topic = event.getTopic();
    List<SubscriptionDetails> subscription = localCache.getTopicSubscription(topic);
    if (subscription.isEmpty()) {
      return Mono.just(Collections.emptyList());
    }
    EventHeaders headers = event.getHeaders();
    List<RouteInstance> routeInstanceList = new ArrayList<>();
    for (SubscriptionDetails details : subscription) {
      List<Set<String>> group = details.getConditionGroup();
      if (!ConditionMatcher.match(group, headers)) {
        continue;
      }
      RouteInstance instance = createRouteInstance(event, details);
      routeInstanceList.add(instance);
    }
    if (routeInstanceList.isEmpty()) {
      return Mono.just(Collections.emptyList());
    }
    return routeInstanceService.saveAll(routeInstanceList);
  }

  @Nonnull
  private RouteInstance createRouteInstance(@Nonnull EventInstance event,
                                            @Nonnull SubscriptionDetails details) {
    RouteInstance instance = new RouteInstance();
    instance.setNodeId(properties.getNodeId());
    instance.setEventId(event.getEventId());
    instance.setKey(event.getKey());
    instance.setSubscriptionId(details.getSubscriptionId());
    instance.setApplicationId(details.getApplicationId());
    instance.setTopic(event.getTopic());
    int delaySeconds = event.getDelaySeconds();
    if (delaySeconds > 0) {
      long nextPushTime = System.currentTimeMillis() + delaySeconds * 1000L;
      instance.setNextPushTime(nextPushTime);
    }
    instance.setStatus(RouteInstance.STATUS_WAITING);
    instance.setMessage("waiting");
    return instance;
  }
}
