package com.zzsong.bus.abs.storage;

import com.zzsong.bus.abs.domain.Subscription;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface SubscriptionStorage {
  @Nonnull
  Mono<Subscription> save(@Nonnull Subscription subscription);

  @Nonnull
  Mono<List<Subscription>> saveAll(@Nonnull Collection<Subscription> subscriptions);

  /**
   * 通过订阅关系ID 解除订阅
   *
   * @param subscriptionId 订阅关系ID
   * @return 解除条数
   */
  @Nonnull
  Mono<Long> unsubscribe(long subscriptionId);

  @Nonnull
  Mono<Long> unsubscribe(long subscriberId, @Nonnull String topic);

  /**
   * 通过订阅关系ID列表批量解除订阅
   *
   * @param subscriptionIds 订阅关系ID列表
   * @return 解除条数
   */
  @Nonnull
  Mono<Long> unsubscribeAll(@Nonnull Collection<Long> subscriptionIds);

  @Nonnull
  Mono<Long> unsubscribeAll(long subscriberId);

  @Nonnull
  Mono<Long> unsubscribeAll(@Nonnull String topic);

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
