package com.zzsong.bus.core.admin.service;

import com.zzsong.bus.abs.converter.SubscriptionConverter;
import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.abs.storage.SubscriptionStorage;
import com.zzsong.bus.abs.transfer.SubscribeArgs;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Service
public class SubscriptionService {
  @Nonnull
  private final SubscriptionStorage storage;

  public SubscriptionService(@Nonnull SubscriptionStorage storage) {
    this.storage = storage;
  }


  @Nonnull
  Mono<Boolean> existByTopic(@Nonnull String topic) {
    return storage.existByTopic(topic);
  }

  @Nonnull
  Mono<Boolean> existBySubscriber(long subscriberId) {
    return storage.existBySubscriber(subscriberId);
  }

  @Nonnull
  public Mono<Subscription> subscribe(@Nonnull SubscribeArgs args) {
    Subscription subscription = SubscriptionConverter.fromSubscribeArgs(args);
    return storage.save(subscription);
  }

  @Nonnull
  public Mono<Long> unsubscribe(long subscriberId, @Nonnull String topic) {
    return storage.unsubscribe(subscriberId, topic);
  }

  @Nonnull
  public Mono<Long> unsubscribe(@Nonnull String topic) {
    return storage.unsubscribe(topic);
  }

  @Nonnull
  public Mono<Long> unsubscribe(long subscriberId) {
    return storage.unsubscribe(subscriberId);
  }

  @Nonnull
  public Mono<List<Subscription>> getSubscription(long subscriberId) {
    return storage.findAllBySubscriber(subscriberId);
  }

  @Nonnull
  public Mono<List<Subscription>> findAllEnabled() {
    return storage.findAllEnabled();
  }
}
