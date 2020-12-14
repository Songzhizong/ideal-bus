package com.zzsong.bus.abs.share;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2020/8/23
 */
public class VisibleException extends RuntimeException {

  @Nullable
  private ResMsg resMsg = null;

  public VisibleException(@Nonnull String message) {
    super(message);
  }

  public VisibleException(@Nonnull ResMsg resMsg) {
    this(resMsg.message());
    this.resMsg = resMsg;
  }

  public VisibleException(@Nonnull ResMsg resMsg, String message) {
    this(message);
    this.resMsg = resMsg;
  }

  @Nullable
  public ResMsg getResMsg() {
    return resMsg;
  }
}
