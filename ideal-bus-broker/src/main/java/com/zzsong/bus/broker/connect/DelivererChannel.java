package com.zzsong.bus.broker.connect;

import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.message.DeliverResult;
import com.zzsong.bus.common.share.loadbalancer.LbServer;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/20 1:09 上午
 */
@Deprecated
public interface DelivererChannel extends LbServer {

  @Nonnull
  Mono<DeliverResult> deliver(@Nonnull DeliverEvent event);
}
