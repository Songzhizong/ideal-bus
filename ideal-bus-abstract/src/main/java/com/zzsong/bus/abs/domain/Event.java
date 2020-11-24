package com.zzsong.bus.abs.domain;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
public class Event {
  public static final long TOTAL_MILLIS_OF_DAY = 86400000L;

  /** 主题, 也是事件的唯一id */
  @Nonnull
  private String topic;

  /** 事件名称 */
  @Nonnull
  private String eventName;

  /** 描述 */
  @Nonnull
  private String desc;
}
