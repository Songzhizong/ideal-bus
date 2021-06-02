package com.zzsong.bus.common.constants;

/**
 * @author 宋志宗 on 2020/9/19 10:40 下午
 */
public interface RSocketRoute {
  /**
   * 客户端登录
   */
  String LOGIN = "login";
  /**
   * 通知客户端断开连接
   */
  String INTERRUPT = "interrupt";
  /**
   * 连接成功
   */
  String CONNECTED = "connected";
  /**
   * 心跳
   */
  String HEARTBEAT = "heartbeat";
  /**
   * channel状态变更
   */
  String CHANNEL_CHANGE_STATUS = "channel-change-status";
  /**
   * 向客户端推送消息
   */
  String MESSAGE_DELIVER = "message-deliver";
  /**
   * 签收消息
   */
  String MESSAGE_ACK = "message-ack";
  /**
   * 拒绝消息
   */
  String MESSAGE_REJECT = "message-reject";
}
