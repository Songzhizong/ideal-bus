package com.zzsong.bus.client.impl;

import com.zzsong.bus.client.EventPublisher;
import com.zzsong.bus.common.message.EventMessage;
import com.zzsong.bus.common.message.ExchangeResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author 宋志宗 on 2021/5/24
 */
public class NoneEventPublisher implements EventPublisher {
  private static final ExchangeResult result = ExchangeResult
      .builder().success(true).message("NoneEventPublisher").build();

  @Nonnull
  @Override
  public Mono<ExchangeResult> publish(@Nonnull EventMessage<?> message) {
    return Mono.just(result);
  }

  @Nonnull
  @Override
  public Mono<ExchangeResult> publish(@Nonnull Collection<EventMessage<?>> messages) {
    return Mono.just(result);
  }

  @Nonnull
  @Override
  public Mono<ExchangeResult> publish(@Nonnull Flux<EventMessage<?>> messages) {
    return Mono.just(result);
  }
}
