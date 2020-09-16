package com.zzsong.bus.base.domain;

import com.zzsong.bus.common.constant.DBDefaults;
import com.zzsong.bus.common.constant.SubscriberTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
public class Subscriber {
  /**
   * 订阅者id
   */
  @Nonnull
  private Long subscriberId;

  /**
   * 订阅者名称
   */
  @Nonnull
  private String title = DBDefaults.STRING_VALUE;

  /**
   * 订阅者描述
   */
  @Nonnull
  private String desc = DBDefaults.STRING_VALUE;

  /**
   * 订阅者类型
   */
  @Nonnull
  private SubscriberTypeEnum subscriberType = SubscriberTypeEnum.INTERNAL;

  /**
   * 应用名称, 内部用
   */
  @Nonnull
  private String appName = DBDefaults.STRING_VALUE;

  /**
   * 应用编码, 外部应用拥有此属性
   */
  @Nonnull
  private String application = DBDefaults.STRING_VALUE;

  /**
   * 接收推送的地址, 外部应用拥有此属性
   */
  @Nonnull
  private String receiveUrl = DBDefaults.STRING_VALUE;
}
