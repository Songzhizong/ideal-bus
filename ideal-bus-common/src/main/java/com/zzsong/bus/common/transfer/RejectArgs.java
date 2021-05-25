package com.zzsong.bus.common.transfer;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 宋志宗 on 2021/5/25
 */
@Getter
@Setter
public class RejectArgs {
  private long routeInstanceId;
  private String message;
}
