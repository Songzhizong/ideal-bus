package com.zzsong.bus.abs.domain;

import com.zzsong.bus.abs.constants.DBDefaults;
import com.zzsong.bus.abs.constants.ApplicationTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
public class Application {
  /** 应用ID */
  @Nonnull
  private Long applicationId;

  /** 应用名称 */
  @Nonnull
  private String title = DBDefaults.STRING_VALUE;

  /** 订阅者描述 */
  @Nonnull
  private String desc = DBDefaults.STRING_VALUE;

  @Nonnull
  private String accessToken = DBDefaults.STRING_VALUE;

  /** 订阅者类型 */
  @Nonnull
  private ApplicationTypeEnum applicationType = ApplicationTypeEnum.INTERNAL;

  /** 应用名称, 内部用 */
  @Nonnull
  private String appName = DBDefaults.STRING_VALUE;

  /** 应用编码, 外部应用拥有此属性 */
  @Nonnull
  private String externalApplication = DBDefaults.STRING_VALUE;

  /** 接收推送的地址, 外部应用拥有此属性 */
  @Nonnull
  private String receiveUrl = DBDefaults.STRING_VALUE;
}
