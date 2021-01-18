package com.zzsong.bus.broker.core;

import com.zzsong.bus.abs.constants.DBDefaults;
import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.broker.admin.service.EventInstanceService;
import com.zzsong.bus.broker.config.BusProperties;
import com.zzsong.bus.broker.core.transfer.RouteTransfer;
import com.zzsong.bus.common.message.PublishResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventExchanger {
  @Nonnull
  private final BusProperties properties;
  @Nonnull
  private final RouteTransfer routeTransfer;
  @Nonnull
  private final EventInstanceService eventInstanceService;
  @Nonnull
  private final SubscriptionManager subscriptionManager;

  @Nonnull
  public Mono<PublishResult> exchange(@Nonnull EventInstance event) {
    PublishResult.PublishResultBuilder builder = PublishResult.builder()
        .transactionId(event.getTransactionId())
        .topic(event.getTopic())
        .message("success")
        .success(true);
    Mono<EventInstance> savedEvent = eventInstanceService.save(event);
    Mono<List<RouteInstance>> route = savedEvent
        .doOnNext(e -> builder.eventId(e.getEventId()))
        // 路由, 获取满足订阅条件的订阅者列表
        .flatMap(this::route);
    return route.flatMap(instanceList -> {
      if (instanceList.isEmpty()) {
        return Mono.just(builder.message("该事件没有订阅者").build());
      }
      return Flux.fromIterable(instanceList)
          .flatMap(routeTransfer::submit)
          .collectList()
          .thenReturn(builder.build());
    });
  }

  // ---------------------------------------- private methods ~

  @Nonnull
  private Mono<List<RouteInstance>> route(@Nonnull EventInstance event) {
    return subscriptionManager.getSubscriptions(event)
        .map(list -> list.stream()
            .map(d -> createRouteInstance(event, d))
            .collect(Collectors.toList()));
  }

  @Nonnull
  @SuppressWarnings("DuplicatedCode")
  private RouteInstance createRouteInstance(@Nonnull EventInstance event,
                                            @Nonnull SubscriptionDetails details) {
    RouteInstance instance = new RouteInstance();
    instance.setEventId(event.getEventId());
    instance.setTransactionId(event.getTransactionId());
    instance.setEntity(event.getEntity());
    instance.setAggregate(event.getAggregate());
    instance.setExternalApp(event.getExternalApp());
    instance.setTopic(event.getTopic());
    instance.setHeaders(event.getHeaders());
    instance.setPayload(event.getPayload());
    instance.setTimestamp(event.getTimestamp());
    instance.setShard(properties.getNodeId());
    instance.setSubscriptionId(details.getSubscriptionId());
    instance.setApplicationId(details.getApplicationId());
    instance.setBroadcast(details.isBroadcast());
    instance.setStatus(RouteInstance.STATUS_WAITING);
    instance.setMessage("waiting");
    instance.setRetryLimit(details.getRetryCount());

    Long delaySeconds = getDelaySeconds(event, details);
    if (delaySeconds != null && delaySeconds > 0) {
      instance.setNextPushTime(System.currentTimeMillis() + (delaySeconds * 1000));
    }

    String listenerName = details.getListenerName();
    // 指定的监听器列表
    if (StringUtils.isNotBlank(listenerName)) {
      instance.setListener(listenerName);
    }
    return instance;
  }

  @Nullable
  private Long getDelaySeconds(@Nonnull EventInstance event,
                               @Nonnull SubscriptionDetails details) {
    String delayExp = details.getDelayExp();
    if (StringUtils.isBlank(delayExp)) {
      return null;
    }
    try {
      return Long.parseLong(delayExp);
    } catch (NumberFormatException e) {
      String one = event.getHeaders().getOne(delayExp);
      if (StringUtils.isBlank(one)) {
        log.info("event: {} subscription: {} 解析延迟表达式失败, 事件头: {} 为空",
            event.getEventId(), details.getSubscriptionId(), delayExp);
      } else {
        try {
          return Long.parseLong(one);
        } catch (NumberFormatException numberFormatException) {
          log.info("event: {} subscription: {} 解析延迟表达式失败, 事件头: {} 对应的值: {} 非数字类型",
              event.getEventId(), details.getSubscriptionId(), delayExp, one);
        }
      }
    }
    return null;
  }
}