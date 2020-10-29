package com.zzsong.bus.client.deliver;

import com.fasterxml.jackson.databind.JavaType;
import com.zzsong.bus.client.listener.IEventListener;
import com.zzsong.bus.client.listener.ListenerFactory;
import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import com.zzsong.bus.common.message.EventHeaders;
import com.zzsong.bus.common.util.ConditionMatcher;
import com.zzsong.bus.common.share.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Slf4j
public class EventDelivererImpl implements EventDeliverer {

  @Nonnull
  private final Scheduler scheduler;

  public EventDelivererImpl(@Nonnull Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @Nonnull
  @Override
  public Mono<DeliveredResult> deliver(@Nonnull DeliveredEvent event) {
    DeliveredResult deliveredResult = new DeliveredResult();
    deliveredResult.setEventId(event.getEventId());
    deliveredResult.setSuccess(true);

    //  获取待执行的监听器列表
    List<IEventListener> invokeListeners = getListeners(event, deliveredResult);
    // 待执行的监听器列表为空, 直接返回结果
    if (invokeListeners.isEmpty()) {
      return Mono.just(deliveredResult);
    }

    if (log.isDebugEnabled()) {
      String join = invokeListeners.stream()
          .map(IEventListener::getListenerName)
          .collect(Collectors.joining(", "));
      log.debug("待执行的监听器列表: {}", join);
    }

    Object payload = event.getPayload();
    String payloadString = JsonUtils.toJsonString(payload);
    List<Mono<EventContext<Object>>> monoList = invokeListeners.stream()
        .map(l -> Mono.just(l)
            .map(listener -> {
              String listenerName = listener.getListenerName();
              JavaType payloadType = listener.getPayloadType();
              Object param = JsonUtils.parseJson(payloadString, payloadType);
              EventContext<Object> context = new EventContext<>(param, listenerName);
              context.setEventId(event.getEventId());
              context.setBizId(event.getBizId());
              context.setHeaders(event.getHeaders());
              context.setTimestamp(event.getTimestamp());
              try {
                listener.invoke(context);
                if (listener.isAutoAck()) {
                  context.ack();
                }
              } catch (Exception e) {
                String errMessage = e.getClass().getName() + ":" + e.getMessage();
                log.info("event处理异常: ", e);
                context.reject(errMessage);
              }
              return context;
            })
            .subscribeOn(scheduler)
        ).collect(Collectors.toList());
    return Flux.merge(monoList)
        .collectList()
        .map(eventContexts -> {
          for (EventContext<Object> eventContext : eventContexts) {
            String listenerName = eventContext.getListenerName();
            boolean ack = eventContext.isAck();
            if (!ack) {
              // 只要存在未ack的就说明执行失败了
              deliveredResult.setSuccess(false);
            }
            deliveredResult.markAck(listenerName, ack);
          }
          return deliveredResult;
        });
  }

  /**
   * 根据event信息获取待通知的监听器列表
   *
   * @param event           event信息
   * @param deliveredResult 响应结果
   * @return 待通知的监听器列表
   */
  @Nonnull
  private List<IEventListener> getListeners(@Nonnull DeliveredEvent event,
                                            DeliveredResult deliveredResult) {
    String topic = event.getTopic();
    Map<String, IEventListener> listenerMap = ListenerFactory.get(topic);
    if (listenerMap.isEmpty()) {
      log.warn("topic: {} 没有事件监听器", topic);
      return Collections.emptyList();
    }

    List<IEventListener> invokeListeners = null;
    EventHeaders headers = event.getHeaders();
    List<String> listeners = event.getListeners();
    if (listeners != null && listeners.size() > 0) {
      // 指定的监听器不为空, 直接交给该监听器执行
      for (String listenerName : listeners) {
        IEventListener listener = listenerMap.get(listenerName);
        if (listener == null) {
          // 上一轮投递的事件某些监听器没有ack, 下一轮才会指定监听器.
          // 这里找不到指定的监听器属于异常情况, 可能是客户端各个节点的代码版本不一致导致的
          log.error("topic: {} 没有名称为: {} 的监听器", topic, listenerName);
          deliveredResult.setSuccess(false);
          deliveredResult.markAck(listenerName, false);
        } else {
          if (invokeListeners == null) {
            invokeListeners = new ArrayList<>();
          }
          invokeListeners.add(listener);
        }
      }
    } else {
      // 没有指定监听器, 则根据监听条件筛选出准备执行的监听器列表
      Collection<IEventListener> eventListeners = listenerMap.values();
      for (IEventListener eventListener : eventListeners) {
        List<Set<String>> conditionsGroup = eventListener.getConditionsGroup();
        // 没有通过条件判断的直接跳过
        if (!ConditionMatcher.match(conditionsGroup, headers)) {
          if (log.isDebugEnabled()) {
            String conditions = JsonUtils.toJsonString(conditionsGroup);
            log.debug("条件不匹配 -> event: {} listener: {} conditions: {}",
                event.getEventId(), eventListener.getListenerName(), conditions);
          }
          continue;
        }
        if (invokeListeners == null) {
          invokeListeners = new ArrayList<>();
        }
        invokeListeners.add(eventListener);
      }
    }
    if (invokeListeners == null) {
      invokeListeners = Collections.emptyList();
    }
    return invokeListeners;
  }
}
