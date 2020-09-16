package com.zzsong.bus.core.admin.service;

import com.zzsong.bus.base.converter.EventConverter;
import com.zzsong.bus.base.domain.Event;
import com.zzsong.bus.base.storage.EventStorage;
import com.zzsong.bus.base.transfer.SaveEventArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Service
public class EventService {
  private static final Logger log = LoggerFactory.getLogger(EventService.class);
  @Nonnull
  private final EventStorage eventStorage;

  public EventService(@Nonnull EventStorage eventStorage) {
    this.eventStorage = eventStorage;
  }

  @Nonnull
  public Mono<Event> save(@Nonnull SaveEventArgs args) {
    Event event = EventConverter.fromCreateArgs(args);
    return eventStorage.save(event);
  }
}
