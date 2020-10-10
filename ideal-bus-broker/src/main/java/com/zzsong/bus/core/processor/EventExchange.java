package com.zzsong.bus.core.processor;

import com.zzsong.bus.abs.constants.DBDefaults;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Slf4j
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
        .message("success")
        .success(true);
    // 路由, 获取满足订阅条件的订阅者列表
    Mono<List<RouteInstance>> route = route(event)
        .flatMap(list -> {
          if (list.isEmpty()) {
            event.setStatus(EventInstance.NOT_ROUTED);
          } else {
            event.setStatus(EventInstance.ROUTED);
          }
          return eventInstanceService.save(event).thenReturn(list);
        });
    return route.flatMap(instanceList -> {
      if (instanceList.isEmpty()) {
        PublishResult publishResult = builder.message("该事件没有订阅者").build();
        return Mono.just(publishResult);
      }
      List<RouteInstance> collect = instanceList.stream()
          .filter(instance -> instance.getNextPushTime() < 1L)
          .collect(Collectors.toList());
      return routeTransfer.submit(collect, false)
          .thenReturn(builder.build());
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
        log.debug("event: {} subscription: {} 订阅条件不匹配: {}",
            event.getEventId(), details.getSubscriptionId(), details.getCondition());
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
    String key = event.getKey();
    if (key == null) {
      key = DBDefaults.STRING_VALUE;
    }
    instance.setKey(key);
    instance.setSubscriptionId(details.getSubscriptionId());
    instance.setApplicationId(details.getApplicationId());
    instance.setTopic(event.getTopic());
    instance.setStatus(RouteInstance.STATUS_WAITING);
    instance.setMessage("waiting");
    // 延迟消费事件
    String delayExp = details.getDelayExp();
    if (StringUtils.isNotBlank(delayExp)) {
      long delaySeconds = 0;
      try {
        delaySeconds = Integer.parseInt(delayExp);
      } catch (NumberFormatException e) {
        String one = event.getHeaders().getOne(delayExp);
        if (StringUtils.isBlank(one)) {
          log.info("event: {} subscription: {} 接续延迟表达式失败, 事件头: {} 为空",
              event.getEventId(), details.getSubscriptionId(), delayExp);
        } else {
          try {
            delaySeconds = Integer.parseInt(one);
          } catch (NumberFormatException numberFormatException) {
            log.info("event: {} subscription: {} 接续延迟表达式失败, 事件头: {} 对应的值: {} 非数字类型",
                event.getEventId(), details.getSubscriptionId(), delayExp, one);
          }
        }
      }
      if (delaySeconds > 0) {
        instance.setNextPushTime(System.currentTimeMillis() + (delaySeconds * 1000));
      }
    }
    String listenerName = details.getListenerName();
    // 指定的监听器列表
    if (StringUtils.isNotBlank(listenerName)) {
      instance.setListeners(Collections.singletonList(listenerName));
    }
    return instance;
  }
}
