package com.zzsong.bus.broker.connect;

import com.zzsong.bus.abs.domain.RouteInstance;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/11/28
 */
public interface PreDeliverHandler {

  @Nonnull
  Mono<Boolean> doOnPreDeliver(@Nonnull RouteInstance routeInstance);
}
