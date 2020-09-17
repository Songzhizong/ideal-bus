package com.zzsong.bus.client;

import com.zzsong.bus.common.message.EventMessage;
import com.zzsong.bus.common.transfer.Res;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author 宋志宗 on 2020/9/17
 */
public interface Publisher {
  /**
   * 单条发布
   */
  @Nonnull
  Mono<Res<Void>> publish(@Nonnull EventMessage<?> message);

  /**
   * 批量发布
   */
  @Nonnull
  Flux<Res<Void>> batchPublish(@Nonnull Collection<EventMessage<?>> messages);
}
