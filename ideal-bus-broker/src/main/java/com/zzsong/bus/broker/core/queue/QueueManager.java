package com.zzsong.bus.broker.core.queue;

import com.zzsong.bus.abs.domain.RouteInstance;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 队列管理器接口
 *
 * @author 宋志宗 on 2021/5/14
 */
public interface QueueManager {

  /**
   * 将消息提交到队列管理器
   *
   * @param routeInstances 消息
   * @return 结果
   * @author 宋志宗 on 2021/5/19
   */
  Mono<Boolean> submit(@Nonnull List<RouteInstance> routeInstances);

  /**
   * 签收消息
   *
   * @param routeInstanceId 消息id
   * @author 宋志宗 on 2021/5/26
   */
  Mono<Boolean> ack(long routeInstanceId);

  /**
   * 拒绝消息
   *
   * @param routeInstanceId 消息id
   * @param message         描述信息
   * @author 宋志宗 on 2021/5/26
   */
  Mono<Boolean> reject(long routeInstanceId, @Nullable String message);
}
