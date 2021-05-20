package com.zzsong.bus.broker.core.transfer;

import com.zzsong.bus.abs.domain.RouteInstance;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/11/26
 */
@Slf4j
@Deprecated
public class TransientRouteTransfer implements RouteTransfer {

  public TransientRouteTransfer() {
    RouteTransferFactory.register(TransferType.TRANSIENT, this);
  }

  @Nonnull
  @Override
  public Mono<Boolean> submit(@Nonnull RouteInstance routeInstance) {
    return null;
  }
}
