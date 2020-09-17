package com.zzsong.bus.client.deliver;

import com.fasterxml.jackson.databind.JavaType;
import com.zzsong.bus.client.EventContext;
import com.zzsong.bus.client.listener.IEventListener;
import com.zzsong.bus.client.listener.ListenerFactory;
import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import com.zzsong.bus.common.message.EventHeaders;
import com.zzsong.bus.common.util.ConditionMatcher;
import com.zzsong.bus.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/17
 */
public class EventDelivererImpl implements EventDeliverer {
  private static final Logger log = LoggerFactory.getLogger(EventDelivererImpl.class);

  @Nonnull
  private final Scheduler scheduler;

  public EventDelivererImpl(@Nonnull Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @Nonnull
  @Override
  public Mono<DeliveredResult> deliver(@Nonnull DeliveredEvent event) {
    DeliveredResult deliveredResult = new DeliveredResult();
    deliveredResult.setInstanceId(event.getInstanceId());

    String topic = event.getTopic();
    Map<String, IEventListener> listenerMap = ListenerFactory.get(topic);
    if (listenerMap.isEmpty()) {
      log.warn("topic: {} 没有事件监听器", topic);
      return Mono.just(deliveredResult);
    }
    EventHeaders headers = event.getHeaders();
    Object payload = event.getPayload();
    String payloadString = JsonUtils.toJsonString(payload);
    List<String> listeners = event.getListeners();

    // 存储待调用的监听器列表
    List<IEventListener> invorkListeners = new ArrayList<>();
    if (listeners != null && listeners.size() > 0) {
      // 指定的监听器不为空, 直接交给该监听器执行
      for (String listenerName : listeners) {
        IEventListener listener = listenerMap.get(listenerName);
        if (listener == null) {
          // 上一轮投递的事件某些监听器没有ack, 下一轮才会指定监听器.
          // 这里找不到指定的监听器属于异常情况, 可能是客户端各个节点的代码版本不一致导致的
          log.error("topic: {} 没有名称为: {} 的监听器", topic, listenerName);
          deliveredResult.markAck(listenerName, false);
        } else {
          invorkListeners.add(listener);
        }
      }
    } else {
      // 没有指定监听器, 则根据监听条件筛选出准备执行的监听器列表
      Collection<IEventListener> eventListeners = listenerMap.values();
      for (IEventListener eventListener : eventListeners) {
        List<Set<String>> conditionsGroup = eventListener.getConditionsGroup();
        // 没有通过条件判断的直接跳过
        if (!ConditionMatcher.match(conditionsGroup, headers)) {
          continue;
        }
        invorkListeners.add(eventListener);
      }
    }
    // 待执行的监听器列表为空, 直接返回结果
    if (invorkListeners.isEmpty()) {
      return Mono.just(deliveredResult);
    }
    List<Mono<EventContext<Object>>> monoList = invorkListeners.stream()
        .map(l ->
            Mono.just(l)
                .map(listener -> {
                  String listenerName = listener.getListenerName();
                  JavaType payloadType = listener.getPayloadType();
                  Object param = JsonUtils.parseJson(payloadString, payloadType);
                  EventContext<Object> context = new EventContext<>(listenerName);
                  context.setPayload(param);
                  try {
                    listener.invoke(context);
                  } catch (Exception e) {
                    log.info("");
                    context.setAck(false);
                  }
                  //noinspection ConstantConditions
                  if (context.getAck() == null) {
                    // 如果开发者没有手动设置ack, 则根据配置来进行ack设置
                    context.setAck(listener.isAutoAck());
                  }
                  return context;
                }).subscribeOn(scheduler)
        ).collect(Collectors.toList());
    return Flux.merge(monoList)
        .collectList()
        .map(eventContexts -> {
          for (EventContext<Object> eventContext : eventContexts) {
            String listenerName = eventContext.getListenerName();
            Boolean ack = eventContext.getAck();
            deliveredResult.markAck(listenerName, ack);
          }
          return deliveredResult;
        });
  }
}
