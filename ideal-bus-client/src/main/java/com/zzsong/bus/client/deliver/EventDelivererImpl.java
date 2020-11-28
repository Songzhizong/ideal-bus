package com.zzsong.bus.client.deliver;

import com.fasterxml.jackson.databind.JavaType;
import com.zzsong.bus.client.listener.IEventListener;
import com.zzsong.bus.client.listener.ListenerFactory;
import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.message.DeliverResult;
import com.zzsong.bus.common.share.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

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
  public Mono<DeliverResult> deliver(@Nonnull DeliverEvent event) {
    DeliverResult deliveredResult = new DeliverResult();
    deliveredResult.setEventId(event.getEventId());
    deliveredResult.setStatus(DeliverResult.Status.ACK);

    //  获取待执行的监听器
    IEventListener listener = getListener(event);
    if (listener == null) {
      return Mono.just(deliveredResult);
    }
    return Mono.just(event)
        .map(ev -> {
          Object payload = event.getPayload();
          String payloadString = JsonUtils.toJsonString(payload);
          JavaType payloadType = listener.getPayloadType();
          Object param = JsonUtils.parseJson(payloadString, payloadType);
          EventContext<Object> context = new EventContext<>(param);
          context.setEventId(event.getEventId());
          context.setTransactionId(event.getTransactionId());
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
          if (!context.isAck()) {
            deliveredResult.setStatus(DeliverResult.Status.UN_ACK);
          }
          deliveredResult.setMessage(context.getMessage());
          return deliveredResult;
        }).subscribeOn(scheduler);
  }

  /**
   * 根据event信息获取待通知的监听器列表
   *
   * @param event event信息
   * @return 待通知的监听器列表
   */
  @Nullable
  private IEventListener getListener(@Nonnull DeliverEvent event) {
    String topic = event.getTopic();
    Map<String, IEventListener> listenerMap = ListenerFactory.get(topic);
    if (listenerMap.isEmpty()) {
      log.warn("topic: {} 没有事件监听器", topic);
      return null;
    }
    String listenerName = event.getListener();
    IEventListener listener = listenerMap.get(listenerName);
    if (listener == null) {
      // 上一轮投递的事件某些监听器没有ack, 下一轮才会指定监听器.
      // 这里找不到指定的监听器属于异常情况, 可能是客户端各个节点的代码版本不一致导致的
      log.error("topic: {} 没有名称为: {} 的监听器", topic, listenerName);
      return null;
    } else {
      return listener;
    }
  }
}
