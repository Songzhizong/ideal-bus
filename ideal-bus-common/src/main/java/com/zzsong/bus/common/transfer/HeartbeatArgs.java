package com.zzsong.bus.common.transfer;

import lombok.*;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2021/6/2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeartbeatArgs {

  /** 应用名称 */
  private long applicationId;

  /** 通道实例id */
  @Nonnull
  private String instanceId;
}
