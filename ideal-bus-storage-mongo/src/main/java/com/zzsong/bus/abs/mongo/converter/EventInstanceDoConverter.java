package com.zzsong.bus.abs.mongo.converter;

import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.mongo.document.EventInstanceDo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
@SuppressWarnings("DuplicatedCode")
public final class EventInstanceDoConverter {

  @Nonnull
  public static EventInstanceDo fromEventInstance(@Nonnull EventInstance eventInstance) {
    EventInstanceDo eventInstanceDo = new EventInstanceDo();
    eventInstanceDo.setEventId(eventInstance.getEventId());
    eventInstanceDo.setBizId(eventInstance.getBizId());
    eventInstanceDo.setApplication(eventInstance.getApplication());
    eventInstanceDo.setTopic(eventInstance.getTopic());
    eventInstanceDo.setHeaders(eventInstance.getHeaders());
    eventInstanceDo.setDelay(eventInstance.getDelay());
    eventInstanceDo.setPayload(eventInstance.getPayload());
    eventInstanceDo.setTimestamp(eventInstance.getTimestamp());
    return eventInstanceDo;
  }

  @Nonnull
  public static EventInstance toEventInstance(@Nonnull EventInstanceDo eventInstanceDo) {
    EventInstance eventInstance = new EventInstance();
    eventInstance.setEventId(eventInstanceDo.getEventId());
    eventInstance.setBizId(eventInstanceDo.getBizId());
    eventInstance.setApplication(eventInstanceDo.getApplication());
    eventInstance.setTopic(eventInstanceDo.getTopic());
    eventInstance.setHeaders(eventInstanceDo.getHeaders());
    eventInstance.setDelay(eventInstanceDo.getDelay());
    eventInstance.setPayload(eventInstanceDo.getPayload());
    eventInstance.setTimestamp(eventInstanceDo.getTimestamp());
    return eventInstance;
  }
}