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
   * 消费模式: 并行
   */
  public static final int CONSUME_TYPE_PARALLEL = 0;
  /**
   * 消费模式: 串行
   */
  public static final int CONSUME_TYPE_SERIAL = 1;
  /**
   * 唯一ID
   */
  private Long subscriptionId;
  /**
   * 订阅者id
   */
  private long applicationId;
  /**
   * 事件主题, 也是事件的唯一id
   */
  @Nonnull
  private String topic;
  /**
   * 监听器名称
   */
  @Nonnull
  private String listenerName;
  /**
   * 延迟表达式
   */
  @Nonnull
  private String delayExp = DBDefaults.STRING_VALUE;
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
   * 失败重试次数, 默认重试3次
   */
  private int retryCount = 3;
  /**
   * 消费模式, 默认并行
   */
  private int consumeType = CONSUME_TYPE_PARALLEL;
  /**
   * 订阅状态
   */
  private int status = STATUS_ENABLED;
}
