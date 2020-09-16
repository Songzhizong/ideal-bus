package com.zzsong.bus.base.transfer;

import com.zzsong.bus.common.constant.DBDefaults;
import com.zzsong.bus.common.constant.EventTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotBlank;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaveEventArgs {

  /**
   * 主题, 也是事件的唯一id
   */
  @Nonnull
  @NotBlank(message = "topic不能为空")
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
  private String eventName = DBDefaults.STRING_VALUE;

  /**
   * 描述
   */
  @Nonnull
  private String desc = DBDefaults.STRING_VALUE;
}
