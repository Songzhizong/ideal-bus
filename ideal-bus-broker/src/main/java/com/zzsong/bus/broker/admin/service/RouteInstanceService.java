package com.zzsong.bus.broker.admin.service;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.storage.RouteInstanceStorage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
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

  /**
   * 从存储库读取延迟时间已到期的消息
   *
   * @param maxNextTime 延迟时间
   * @param count       加载数量
   * @param shard       分片id
   * @return 路由实例
   */
  @Nonnull
  public Mono<List<RouteInstance>> loadDelayed(long maxNextTime, int count, int shard) {
    return storage.loadDelayed(maxNextTime, count, shard);
  }

  public Mono<Long> deleteAllSucceedByCreateTimeLessThan(long time) {
    return storage.deleteAllSucceedByCreateTimeLessThan(time);
  }
}
