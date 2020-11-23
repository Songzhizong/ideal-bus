package com.zzsong.bus.abs.domain;

import com.zzsong.bus.abs.constants.DBDefaults;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
public class Event {
  public static final long TOTAL_MILLIS_OF_DAY = 86400000L;

  /** 主题, 也是事件的唯一id */
  @Nonnull
  private String topic;

  /** 事件名称 */
  @Nonnull
  private String eventName;

  /** 描述 */
  @Nonnull
  private String desc;

  /** 事件实例持久化存储的过期时间 单位 ms, 大于1天生效 */
  private long expire;

  public long getExpire() {
    return expire >= TOTAL_MILLIS_OF_DAY ? expire : DBDefaults.LONG_VALUE;
  }

  public void setExpire(long expire) {
    this.expire = expire >= TOTAL_MILLIS_OF_DAY ? expire : DBDefaults.LONG_VALUE;
  }
}
