package com.zzsong.bus.client.deliver;

import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.message.DeliverResult;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
public interface EventDeliverer {

  @Nonnull
  Mono<DeliverResult> deliver(@Nonnull DeliverEvent event);
}
