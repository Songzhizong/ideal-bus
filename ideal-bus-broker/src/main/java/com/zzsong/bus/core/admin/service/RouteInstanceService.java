package com.zzsong.bus.core.admin.service;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.storage.RouteInstanceStorage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Service
public class RouteInstanceService {
  @Nonnull
  private final RouteInstanceStorage storage;

  public RouteInstanceService(@Nonnull RouteInstanceStorage storage) {
    this.storage = storage;
  }

  @Nonnull
  public Mono<RouteInstance> save(@Nonnull RouteInstance routeInstance) {
    return storage.save(routeInstance);
  }

  @Nonnull
  public Mono<List<RouteInstance>> saveAll(@Nonnull Collection<RouteInstance> routeInstances) {
    return storage.saveAll(routeInstances);
  }

  /**
   * 从存储库读取延迟时间已到期的消息
   *
   * @param maxNextTime 延迟时间
   * @param count       加载数量
   * @param nodeId      当前节点id
   * @return 路由实例
   */
  @Nonnull
  public Mono<List<RouteInstance>> loadDelayed(long maxNextTime, int count, int nodeId) {
    return storage.loadDelayed(maxNextTime, count, nodeId);
  }

  /**
   * 从存储库中读取等待消费的消息
   *
   * @param count          读取实例
   * @param nodeId         当前节点id
   * @param subscriptionId 订阅关系
   * @return 路由实例
   */
  @Nonnull
  public Mono<List<RouteInstance>> loadWaiting(int count, int nodeId, long subscriptionId) {
    return storage.loadWaiting(count, nodeId, subscriptionId);
  }

  @Nonnull
  public Mono<Long> updateStatus(long instanceId, int status, @Nonnull String message) {
    return storage.updateStatus(instanceId, status, message);
  }

  public Mono<Long> deleteAllSucceedByCreateTimeLessThan(long time) {
    return storage.deleteAllSucceedByCreateTimeLessThan(time);
  }
}
