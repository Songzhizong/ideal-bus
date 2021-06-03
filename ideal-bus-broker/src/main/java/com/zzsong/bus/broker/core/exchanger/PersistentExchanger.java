package com.zzsong.bus.broker.core.exchanger;

import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.abs.storage.EventInstanceStorage;
import com.zzsong.bus.broker.config.BusProperties;
import com.zzsong.bus.broker.core.SubscriptionManager;
import com.zzsong.bus.broker.core.queue.QueueManager;
import com.zzsong.bus.common.message.ExchangeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 持久化交换机
 * <pre>
 *   发布的事件将被持久化保存
 * </pre>
 *
 * @author 宋志宗 on 2021/5/14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersistentExchanger implements Exchanger {
  private final BusProperties properties;
  private final EventInstanceStorage eventInstanceStorage;
  private final SubscriptionManager subscriptionManager;
  private final QueueManager queueManager;

  @Override
  public Mono<ExchangeResult> exchange(@Nonnull List<EventInstance> events) {
    Mono<List<RouteInstance>> listMono = eventInstanceStorage
        .saveAll(events) // 1. 持久化
        .flatMap(list -> Flux.fromIterable(list)
            .flatMap(this::route) // 2. 获取路由消息
            .collectList()
            .map(dList -> dList.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
            ) // 3. List<List<RouteInstance>> -> List<RouteInstance>
        );
    // 提交到队列管理器并返回执行结果
    return listMono.flatMap(queueManager::submit)
        .map(b -> ExchangeResult.builder().success(true).message("success").build())
        .onErrorResume(throwable -> {
          log.info("exchange ex: ", throwable);
          ExchangeResult result = ExchangeResult.builder()
              .success(false)
              .message(throwable.getMessage())
              .build();
          return Mono.just(result);
        });
  }


  // ---------------------------------------- private methods ~

  @Nonnull
  private Mono<List<RouteInstance>> route(@Nonnull EventInstance event) {
    return subscriptionManager.getSubscriptions(event)
        .flatMap(list -> {
          try {
            List<RouteInstance> collect = list.stream()
                .map(d -> createRouteInstance(event, d))
                .collect(Collectors.toList());
            return Mono.just(collect);
          } catch (Exception e) {
            log.error("createRouteInstance ex: ", e);
            return Mono.error(e);
          }
        })
        .doOnNext(list -> {
          int size = list.size();
          if (size == 0) {
            String topic = event.getTopic();
            log.debug("topic: {} 没有订阅者", topic);
          }
        });
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
    instance.setStatus(RouteInstance.STATUS_QUEUING);
    instance.setStatusTime(System.currentTimeMillis());
    instance.setMessage("init");
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
