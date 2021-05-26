package com.zzsong.bus.common.transfer;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/24
 */
@Getter
@Setter
public class ChannelArgs {
  /** 忙碌 */
  public static final int STATUS_BUSY = 0;
  /** 空闲 */
  public static final int STATUS_IDLE = 1;

  /** 应用名称 */
  private long applicationId;

  /** 通道实例id */
  @Nonnull
  private String instanceId;

  /** 通道状态 */
  private int status;
}
