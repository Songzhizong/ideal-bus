package com.zzsong.bus.abs.storage;

import com.zzsong.bus.abs.domain.RouteInstance;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/17
 */
public interface RouteInstanceStorage {

  @Nonnull
  Mono<RouteInstance> save(@Nonnull RouteInstance routeInstance);

  @Nonnull
  Mono<List<RouteInstance>> saveAll(@Nonnull Collection<RouteInstance> routeInstances);

  @Nonnull
  Mono<List<RouteInstance>> loadDelayed(long maxNextTime, int count, int shard);

  /**
   * 获取所有尚未投递给消费者个消息, 跑个排队和暂存状态
   *
   * @param count          数量
   * @param shard          分片id
   * @param subscriptionId 队列id, 也就是订阅关系id
   * @return 消息列表
   * @author 宋志宗 on 2021/5/18
   */
  @Nonnull
  Mono<List<RouteInstance>> loadWaiting(int count, int shard, long subscriptionId);

  @Nonnull
  Mono<List<RouteInstance>> loadTemping(int count, int shard, long subscriptionId);

  @Nonnull
  Mono<Long> updateStatus(long instanceId, int status, @Nonnull String message);

  @Nonnull
  Mono<Long> updateStatus(Collection<Long> instanceIdList, int status, @Nonnull String message);

  /**
   * 删除创建时间小于或等于指定时间戳的成功推送数据
   *
   * @param time 最小时间
   * @return 删除数量
   * @author 宋志宗 on 2020/11/23
   */
  @Nonnull
  Mono<Long> deleteAllSucceedByCreateTimeLessThan(long time);
}
