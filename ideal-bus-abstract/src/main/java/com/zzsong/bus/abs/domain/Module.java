package com.zzsong.bus.abs.domain;

import com.zzsong.bus.abs.constants.DBDefaults;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
public class Module {
  /**
   * 订阅者id
   */
  @Nonnull
  private Long applicationId;

  /**
   * 模块名称
   */
  @Nonnull
  private String moduleName = DBDefaults.STRING_VALUE;

  /**
   * 订阅者描述
   */
  @Nonnull
  private String desc = DBDefaults.STRING_VALUE;

}
