package com.zzsong.bus.client;

import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.message.DeliverResult;
import com.zzsong.bus.common.message.EventMessage;
import com.zzsong.bus.common.message.PublishResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author 宋志宗 on 2020/9/27
 */
public class DefaultBusClient implements BusClient {
  @Nonnull
  @Override
  public Mono<PublishResult> publish(@Nonnull EventMessage<?> message) {
    return Mono.empty();
  }

  @Nonnull
  @Override
  public Flux<PublishResult> publish(@Nonnull Collection<EventMessage<?>> messages) {
    return Flux.empty();
  }

  @Nonnull
  @Override
  public Mono<DeliverResult> receive(@Nonnull DeliverEvent event) {
    return Mono.empty();
  }
}
