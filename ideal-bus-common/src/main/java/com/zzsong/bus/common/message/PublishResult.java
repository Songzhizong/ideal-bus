package com.zzsong.bus.common.message;

import lombok.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishResult {
  /** 事件id */
  private long eventId;

  /** 业务方事务id */
  @Nullable
  private String transactionId;

  /** 主题 */
  @Nonnull
  private String topic;

  /** 执行结果 */
  private boolean success;

  /** 描述信息 */
  @Nonnull
  private String message;
}
