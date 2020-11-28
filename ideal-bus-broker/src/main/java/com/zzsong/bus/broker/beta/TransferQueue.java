package com.zzsong.bus.broker.beta;

import com.zzsong.bus.abs.domain.RouteInstance;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/11/26
 */
public interface TransferQueue {

  /**
   * 投递路由实例
   * <pre>
   *   - 每条订阅关系一个队列组
   *   - 一条订阅关系可以有N个队列实例
   * </pre>
   *
   * @param instance 路由实例
   * @param quantity 队列实例数量
   * @return 是否投递成功
   * @author 宋志宗 on 2020/11/26
   */
  boolean submit(@Nonnull RouteInstance instance, int quantity);
}
