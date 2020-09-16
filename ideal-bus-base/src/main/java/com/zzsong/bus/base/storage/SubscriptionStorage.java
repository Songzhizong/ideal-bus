package com.zzsong.bus.base.storage;

import com.zzsong.bus.base.domain.Subscription;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface SubscriptionStorage {
  @Nonnull
  Mono<Subscription> save(@Nonnull Subscription subscription);

  @Nonnull
  Mono<Integer> unsubscribe(long subscriberId);

  @Nonnull
  Mono<Integer> unsubscribe(@Nonnull String topic);

  @Nonnull
  Mono<Integer> unsubscribe(long subscriberId, @Nonnull String topic);

  @Nonnull
  Mono<List<Subscription>> findAll();
}
