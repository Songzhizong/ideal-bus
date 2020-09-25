package com.zzsong.bus.common.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2020/9/18 10:58 下午
 */
@Data
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

  public SubscriptionArgs checkAndGet() {
    if (StringUtils.isBlank(topic)) {
      throw new IllegalArgumentException("topic不能为空");
    }
    return this;
  }
}
