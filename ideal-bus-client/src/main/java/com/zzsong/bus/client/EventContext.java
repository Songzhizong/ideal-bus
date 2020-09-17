package com.zzsong.bus.client;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
public class EventContext<T> {
  /**
   * 是否ack
   * <p>使用包装类是为了判断开发者是否手动设置了ack, 最终肯定是不为null的</p>
   */
  @Nonnull
  @SuppressWarnings("ConstantConditions")
  private Boolean ack = null;
  /**
   * 事件主体
   */
  private T payload;

  @Nonnull
  private final String listenerName;

  public EventContext(@Nonnull String listenerName) {
    this.listenerName = listenerName;
  }
}
