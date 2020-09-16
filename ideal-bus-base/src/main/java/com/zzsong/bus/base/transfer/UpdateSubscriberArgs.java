package com.zzsong.bus.base.transfer;

import com.zzsong.bus.common.constant.DBDefaults;
import com.zzsong.bus.common.constant.SubscriberTypeEnum;
import com.zzsong.bus.common.exception.VisibleException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
public class UpdateSubscriberArgs {
  /**
   * 订阅者id
   */
  @Nonnull
  private Long subscriberId;
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

  public void checkArgs() {
    //noinspection ConstantConditions
    if (this.subscriberId == null || this.subscriberId < 1) {
      throw new VisibleException("subscriberId不能为空");
    }
    if (StringUtils.isBlank(title)) {
      throw new VisibleException("订阅者名称不能为空");
    }
  }
}
