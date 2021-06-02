package com.zzsong.bus.storage.mongo.converter;

import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.storage.mongo.document.EventInstanceDo;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
@SuppressWarnings("DuplicatedCode")
public final class EventInstanceDoConverter {
  private EventInstanceDoConverter() {
  }

  @Nonnull
  public static EventInstanceDo fromEventInstance(@Nonnull EventInstance eventInstance) {
    EventInstanceDo eventInstanceDo = new EventInstanceDo();
    eventInstanceDo.setEventId(eventInstance.getEventId());
    eventInstanceDo.setUuid(eventInstance.getUuid());
    String transactionId = eventInstance.getTransactionId();
    if (StringUtils.isBlank(transactionId)) {
      eventInstanceDo.setTransactionId(null);
    } else {
      eventInstanceDo.setTransactionId(transactionId);
    }
    eventInstanceDo.setEntity(eventInstance.getEntity());
    eventInstanceDo.setAggregate(eventInstance.getAggregate());
    String externalApp = eventInstance.getExternalApp();
    if (StringUtils.isNotBlank(externalApp)) {
      eventInstanceDo.setExternalApp(externalApp);
    }
    eventInstanceDo.setTopic(eventInstance.getTopic());
    eventInstanceDo.setHeaders(eventInstance.getHeaders());
    eventInstanceDo.setPayload(eventInstance.getPayload());
    eventInstanceDo.setTimestamp(eventInstance.getTimestamp());
    return eventInstanceDo;
  }

  @Nonnull
  public static EventInstance toEventInstance(@Nonnull EventInstanceDo eventInstanceDo) {
    EventInstance eventInstance = new EventInstance();
    eventInstance.setEventId(eventInstanceDo.getEventId());
    eventInstance.setUuid(eventInstanceDo.getUuid());
    eventInstance.setTransactionId(eventInstanceDo.getTransactionId());
    eventInstance.setEntity(eventInstanceDo.getEntity());
    eventInstance.setAggregate(eventInstanceDo.getAggregate());
    eventInstance.setExternalApp(eventInstanceDo.getExternalApp());
    eventInstance.setTopic(eventInstanceDo.getTopic());
    eventInstance.setHeaders(eventInstanceDo.getHeaders());
    eventInstance.setPayload(eventInstanceDo.getPayload());
    eventInstance.setTimestamp(eventInstanceDo.getTimestamp());
    return eventInstance;
  }
}
