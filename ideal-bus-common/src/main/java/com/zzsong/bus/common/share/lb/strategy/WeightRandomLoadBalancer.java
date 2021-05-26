package com.zzsong.bus.common.share.lb.strategy;

import com.zzsong.bus.common.share.lb.LbServer;
import com.zzsong.bus.common.share.lb.LoadBalancer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加权随机策略
 *
 * @author 宋志宗 on 2020/8/19
 */
public class WeightRandomLoadBalancer<Server extends LbServer> implements LoadBalancer<Server> {
  private final Random defaultRandom = new Random(System.currentTimeMillis());
  private final Map<Object, Random> randomMap = new ConcurrentHashMap<>();

  @Override
  @Nullable
  @SuppressWarnings("DuplicatedCode")
  public Server chooseServer(@Nullable Object key,
                             @Nonnull List<Server> servers) {
    if (servers.isEmpty()) {
      return null;
    }
    final int size = servers.size();
    if (size == 1) {
      return servers.get(0);
    }
    // 为了保障随机均匀, 将权重放大一定的倍数
    final int multiple = 10;
    int sum = 0;
    for (LbServer server : servers) {
      final int weight = server.getWeight();
      if (weight < 1) {
        throw new IllegalArgumentException("Weight least for 1");
      }
      sum += weight * multiple;
    }
    if (sum == 0) {
      return null;
    }
    if (sum < 0) {
      throw new IllegalArgumentException("sun less then 0");
    }
    Random random = defaultRandom;
    if (key != null) {
      random = randomMap.computeIfAbsent(key, k -> new Random(System.currentTimeMillis()));
    }
    int randomValue = random.nextInt(sum) + 1;
    int tmp = 0;
    for (Server server : servers) {
      final int weight = server.getWeight();
      if (weight < 1) {
        throw new IllegalArgumentException("Weight least for 1");
      }
      tmp += weight * multiple;
      if (tmp >= randomValue) {
        return server;
      }
    }
    // 不可能运行到这里的, 如果有那肯定是电脑的问题
    String message = "加权随机算法运算错误: sum=" + sum + ", randomValue=" + randomValue + ", tmp=" + tmp;
    throw new WeightedRandomError(message);
  }

  public static class WeightedRandomError extends Error {
    public WeightedRandomError(String message) {
      super(message);
    }
  }
}
