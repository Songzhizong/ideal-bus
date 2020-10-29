package com.zzsong.bus.core.processor.pusher;

import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.abs.share.VisibleException;
import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import com.zzsong.bus.core.processor.LocalCache;
import com.zzsong.bus.common.share.utils.ReactorUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * @author 宋志宗 on 2020/9/20 11:48 上午
 */
@Component
public class ExternalDelivererChannel implements DelivererChannel {
  /** 2分钟超时时间 */
  private final WebClient webClient = ReactorUtils.createWebClient(Duration.ofMinutes(2));

  @Nonnull
  private final LocalCache localCache;

  public ExternalDelivererChannel(@Nonnull LocalCache localCache) {
    this.localCache = localCache;
  }

  @Nonnull
  @Override
  public Mono<DeliveredResult> deliver(@Nonnull DeliveredEvent event) {
    long subscriptionId = event.getSubscriptionId();
    SubscriptionDetails subscription = localCache.getSubscription(subscriptionId);
    if (subscription == null) {
      return Mono.error(new VisibleException("订阅关系不存在"));
    }
    String receiveUrl = subscription.getReceiveUrl();
    return webClient.post().uri(receiveUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(event))
        .retrieve()
        .bodyToMono(DeliveredResult.class)
        .onErrorResume(throwable -> {
          DeliveredResult deliveredResult = new DeliveredResult();
          deliveredResult.setEventId(event.getEventId());
          deliveredResult.setSuccess(false);
          return Mono.just(deliveredResult);
        });
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return "";
  }

  @Override
  public boolean heartbeat() {
    return true;
  }
}
