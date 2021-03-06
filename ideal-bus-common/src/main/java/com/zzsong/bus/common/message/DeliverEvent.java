package com.zzsong.bus.common.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 交付到订阅者的事件信息
 *
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliverEvent {
  private long routeInstanceId;
  private long subscriptionId;
  /**
   * 事件唯一id
   */
  private long eventId;

  /** 唯一编号 */
  private String uuid;

  /** 实体类型 */
  private String entity;

  /** 聚合id */
  private String aggregate;

  /**
   * 业务方唯一id
   */
  @Nullable
  private String transactionId = "";
  /**
   * 事件主题
   */
  @Nonnull
  private String topic;
  /**
   * 消息头
   */
  @Nonnull
  private EventHeaders headers;
  /**
   * 消息内容
   */
  @Nonnull
  private String payload;
  /**
   * 事件产生时间戳
   */
  private long timestamp;
  /**
   * 标记该事件应交由指定的监听器进行处理
   * <p>如果为null或者空则代表应交由所有的监听器进行处理</p>
   */
  @Nullable
  private String listener;
}
