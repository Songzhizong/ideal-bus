package com.zzsong.bus.receiver.deliver;

import lombok.Getter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
public class EventContext<T> {
  /**
   * 是否ack
   */
  @Getter
  private transient boolean ack = false;
  /**
   * 事件主体
   */
  @Getter
  @Nonnull
  private final T payload;

  @Getter
  @Nonnull
  private transient final String listenerName;

  public EventContext(@Nonnull T payload,
                      @Nonnull String listenerName) {
    this.payload = payload;
    this.listenerName = listenerName;
  }

  public void ack() {
    this.ack = true;
  }
}
