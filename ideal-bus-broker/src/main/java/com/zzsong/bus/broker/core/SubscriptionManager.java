package com.zzsong.bus.broker.core;

import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.common.message.EventHeaders;
import com.zzsong.bus.common.util.ConditionMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 订阅管理器
 *
 * @author 宋志宗 on 2020/11/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionManager {

  @Nonnull
  private final LocalCache localCache;

  public Mono<List<SubscriptionDetails>> getSubscriptions(@Nonnull EventInstance event) {
    // 保存事件实例
    String topic = event.getTopic();
    List<SubscriptionDetails> subscriptions = localCache.getTopicSubscription(topic);
    if (subscriptions.isEmpty()) {
      return Mono.just(Collections.emptyList());
    }
    EventHeaders headers = event.getHeaders();
    List<SubscriptionDetails> collect = subscriptions.stream().filter(details -> {
      List<Set<String>> group = details.getConditionGroup();
      boolean match = ConditionMatcher.match(group, headers);
      if (!match && log.isDebugEnabled()) {
        log.debug("event: {} subscription: {} 订阅条件不匹配: {}",
            event.getEventId(), details.getSubscriptionId(), details.getCondition());
      }
      return match;
    }).collect(Collectors.toList());
    return Mono.just(collect);
  }
}
