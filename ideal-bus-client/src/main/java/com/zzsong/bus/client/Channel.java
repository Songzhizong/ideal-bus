package com.zzsong.bus.client;

/**
 * @author 宋志宗 on 2021/5/14
 */
public interface Channel {

  void ack(long routeInstanceId);

  void reject(long routeInstanceId);
}
