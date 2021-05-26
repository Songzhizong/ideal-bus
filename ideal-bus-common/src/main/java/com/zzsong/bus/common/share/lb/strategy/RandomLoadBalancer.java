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
 * 随机策略
 *
 * @author 宋志宗 on 2020/8/19
 */
public class RandomLoadBalancer<Server extends LbServer> implements LoadBalancer<Server> {
  private final Random defaultRandom = new Random(System.currentTimeMillis());
  private final Map<Object, Random> randomMap = new ConcurrentHashMap<>();

  @Override
  @Nullable
  public Server chooseServer(@Nullable Object key,
                             @Nonnull List<Server> servers) {
    if (servers.isEmpty()) {
      return null;
    }
    int size = servers.size();
    if (size == 1) {
      return servers.get(0);
    }
    Random random = defaultRandom;
    if (key != null) {
      random = randomMap.computeIfAbsent(key, k -> new Random(System.currentTimeMillis()));
    }
    int nextInt = random.nextInt(size);
    return servers.get(nextInt);
  }
}
