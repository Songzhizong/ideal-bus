package com.zzsong.bus.broker.core.channel;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.broker.core.consumer.DeliverStatus;
import com.zzsong.bus.common.share.lb.LbServer;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2021/5/19
 */
public interface Channel extends LbServer {

  @Nonnull
  String getChannelId();

  @Nonnull
  Mono<DeliverStatus> deliverMessage(@Nonnull RouteInstance routeInstance);

  @Nonnull
  @Override
  default String getInstanceId() {
    return getChannelId();
  }
}
