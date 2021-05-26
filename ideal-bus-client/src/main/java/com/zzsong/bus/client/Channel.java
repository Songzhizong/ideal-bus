package com.zzsong.bus.client;

import javax.annotation.Nullable;

/**
 * 消费通道
 *
 * @author 宋志宗 on 2021/5/14
 */
public interface Channel {

  void ack(long routeInstanceId);

  void reject(long routeInstanceId, @Nullable String message);
}
