package com.zzsong.bus.base.transfer;

import com.zzsong.bus.common.constant.DBDefaults;
import com.zzsong.bus.common.constant.EventTypeEnum;
import com.zzsong.bus.common.exception.VisibleException;
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

  public void checkArgs() {
    if (StringUtils.isBlank(topic)) {
      throw new VisibleException("topic不能为空");
    }
  }
}
