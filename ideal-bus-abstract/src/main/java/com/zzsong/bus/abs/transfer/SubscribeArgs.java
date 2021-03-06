package com.zzsong.bus.abs.transfer;

import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.abs.constants.DBDefaults;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
public class SubscribeArgs {

  /**
   * 订阅者id
   */
  @Nonnull
  private Long applicationId;
  /**
   * 事件主题, 也是事件的唯一id
   */
  @Nonnull
  private String topic;
  /**
   * 订阅条件表达式
   */
  @Nonnull
  private String condition = DBDefaults.STRING_VALUE;
  /**
   * 是否广播
   */
  private boolean broadcast = false;
  /**
   * 失败重试次数
   */
  private int retryCount = 0;
  /**
   * 订阅状态
   */
  private int status = Subscription.STATUS_ENABLED;

  public SubscribeArgs checkAndGet() {
    //noinspection ConstantConditions
    if (applicationId == null) {
      throw new IllegalArgumentException("应用id不能为空");
    }
    if (StringUtils.isBlank(topic)) {
      throw new IllegalArgumentException("topic不能为空");
    }
    return this;
  }
}
