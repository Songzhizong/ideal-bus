package com.zzsong.bus.receiver.deliver;

import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
public interface EventDeliverer {

  @Nonnull
  Mono<DeliveredResult> deliver(@Nonnull DeliveredEvent event);
}
