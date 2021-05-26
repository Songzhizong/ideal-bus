package com.zzsong.bus.common.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliverResult {

  private long eventId;

  private Status status;

  @Nonnull
  private String message = "";

  public enum Status {
    /** 成功 */
    SUCCESS,

    /** 忙碌 */
    BUSY,
  }
}
