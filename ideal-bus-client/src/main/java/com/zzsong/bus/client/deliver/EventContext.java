package com.zzsong.bus.client.deliver;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
public class EventContext<T> {
  /**
   * 是否ack
   */
  @Getter
  private boolean ack = false;
  /**
   * 执行信息
   */
  @Getter
  @Setter
  @Nonnull
  private String message = "";
  /**
   * 事件主体
   */
  @Getter
  @Nonnull
  private final T payload;

  @Getter
  @Nonnull
  private final String listenerName;

  public EventContext(@Nonnull T payload,
                      @Nonnull String listenerName) {
    this.payload = payload;
    this.listenerName = listenerName;
  }

  public void ack() {
    this.ack = true;
  }

  public void reject(@Nonnull String message) {
    this.ack = false;
    this.message = message;
  }
}
