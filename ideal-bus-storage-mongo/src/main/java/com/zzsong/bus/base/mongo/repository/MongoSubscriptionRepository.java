package com.zzsong.bus.base.mongo.repository;

import com.zzsong.bus.base.mongo.document.SubscriptionMongoDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface MongoSubscriptionRepository
    extends ReactiveMongoRepository<SubscriptionMongoDo, Long> {

  @Nonnull
  Mono<SubscriptionMongoDo> findFirstByTopic(@Nonnull String topic);

  @Nonnull
  Mono<SubscriptionMongoDo> findFirstBySubscriberId(long subscriberId);

  @Nonnull
  Mono<SubscriptionMongoDo> findFirstBySubscriberIdAndTopic(long subscriberId, @Nonnull String topic);

  @Nonnull
  Flux<SubscriptionMongoDo> findAllByTopic(@Nonnull String topic);

  @Nonnull
  Flux<SubscriptionMongoDo> findAllBySubscriberId(long subscriberId);

  @Nonnull
  Mono<Long> deleteAllByTopic(@Nonnull String topic);

  @Nonnull
  Mono<Long> deleteAllBySubscriberId(long subscriberId);

  @Nonnull
  Mono<Long> deleteBySubscriberIdAndTopic(long subscriberId, @Nonnull String topic);

}
