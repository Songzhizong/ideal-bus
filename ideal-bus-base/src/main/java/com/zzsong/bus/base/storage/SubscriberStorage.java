package com.zzsong.bus.base.storage;

import com.zzsong.bus.base.domain.Subscriber;
import com.zzsong.bus.base.transfer.QuerySubscriberArgs;
import com.zzsong.bus.common.transfer.Paging;
import com.zzsong.bus.common.transfer.Res;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface SubscriberStorage {
  @Nonnull
  Mono<Subscriber> save(@Nonnull Subscriber subscriber);

  @Nonnull
  Mono<Long> delete(long subscriberId);

  @Nonnull
  Mono<Optional<Subscriber>> findById(long subscriberId);

  @Nonnull
  Mono<List<Subscriber>> findAll();

  Mono<Res<List<Subscriber>>> query(@Nullable QuerySubscriberArgs args,
                              @Nonnull Paging paging);
}
