package com.zzsong.bus.broker.core.transfer;

import com.zzsong.bus.abs.domain.RouteInstance;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/11/26
 */
@Deprecated
public interface RouteTransfer {
  @Nonnull
  Mono<Boolean> submit(@Nonnull RouteInstance routeInstance);
}
