package com.zzsong.bus.broker.core.transfer;

/**
 * 传输类型
 *
 * @author 宋志宗 on 2020/11/26
 */
public enum TransferType {
  /**
   * 持久化
   * <p>消息持久化, 如果客户端不在线则进行缓存</p>
   */
  PERSISTENCE,
  /**
   * 瞬态传输
   * <p>直接将消息交付给客户端, 如果客户端不在线则丢弃</p>
   */
  TRANSIENT,
  ;
}
