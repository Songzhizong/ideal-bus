package com.zzsong.bus.base.transfer;

import com.zzsong.bus.common.constant.SubscriberTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
public class QuerySubscriberArgs {
  /**
   * 订阅者名称
   */
  @Nullable
  private String title;
  /**
   * 订阅者类型
   */
  @Nullable
  private SubscriberTypeEnum subscriberType;
  /**
   * 应用编码, 外部应用拥有此属性
   */
  @Nullable
  private String application;
}
