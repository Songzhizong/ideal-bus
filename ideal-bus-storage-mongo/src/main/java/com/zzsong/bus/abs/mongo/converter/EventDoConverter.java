package com.zzsong.bus.abs.mongo.converter;

import com.zzsong.bus.abs.domain.Event;
import com.zzsong.bus.abs.mongo.document.EventDo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
public final class EventDoConverter {

  @Nonnull
  public static EventDo fromEvent(@Nonnull Event event) {
    EventDo eventDo = new EventDo();
    eventDo.setTopic(event.getTopic());
    eventDo.setModuleId(event.getModuleId());
    eventDo.setEventType(event.getEventType());
    eventDo.setEventName(event.getEventName());
    eventDo.setDesc(event.getDesc());
    return eventDo;
  }

  @Nonnull
  public static Event toEvent(@Nonnull EventDo eventDo) {
    Event event = new Event();
    event.setTopic(eventDo.getTopic());
    event.setModuleId(eventDo.getModuleId());
    event.setEventType(eventDo.getEventType());
    event.setEventName(eventDo.getEventName());
    event.setDesc(eventDo.getDesc());
    return event;
  }
}
