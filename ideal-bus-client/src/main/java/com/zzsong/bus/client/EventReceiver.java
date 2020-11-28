package com.zzsong.bus.client;

import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.message.DeliverResult;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/18
 */
public interface EventReceiver {

  @Nonnull
  Mono<DeliverResult> receive(@Nonnull DeliverEvent event);
}
