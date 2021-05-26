package com.zzsong.bus.broker.core.consumer;

/**
 * @author 宋志宗 on 2021/5/18
 */
public enum DeliverStatus {
  /**
   * 消费成功
   */
  SUCCESS,
  /**
   * 当前通道处在忙碌状态, 可尝试其他通道
   */
  CHANNEL_BUSY,
  /**
   * 整个消费者处在不可达状态, 暂时无法接收消息
   */
  UNREACHABLE,
  /**
   * 应用不在线
   */
  OFFLINE,
}
