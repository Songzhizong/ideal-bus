package com.zzsong.bus.client;

import com.zzsong.bus.common.message.EventMessage;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author 宋志宗 on 2020/12/19
 */
public interface TransactionalEventPublisher {
  /**
   * 单条发布
   */
  void publish(@Nonnull EventMessage<?> message);

  /**
   * 批量发布
   */
  void publish(@Nonnull Collection<EventMessage<?>> messages);

  /**
   * 批量发布
   */
  void publish(@Nonnull Flux<EventMessage<?>> messages);
}
