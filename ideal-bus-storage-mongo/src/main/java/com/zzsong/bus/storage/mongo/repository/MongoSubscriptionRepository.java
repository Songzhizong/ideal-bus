package com.zzsong.bus.storage.mongo.repository;

import com.zzsong.bus.storage.mongo.document.SubscriptionDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface MongoSubscriptionRepository
    extends ReactiveMongoRepository<SubscriptionDo, Long> {

  @Nonnull
  Mono<SubscriptionDo> findFirstByTopic(@Nonnull String topic);

  @Nonnull
  Mono<SubscriptionDo> findFirstBySubscriberId(long subscriberId);

  @Nonnull
  Mono<SubscriptionDo> findFirstBySubscriberIdAndTopic(long subscriberId,
                                                       @Nonnull String topic);

  @Nonnull
  Flux<SubscriptionDo> findAllByStatus(int status);

  @Nonnull
  Flux<SubscriptionDo> findAllByTopic(@Nonnull String topic);

  @Nonnull
  Flux<SubscriptionDo> findAllBySubscriberId(long subscriberId);

  @Nonnull
  Mono<Long> deleteBySubscriptionId(long subscriptionId);

  @Nonnull
  Mono<Long> deleteAllBySubscriptionIdIn(@Nonnull Collection<Long> subscriptionIds);

  @Nonnull
  Mono<Long> deleteAllByTopic(@Nonnull String topic);

  @Nonnull
  Mono<Long> deleteAllBySubscriberId(long subscriberId);

  @Nonnull
  Mono<Long> deleteBySubscriberIdAndTopic(long subscriberId, @Nonnull String topic);

}
