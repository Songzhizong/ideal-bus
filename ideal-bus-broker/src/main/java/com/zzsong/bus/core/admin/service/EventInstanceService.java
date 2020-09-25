package com.zzsong.bus.core.admin.service;

import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.storage.EventInstanceStorage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Service
public class EventInstanceService {
  @Nonnull
  private final EventInstanceStorage storage;

  public EventInstanceService(@Nonnull EventInstanceStorage storage) {
    this.storage = storage;
  }

  @Nonnull
  public Mono<EventInstance> save(@Nonnull EventInstance eventInstance) {
    return storage.save(eventInstance);
  }

  public Mono<Optional<EventInstance>> loadByEventId(@Nonnull String eventId) {
    return storage.findByEventId(eventId);
  }
}
