package com.zzsong.bus.abs.pojo;

import com.zzsong.bus.abs.constants.DBDefaults;
import com.zzsong.bus.abs.constants.SubscriberTypeEnum;
import com.zzsong.bus.abs.domain.Subscription;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
public class SubscriptionDetails extends Subscription {

  /**
   * 订阅者类型
   */
  @Nonnull
  private SubscriberTypeEnum subscriberType;

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
