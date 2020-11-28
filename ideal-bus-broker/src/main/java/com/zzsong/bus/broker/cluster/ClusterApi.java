package com.zzsong.bus.broker.cluster;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.common.message.DeliverResult;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * 集群模块对外开放接口
 *
 * @author 宋志宗 on 2020/11/24
 */
public interface ClusterApi {
  /**
   * 委托集群执行消息交付
   *
   * @param routeInstance 消息信息
   * @return 交付结果
   * @author 宋志宗 on 2020/11/25
   */
  Mono<DeliverResult> entrustDeliver(@Nonnull RouteInstance routeInstance);
}
