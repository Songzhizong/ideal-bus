package com.zzsong.bus.core.admin.service;

import com.zzsong.bus.base.storage.SubscriptionStorage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

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
}
