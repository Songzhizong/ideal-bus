package com.zzsong.bus.common.share.lb;


import com.zzsong.bus.common.share.lb.strategy.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author 宋志宗 on 2020/8/19
 */
public interface LoadBalancer<Server extends LbServer> {
  /**
   * 选取一个server
   *
   * @param key     负载均衡器可以使用该对象来确定返回哪个服务
   * @param servers 服务列表
   * @return 选取的服务
   * @author 宋志宗 on 2020/8/28 11:19 上午
   */
  @Nullable
  Server chooseServer(@Nullable Object key,
                      @Nonnull List<Server> servers);


  @SuppressWarnings("DuplicateBranchesInSwitch")
  @Nonnull
  static <Server extends LbServer> LoadBalancer<Server> newLoadBalancer(@Nonnull LbStrategyEnum strategy) {
    switch (strategy) {
      case CONSISTENT_HASH: {
        return new ConsistentHashLoadBalancer<>();
      }
      case LFU: {
        return new LFULoadBalancer<>();
      }
      case LRU: {
        return new LRULoadBalancer<>();
      }
      case ROUND_ROBIN: {
        return new RoundRobinLoadBalancer<>();
      }
      case RANDOM: {
        return new RandomLoadBalancer<>();
      }
      case WEIGHT_ROUND_ROBIN: {
        return new WeightRoundRobinLoadBalancer<>();
      }
      case WEIGHT_RANDOM: {
        return new WeightRandomLoadBalancer<>();
      }
      default: {
        return new RoundRobinLoadBalancer<>();
      }
    }
  }
}
