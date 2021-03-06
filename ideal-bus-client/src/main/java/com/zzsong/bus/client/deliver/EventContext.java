package com.zzsong.bus.client.deliver;

import com.zzsong.bus.common.message.EventHeaders;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
public class EventContext<T> {
  /** 是否ack */
  @Setter(AccessLevel.NONE)
  private boolean ack = false;

  /** 执行信息 */
  @Nonnull
  private String message = "";

  /** 事件唯一id */
  private long eventId;

  /** 业务方唯一id */
  @Nullable
  private String transactionId = "";

  /** 消息头 */
  @Nonnull
  private EventHeaders headers;

  /** 事件主体 */
  @Nonnull
  private final T payload;

  /** 事件产生时间戳 */
  private long timestamp;

  public EventContext(@Nonnull T payload) {
    this.payload = payload;
  }

  /**
   * 签收消息
   *
   * @author 宋志宗 on 2020/10/29
   */
  public void ack() {
    this.ack = true;
  }

  /**
   * 拒签消息
   *
   * @param message 描述信息
   * @author 宋志宗 on 2020/10/29
   */
  public void reject(@Nonnull String message) {
    this.ack = false;
    this.message = message;
  }
}