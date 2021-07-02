package com.zzsong.bus.ideal.boot.starter.event;

import cn.idealframework.core.json.jackson.JacksonUtils;
import cn.idealframework.event.listener.EventDeliverer;
import cn.idealframework.event.message.impl.EventHeadersImpl;
import cn.idealframework.event.message.impl.SimpleDelivererEvent;
import com.zzsong.bus.client.Channel;
import com.zzsong.bus.client.EventConsumer;
import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.message.EventHeaders;
import com.zzsong.bus.common.share.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2021/6/2
 */
@CommonsLog
@RequiredArgsConstructor
public class BusEventConsumer implements EventConsumer {
  private final EventDeliverer eventDeliverer;

  @Override
  public void onMessage(@Nonnull DeliverEvent event, @Nonnull Channel channel) {
    long routeInstanceId = event.getRouteInstanceId();
    SimpleDelivererEvent delivererEvent = convert(event);
    try {
      eventDeliverer.deliver(delivererEvent);
      channel.ack(routeInstanceId);
    } catch (Exception e) {
      String message = e.getMessage();
      log.info("事件交付异常: ", e);
      channel.reject(routeInstanceId, message);
    }
  }

  @Nonnull
  private SimpleDelivererEvent convert(@Nonnull DeliverEvent event) {
    SimpleDelivererEvent simpleDelivererEvent = new SimpleDelivererEvent();
    simpleDelivererEvent.setListenerName(event.getListener());
    simpleDelivererEvent.setUuid(event.getUuid());
    simpleDelivererEvent.setTopic(event.getTopic());
    simpleDelivererEvent.setAggregateType(event.getEntity());
    simpleDelivererEvent.setAggregateId(event.getAggregate());
    EventHeaders headers = event.getHeaders();
    String jsonString = JsonUtils.toJsonString(headers);
    EventHeadersImpl eventHeaders = JacksonUtils.parse(jsonString, EventHeadersImpl.class);
    simpleDelivererEvent.setHeaders(eventHeaders);
    simpleDelivererEvent.setPayload(event.getPayload());
    simpleDelivererEvent.setEventTime(event.getTimestamp());
    return simpleDelivererEvent;
  }
}
