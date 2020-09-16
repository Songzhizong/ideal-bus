package com.zzsong.bus.base.domain;

import com.zzsong.bus.common.constant.DBDefaults;
import com.zzsong.bus.common.constant.EventTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
public class Event {

  /**
   * 主题, 也是事件的唯一id
   */
  @Nonnull
  private String topic;

  /**
   * 归属模块
   */
  private long moduleId = DBDefaults.LONG_VALUE;

  /**
   * 事件类型
   */
  @Nonnull
  private EventTypeEnum eventType = EventTypeEnum.UNKNOWN;

  /**
   * 事件名称
   */
  @Nonnull
  private String eventName;

  /**
   * 描述
   */
  @Nonnull
  private String desc;
}
