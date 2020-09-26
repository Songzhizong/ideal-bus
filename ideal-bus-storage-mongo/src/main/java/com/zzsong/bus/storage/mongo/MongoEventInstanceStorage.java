package com.zzsong.bus.storage.mongo;

import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.storage.EventInstanceStorage;
import com.zzsong.bus.storage.mongo.converter.EventInstanceDoConverter;
import com.zzsong.bus.storage.mongo.document.EventInstanceDo;
import com.zzsong.bus.storage.mongo.repository.MongoEventInstanceRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Component
public class MongoEventInstanceStorage implements EventInstanceStorage {
  private final MongoEventInstanceRepository repository;

  public MongoEventInstanceStorage(MongoEventInstanceRepository repository) {
    this.repository = repository;
  }

  @Nonnull
  @Override
  public Mono<EventInstance> save(@Nonnull EventInstance eventInstance) {
    EventInstanceDo instanceDo = EventInstanceDoConverter.fromEventInstance(eventInstance);
    return repository.save(instanceDo).map(EventInstanceDoConverter::toEventInstance);
  }

  @Nonnull
  @Override
  public Flux<EventInstance> saveAll(@Nonnull Flux<EventInstance> eventInstances) {
    Flux<EventInstanceDo> map = eventInstances.map(EventInstanceDoConverter::fromEventInstance);
    return repository.saveAll(map).map(EventInstanceDoConverter::toEventInstance);
  }

  @Nonnull
  @Override
  public Mono<Optional<EventInstance>> findByEventId(@Nonnull String eventId) {
    return repository.findById(eventId)
        .map(EventInstanceDoConverter::toEventInstance)
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty());
  }
}
