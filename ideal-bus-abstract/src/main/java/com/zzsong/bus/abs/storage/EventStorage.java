package com.zzsong.bus.abs.storage;

import com.zzsong.bus.abs.domain.Event;
import com.zzsong.bus.abs.transfer.QueryEventArgs;
import com.zzsong.bus.common.transfer.Paging;
import com.zzsong.bus.common.transfer.Res;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface EventStorage {
  @Nonnull
  Mono<Event> save(@Nonnull Event event);

  @Nonnull
  Mono<Long> delete(@Nonnull String topic);

  @Nonnull
  Mono<Optional<Event>> findByTopic(@Nonnull String topic);

  @Nonnull
  Mono<List<Event>> findAll();

  @Nonnull
  Mono<List<Event>> findAll(@Nonnull Collection<String> topicList);

  @Nonnull
  Mono<Res<List<Event>>> query(@Nullable QueryEventArgs args,
                               @Nonnull Paging paging);
}
