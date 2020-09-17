package com.zzsong.bus.abs.mongo;

import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.abs.mongo.converter.SubscriptionDoConverter;
import com.zzsong.bus.abs.mongo.document.SubscriptionDo;
import com.zzsong.bus.abs.mongo.repository.MongoSubscriptionRepository;
import com.zzsong.bus.abs.storage.SubscriptionStorage;
import com.zzsong.bus.abs.generator.IDGenerator;
import com.zzsong.bus.abs.generator.IDGeneratorFactory;
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
    long subscriberId = subscription.getSubscriberId();
    String topic = subscription.getTopic();
    SubscriptionDo mongoDo = SubscriptionDoConverter.fromSubscription(subscription);
    return repository.findFirstBySubscriberIdAndTopic(subscriberId, topic)
        .map(SubscriptionDo::getSubscriptionId)
        .defaultIfEmpty(-1L)
        .flatMap(id -> {
          if (id > 0) {
            mongoDo.setSubscriptionId(id);
          } else {
            mongoDo.setSubscriptionId(idGenerator.generate());
          }
          return repository.save(mongoDo)
              .map(SubscriptionDoConverter::toSubscription);
        });
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
  public Mono<List<Subscription>> findAllBySubscriber(long subscriberId) {
    return repository.findAllBySubscriberId(subscriberId)
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
  public Mono<Boolean> existBySubscriber(long subscriberId) {
    return repository.findFirstBySubscriberId(subscriberId)
        .map(s -> true).defaultIfEmpty(false);
  }
}
