package com.zzsong.bus.abs.transfer;

import com.zzsong.bus.abs.constants.EventTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * 事件查询参数
 *
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
public class QueryEventArgs {

  /**
   * 主题, 也是事件的唯一id
   */
  @Nullable
  private String topic;

  /**
   * 归属模块
   */
  @Nullable
  private Long moduleId;

  /**
   * 事件类型
   */
  @Nullable
  private EventTypeEnum eventType;

  /**
   * 事件名称
   */
  @Nullable
  private String eventName;

}
