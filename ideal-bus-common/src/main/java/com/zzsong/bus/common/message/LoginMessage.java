package com.zzsong.bus.common.message;

import com.zzsong.bus.common.share.utils.JsonUtils;
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
  public static final int SOCKET_TYPE_SEND = 0;
  public static final int SOCKET_TYPE_RECEIVE = 1;
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

  private int socketType;

  @Nonnull
  public String toMessageString() {
    return JsonUtils.toJsonString(this);
  }

  public static LoginMessage parseMessage(@Nonnull String message) {
    return JsonUtils.parseJson(message, LoginMessage.class);
  }
}
