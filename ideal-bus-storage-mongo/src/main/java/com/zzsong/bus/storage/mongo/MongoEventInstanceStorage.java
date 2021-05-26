package com.zzsong.bus.storage.mongo;

import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.generator.IDGenerator;
import com.zzsong.bus.abs.generator.IDGeneratorFactory;
import com.zzsong.bus.abs.storage.EventInstanceStorage;
import com.zzsong.bus.storage.mongo.converter.EventInstanceDoConverter;
import com.zzsong.bus.storage.mongo.document.EventInstanceDo;
import com.zzsong.bus.storage.mongo.repository.MongoEventInstanceRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Component
public class MongoEventInstanceStorage implements EventInstanceStorage {
  @Nonnull
  private final IDGenerator idGenerator;
  private final MongoEventInstanceRepository repository;

  public MongoEventInstanceStorage(MongoEventInstanceRepository repository,
                                   @Nonnull IDGeneratorFactory idGeneratorFactory) {
    this.idGenerator = idGeneratorFactory.getGenerator("eventInstance");
    this.repository = repository;
  }

  @Nonnull
  @Override
  public Mono<EventInstance> save(@Nonnull EventInstance eventInstance) {
    if (eventInstance.getEventId() == null) {
      eventInstance.setEventId(idGenerator.generate());
    }
    EventInstanceDo instanceDo = EventInstanceDoConverter.fromEventInstance(eventInstance);
    return repository.save(instanceDo).map(EventInstanceDoConverter::toEventInstance);
  }

  @Override
  public Mono<List<EventInstance>> saveAll(@Nonnull List<EventInstance> eventInstances) {
    List<EventInstanceDo> collect = eventInstances.stream().map(eventInstance -> {
      if (eventInstance.getEventId() == null) {
        eventInstance.setEventId(idGenerator.generate());
      }
      return EventInstanceDoConverter.fromEventInstance(eventInstance);
    }).collect(Collectors.toList());
    return repository.saveAll(collect).map(EventInstanceDoConverter::toEventInstance).collectList();
  }

  @Nonnull
  @Override
  public Mono<Optional<EventInstance>> findByEventId(long eventId) {
    return repository.findById(eventId)
        .map(EventInstanceDoConverter::toEventInstance)
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty());
  }
}
