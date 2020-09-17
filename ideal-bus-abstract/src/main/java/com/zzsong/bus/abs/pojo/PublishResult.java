package com.zzsong.bus.abs.pojo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@Builder
public class PublishResult {
  @Nonnull
  private String eventId;
  @Nullable
  private String bizId;
  @Nonnull
  private String topic;
  private boolean success;
  @Nonnull
  private String message;
}
