package com.zzsong.bus.common.message;

import com.zzsong.common.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2020/9/20 12:13 上午
 */
@Getter
@Setter
public class LoginMessage {
  /**
   * 应用ID
   */
  private long applicationId;
  /**
   * accessToken
   */
  @Nullable
  private String accessToken;
  /**
   * 客户端唯一标识
   */
  @Nonnull
  private String instanceId;

  @Nonnull
  public String toMessageString() {
    return JsonUtils.toJsonString(this);
  }

  public static LoginMessage parseMessage(@Nonnull String message) {
    return JsonUtils.parseJson(message, LoginMessage.class);
  }
}