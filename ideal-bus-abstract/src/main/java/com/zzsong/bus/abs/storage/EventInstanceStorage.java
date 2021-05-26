package com.zzsong.bus.abs.storage;

import com.zzsong.bus.abs.domain.EventInstance;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/17
 */
public interface EventInstanceStorage {

  @Nonnull
  Mono<EventInstance> save(@Nonnull EventInstance eventInstance);

  Mono<List<EventInstance>> saveAll(List<EventInstance> eventInstances);

  @Nonnull
  Mono<Optional<EventInstance>> findByEventId(long eventId);

  @Nonnull
  Mono<Long> deleteByIdLessThenAndTopicIn(long maxId, @Nonnull Collection<String> topics);
}
