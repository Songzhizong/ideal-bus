package com.zzsong.bus.client.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.zzsong.bus.client.Channel;
import com.zzsong.bus.client.EventConsumer;
import com.zzsong.bus.client.deliver.EventContext;
import com.zzsong.bus.client.listener.IEventListener;
import com.zzsong.bus.client.listener.ListenerFactory;
import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.share.utils.JsonUtils;
import lombok.extern.apachecommons.CommonsLog;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author 宋志宗 on 2021/5/25
 */
@CommonsLog
public class AutoDeliverEventConsumer implements EventConsumer {
  @Override
  public void onMessage(@Nonnull DeliverEvent event, @Nonnull Channel channel) {
    long routeInstanceId = event.getRouteInstanceId();
    IEventListener listener = getListener(event);
    if (listener == null) {
      channel.ack(routeInstanceId);
      return;
    }
    JavaType payloadType = listener.getPayloadType();
    Object param = JsonUtils.parseJson(event.getPayload(), payloadType);
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
    if (context.isAck()) {
      channel.ack(routeInstanceId);
    } else {
      channel.reject(routeInstanceId, context.getMessage());
    }
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
      log.warn("topic: " + topic + " 没有事件监听器");
      return null;
    }
    String listenerName = event.getListener();
    IEventListener listener = listenerMap.get(listenerName);
    if (listener == null) {
      // 上一轮投递的事件某些监听器没有ack, 下一轮才会指定监听器.
      // 这里找不到指定的监听器属于异常情况, 可能是客户端各个节点的代码版本不一致导致的
      log.error("topic: " + topic + " 没有名称为: " + listenerName + " 的监听器");
      return null;
    } else {
      return listener;
    }
  }
}
