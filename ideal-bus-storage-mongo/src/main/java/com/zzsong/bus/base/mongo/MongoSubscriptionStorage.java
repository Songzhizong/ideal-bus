package com.zzsong.bus.base.mongo;

import com.zzsong.bus.base.domain.Subscription;
import com.zzsong.bus.base.mongo.converter.SubscriptionMongoDoConverter;
import com.zzsong.bus.base.mongo.document.SubscriptionMongoDo;
import com.zzsong.bus.base.mongo.repository.MongoSubscriptionRepository;
import com.zzsong.bus.base.storage.SubscriptionStorage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Component
public class MongoSubscriptionStorage implements SubscriptionStorage {

  @Nonnull
  private final MongoSubscriptionRepository repository;

  public MongoSubscriptionStorage(@Nonnull MongoSubscriptionRepository repository) {
    this.repository = repository;
  }

  @Nonnull
  @Override
  public Mono<Subscription> save(@Nonnull Subscription subscription) {
    SubscriptionMongoDo mongoDo = SubscriptionMongoDoConverter.fromSubscription(subscription);
    return repository.save(mongoDo).map(SubscriptionMongoDoConverter::toSubscription);
  }

  @Nonnull
  @Override
  public Mono<Long> unsubscribe(long subscriberId) {
    return repository.deleteAllBySubscriberId(subscriberId);
  }

  @Nonnull
  @Override
  public Mono<Long> unsubscribe(@Nonnull String topic) {
    return repository.deleteAllByTopic(topic);
  }

  @Nonnull
  @Override
  public Mono<Long> unsubscribe(long subscriberId, @Nonnull String topic) {
    return repository.deleteBySubscriberIdAndTopic(subscriberId, topic);
  }

  @Nonnull
  @Override
  public Mono<List<Subscription>> findAll() {
    return repository.findAll()
        .map(SubscriptionMongoDoConverter::toSubscription)
        .collectList();
  }

  @Nonnull
  @Override
  public Mono<List<Subscription>> findAllByTopic(@Nonnull String topic) {
    return repository.findAllByTopic(topic)
        .map(SubscriptionMongoDoConverter::toSubscription)
        .collectList();
  }

  @Nonnull
  @Override
  public Mono<List<Subscription>> findAllBySubscriber(long subscriberId) {
    return repository.findAllBySubscriberId(subscriberId)
        .map(SubscriptionMongoDoConverter::toSubscription)
        .collectList();
  }

  @Nonnull
  @Override
  public Mono<Boolean> existByTopic(@Nonnull String topic) {
    return repository.findFirstByTopic(topic)
        .map(s -> true).defaultIfEmpty(false);
  }

  @Nonnull
  @Override
  public Mono<Boolean> existBySubscriber(long subscriberId) {
    return repository.findFirstBySubscriberId(subscriberId)
        .map(s -> true).defaultIfEmpty(false);
  }
}
