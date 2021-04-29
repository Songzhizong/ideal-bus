package com.zzsong.bus.client.rsocket;

import com.zzsong.bus.common.message.EventMessage;
import com.zzsong.bus.common.message.PublishResult;
import com.zzsong.bus.common.share.loadbalancer.LbServer;
import com.zzsong.bus.common.transfer.ResubscribeArgs;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2021/4/28
 */
public interface SendRSocketChannel extends RSocketChannel, LbServer {

  Mono<PublishResult> publishEvent(@Nonnull EventMessage<?> message);

  Mono<Boolean> resubscribe(@Nonnull ResubscribeArgs resubscribeArgs);
}
