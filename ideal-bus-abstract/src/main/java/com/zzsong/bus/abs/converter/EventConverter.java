package com.zzsong.bus.abs.converter;

import com.zzsong.bus.abs.domain.Event;
import com.zzsong.bus.abs.transfer.SaveEventArgs;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
public final class EventConverter {

  @SuppressWarnings("DuplicatedCode")
  @Nonnull
  public static Event fromCreateArgs(@Nonnull SaveEventArgs args) {
    Event event = new Event();
    event.setTopic(args.getTopic());
    event.setModuleId(args.getModuleId());
    event.setEventType(args.getEventType());
    event.setEventName(args.getEventName());
    event.setDesc(args.getDesc());
    return event;
  }
}
