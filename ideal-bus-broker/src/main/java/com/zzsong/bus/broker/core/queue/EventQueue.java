package com.zzsong.bus.broker.core.queue;

import com.zzsong.bus.abs.domain.RouteInstance;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2021/5/14
 */
public interface EventQueue {

  boolean test();

  void offer(@Nonnull RouteInstance routeInstance);

  /**
   * 销毁队列
   */
  void destroy();
}
