package com.zzsong.bus.client;

import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.message.DeliverResult;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2021/4/29
 */
public interface EventConsumer {

  Mono<DeliverResult> onMessage(@Nonnull DeliverEvent event, @Nonnull Channel channel);
}
