package com.zzsong.bus.core.processor;

import com.zzsong.bus.abs.core.MessageRouter;
import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.pojo.PublishResult;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.abs.storage.EventInstanceStorage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Component
public class MessageRouterImpl implements MessageRouter {
  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final EventInstanceStorage eventInstanceStorage;

  public MessageRouterImpl(@Nonnull LocalCache localCache,
                           @Nonnull EventInstanceStorage eventInstanceStorage) {
    this.localCache = localCache;
    this.eventInstanceStorage = eventInstanceStorage;
  }

  @Override
  public Mono<PublishResult> route(@Nonnull EventInstance instance) {
    return eventInstanceStorage.save(instance)
        .flatMap(ins -> {
          String topic = instance.getTopic();
          List<SubscriptionDetails> subscription = localCache.getTopicSubscription(topic);
          PublishResult.PublishResultBuilder builder = PublishResult.builder()
              .eventId(instance.getEventId())
              .bizId(instance.getBizId())
              .topic(instance.getTopic())
              .success(true);
          if (subscription.isEmpty()) {
            builder.message("该事件没有订阅者").build();
            return Mono.just(builder.build());
          }
          return Mono.just(builder.build());
        });
  }
}
