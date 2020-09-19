package com.zzsong.bus.storage.mongo;

import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.storage.mongo.converter.SubscriptionDoConverter;
import com.zzsong.bus.storage.mongo.document.SubscriptionDo;
import com.zzsong.bus.storage.mongo.repository.MongoSubscriptionRepository;
import com.zzsong.bus.abs.storage.SubscriptionStorage;
import com.zzsong.bus.abs.generator.IDGenerator;
import com.zzsong.bus.abs.generator.IDGeneratorFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Component
public class MongoSubscriptionStorage implements SubscriptionStorage {

  @Nonnull
  private final IDGenerator idGenerator;
  @Nonnull
  private final MongoSubscriptionRepository repository;

  public MongoSubscriptionStorage(@Nonnull MongoSubscriptionRepository repository,
                                  @Nonnull IDGeneratorFactory idGeneratorFactory) {
    this.repository = repository;
    this.idGenerator = idGeneratorFactory.getGenerator("subscription");
  }

  @Nonnull
  @Override
  public Mono<Subscription> save(@Nonnull Subscription subscription) {
    if (subscription.getSubscriptionId() == null) {
      subscription.setSubscriptionId(idGenerator.generate());
    }
    SubscriptionDo mongoDo = SubscriptionDoConverter.fromSubscription(subscription);
    return repository.save(mongoDo).map(SubscriptionDoConverter::toSubscription);
  }

  @Nonnull
  @Override
  public Mono<List<Subscription>> saveAll(@Nonnull Collection<Subscription> subscriptions) {
    if (subscriptions.isEmpty()) {
      return Mono.just(Collections.emptyList());
    }
    List<SubscriptionDo> collect = subscriptions.stream().map(subscription -> {
      if (subscription.getSubscriptionId() == null) {
        subscription.setSubscriptionId(idGenerator.generate());
      }
      return SubscriptionDoConverter.fromSubscription(subscription);
    }).collect(Collectors.toList());
    return repository.saveAll(collect)
        .map(SubscriptionDoConverter::toSubscription)
        .collectList()
        .defaultIfEmpty(Collections.emptyList());
  }

  @Nonnull
  @Override
  public Mono<Long> unsubscribe(long subscriptionId) {
    return repository.deleteBySubscriptionId(subscriptionId);
  }

  @Nonnull
  @Override
  public Mono<Long> unsubscribe(long applicationId, @Nonnull String topic) {
    return repository.deleteByApplicationIdAndTopic(applicationId, topic);
  }

  @Nonnull
  @Override
  public Mono<Long> unsubscribeAll(@Nonnull Collection<Long> subscriptionIds) {
    if (subscriptionIds.isEmpty()) {
      return Mono.just(0L);
    }
    return repository.deleteAllBySubscriptionIdIn(subscriptionIds);
  }

  @Nonnull
  @Override
  public Mono<Long> unsubscribeAll(long applicationId) {
    return repository.deleteAllByApplicationId(applicationId);
  }

  @Nonnull
  @Override
  public Mono<Long> unsubscribeAll(@Nonnull String topic) {
    return repository.deleteAllByTopic(topic);
  }

  @Nonnull
  @Override
  public Mono<List<Subscription>> findAll() {
    return repository.findAll()
        .map(SubscriptionDoConverter::toSubscription)
        .collectList();
  }

  @Nonnull
  @Override
  public Mono<List<Subscription>> findAllEnabled() {
    return repository.findAllByStatus(Subscription.STATUS_ENABLED)
        .map(SubscriptionDoConverter::toSubscription)
        .collectList();
  }

  @Nonnull
  @Override
  public Mono<List<Subscription>> findAllByTopic(@Nonnull String topic) {
    return repository.findAllByTopic(topic)
        .map(SubscriptionDoConverter::toSubscription)
        .collectList();
  }

  @Nonnull
  @Override
  public Mono<List<Subscription>> findAllByApplication(long applicationId) {
    return repository.findAllByApplicationId(applicationId)
        .map(SubscriptionDoConverter::toSubscription)
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
  public Mono<Boolean> existByApplication(long applicationId) {
    return repository.findFirstByApplicationId(applicationId)
        .map(s -> true).defaultIfEmpty(false);
  }
}
