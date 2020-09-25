package com.zzsong.bus.common.message;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/24
 */
@Getter
@Setter
public class ChannelInfo {
  public static final int STATUS_BUSY = 0;
  public static final int STATUS_IDLE = 1;
  @Nonnull
  private String appName;
  @Nonnull
  private String instanceId;
  private int status;
}
