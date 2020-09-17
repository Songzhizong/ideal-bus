package com.zzsong.bus.abs.storage;

import com.zzsong.bus.abs.domain.Subscription;
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
  Mono<Long> unsubscribe(long subscriberId);

  @Nonnull
  Mono<Long> unsubscribe(@Nonnull String topic);

  @Nonnull
  Mono<Long> unsubscribe(long subscriberId, @Nonnull String topic);

  @Nonnull
  Mono<List<Subscription>> findAll();

  @Nonnull
  Mono<List<Subscription>> findAllEnabled();

  @Nonnull
  Mono<List<Subscription>> findAllByTopic(@Nonnull String topic);

  @Nonnull
  Mono<List<Subscription>> findAllBySubscriber(long subscriberId);

  @Nonnull
  Mono<Boolean> existByTopic(@Nonnull String topic);

  @Nonnull
  Mono<Boolean> existBySubscriber(long subscriberId);
}
