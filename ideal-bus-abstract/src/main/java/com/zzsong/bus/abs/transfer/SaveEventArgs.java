package com.zzsong.bus.abs.transfer;

import com.zzsong.bus.abs.constants.DBDefaults;
import com.zzsong.bus.abs.constants.EventTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

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

  public SaveEventArgs checkAndGet() {
    if (StringUtils.isBlank(topic)) {
      throw new IllegalArgumentException("topic不能为空");
    }
    return this;
  }
}
