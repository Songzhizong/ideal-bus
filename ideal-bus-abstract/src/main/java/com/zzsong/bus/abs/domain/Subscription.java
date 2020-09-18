package com.zzsong.bus.abs.domain;

import com.zzsong.bus.abs.constants.DBDefaults;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
public class Subscription {
  public static final int STATUS_DISABLED = 0;
  public static final int STATUS_ENABLED = 1;
  /**
   * 唯一ID
   */
  private Long subscriptionId;
  /**
   * 订阅者id
   */
  private long subscriberId;
  /**
   * 事件主题, 也是事件的唯一id
   */
  @Nonnull
  private String topic;
  /**
   * 订阅条件表达式
   */
  @Nonnull
  private String condition = DBDefaults.STRING_VALUE;
  /**
   * 是否广播
   */
  private boolean broadcast = false;
  /**
   * 失败重试次数
   */
  private int retryCount = 0;
  /**
   * 订阅状态
   */
  private int status = STATUS_ENABLED;
}
