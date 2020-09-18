package com.zzsong.bus.common.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

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
  @NotBlank(message = "topic不能为空")
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
}
