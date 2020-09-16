package com.zzsong.bus.base.storage;

import com.zzsong.bus.base.domain.Event;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface EventStorage {
  @Nonnull
  Mono<Event> save(@Nonnull Event event);

  @Nonnull
  Mono<Integer> delete(@Nonnull String topic);

  @Nonnull
  Mono<Optional<Event>> findByTopic(@Nonnull String topic);

  @Nonnull
  Mono<List<Event>> findAll();
}
