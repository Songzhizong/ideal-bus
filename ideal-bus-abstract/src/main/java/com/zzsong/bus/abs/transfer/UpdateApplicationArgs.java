package com.zzsong.bus.abs.transfer;

import com.zzsong.bus.abs.constants.DBDefaults;
import com.zzsong.bus.abs.constants.ApplicationTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
public class UpdateApplicationArgs {
  /**
   * 订阅者id
   */
  @Nonnull
  @NotNull(message = "applicationId不能为空")
  private Long applicationId;
  /**
   * 订阅者名称
   */
  @Nonnull
  @NotBlank(message = "订阅者名称不能为空")
  private String title;

  /**
   * 订阅者描述
   */
  @Nonnull
  private String desc = DBDefaults.STRING_VALUE;

  @Nonnull
  private String accessToken = DBDefaults.STRING_VALUE;

  /**
   * 订阅者类型
   */
  @Nonnull
  private ApplicationTypeEnum applicationType = ApplicationTypeEnum.INTERNAL;

  /**
   * 应用名称, 内部用
   */
  @Nonnull
  private String appName = DBDefaults.STRING_VALUE;

  /**
   * 应用编码, 外部应用拥有此属性
   */
  @Nonnull
  private String externalId = DBDefaults.STRING_VALUE;

  /**
   * 接收推送的地址, 外部应用拥有此属性
   */
  @Nonnull
  private String receiveUrl = DBDefaults.STRING_VALUE;
}
