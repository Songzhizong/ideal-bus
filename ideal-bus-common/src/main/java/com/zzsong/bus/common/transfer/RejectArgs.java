package com.zzsong.bus.common.transfer;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2021/5/25
 */
@Getter
@Setter
public class RejectArgs {
  private long routeInstanceId;
  @Nullable
  private String message;
}
