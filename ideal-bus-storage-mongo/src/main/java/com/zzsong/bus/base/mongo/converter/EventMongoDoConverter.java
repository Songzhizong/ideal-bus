package com.zzsong.bus.base.mongo.converter;

import com.zzsong.bus.base.domain.Event;
import com.zzsong.bus.base.mongo.document.EventMongoDo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
public final class EventMongoDoConverter {

  @Nonnull
  public static EventMongoDo fromEvent(@Nonnull Event event) {
    EventMongoDo eventMongoDo = new EventMongoDo();
    eventMongoDo.setTopic(event.getTopic());
    eventMongoDo.setModuleId(event.getModuleId());
    eventMongoDo.setEventType(event.getEventType());
    eventMongoDo.setEventName(event.getEventName());
    eventMongoDo.setDesc(event.getDesc());
    return eventMongoDo;
  }

  @Nonnull
  public static Event toEvent(@Nonnull EventMongoDo eventMongoDo) {
    Event event = new Event();
    event.setTopic(eventMongoDo.getTopic());
    event.setModuleId(eventMongoDo.getModuleId());
    event.setEventType(eventMongoDo.getEventType());
    event.setEventName(eventMongoDo.getEventName());
    event.setDesc(eventMongoDo.getDesc());
    return event;
  }
}
