package com.zzsong.bus.common.transfer;

import lombok.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2020/9/18 10:58 下午
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionArgs {
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
  @Nullable
  private String delayExp;
  /**
   * 订阅条件表达式
   * <pre>
   *
   * </pre>
   */
  @Nullable
  private String condition;
  /**
   * 是否广播
   */
  @Nullable
  private Boolean broadcast;
  /**
   * 失败重试次数
   */
  @Nullable
  private Integer retryCount;

  public void checkAndGet() {
    if (StringUtils.isBlank(topic)) {
      throw new IllegalArgumentException("topic不能为空");
    }
  }
}
