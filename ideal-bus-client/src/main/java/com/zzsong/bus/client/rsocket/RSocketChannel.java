package com.zzsong.bus.client.rsocket;

/**
 * @author 宋志宗 on 2021/4/28
 */
public interface RSocketChannel {

  /**
   * 建立连接
   */
  void connect();

  /**
   * 关闭通道
   */
  void close();
}
