package com.zzsong.bus.abs.transfer;

import com.zzsong.bus.abs.constants.DBDefaults;
import com.zzsong.bus.abs.constants.ApplicationType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
public class CreateApplicationArgs {

  /**
   * 订阅者名称
   */
  @Nonnull
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
  private ApplicationType applicationType = ApplicationType.INTERNAL;

  /**
   * 应用名称, 内部用
   */
  @Nonnull
  private String appName = DBDefaults.STRING_VALUE;

  /**
   * 应用编码, 外部应用拥有此属性
   */
  @Nonnull
  private String externalApp = DBDefaults.STRING_VALUE;

  /**
   * 接收推送的地址, 外部应用拥有此属性
   */
  @Nonnull
  private String receiveUrl = DBDefaults.STRING_VALUE;

  public CreateApplicationArgs checkAndGet() {
    if (StringUtils.isBlank(title)) {
      throw new IllegalArgumentException("订阅者名称不能为空");
    }
    return this;
  }
}
