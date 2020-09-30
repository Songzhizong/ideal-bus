package com.zzsong.bus.core.processor.pusher;

import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import com.zzsong.bus.common.share.loadbalancer.LbServer;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/20 1:09 上午
 */
public interface DelivererChannel extends LbServer {

  @Nonnull
  Mono<DeliveredResult> deliver(@Nonnull DeliveredEvent event);
}
