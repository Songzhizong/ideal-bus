package com.zzsong.bus.base.mongo;

import com.zzsong.bus.base.domain.Event;
import com.zzsong.bus.base.storage.EventStorage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Component
public class MongoEventStorage implements EventStorage {
  @Nonnull
  @Override
  public Mono<Event> save(@Nonnull Event event) {
    return null;
  }

  @Nonnull
  @Override
  public Mono<Integer> delete(@Nonnull String topic) {
    return null;
  }

  @Nonnull
  @Override
  public Mono<Optional<Event>> findByTopic(@Nonnull String topic) {
    return null;
  }

  @Nonnull
  @Override
  public Mono<List<Event>> findAll() {
    return null;
  }
}
