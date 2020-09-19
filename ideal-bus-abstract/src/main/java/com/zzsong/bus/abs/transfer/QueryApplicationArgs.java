package com.zzsong.bus.abs.transfer;

import com.zzsong.bus.abs.constants.ApplicationTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
public class QueryApplicationArgs {
  /**
   * 订阅者名称
   */
  @Nullable
  private String title;
  /**
   * 订阅者类型
   */
  @Nullable
  private ApplicationTypeEnum applicationType;
  /**
   * 外部应用ID, 外部应用拥有此属性
   */
  @Nullable
  private String externalId;
}
