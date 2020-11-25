package com.zzsong.bus.broker.cluster;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.broker.constants.DeliverResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/11/24
 */
@Component
public class ClusterApiImpl implements ClusterApi {


  @Override
  public Mono<DeliverResult> entrustDeliver(@Nonnull RouteInstance routeInstance) {
    return Mono.just(DeliverResult.APP_OFFLINE);
  }
}
