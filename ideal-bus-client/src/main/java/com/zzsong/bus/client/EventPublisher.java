package com.zzsong.bus.client;

import com.zzsong.bus.common.message.EventMessage;
import com.zzsong.bus.common.message.PublishResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author 宋志宗 on 2020/9/17
 */
public interface EventPublisher {
  /**
   * 单条发布
   */
  @Nonnull
  Mono<PublishResult> publish(@Nonnull EventMessage<?> message);

  /**
   * 批量发布
   */
  @Nonnull
  Flux<PublishResult> publish(@Nonnull Collection<EventMessage<?>> messages);

  /**
   * 批量发布
   */
  @Nonnull
  Flux<PublishResult> publish(@Nonnull Flux<EventMessage<?>> messages);
}
