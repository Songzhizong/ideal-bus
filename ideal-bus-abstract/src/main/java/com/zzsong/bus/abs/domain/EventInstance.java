package com.zzsong.bus.abs.domain;

import com.zzsong.bus.common.message.EventMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
public class EventInstance extends EventMessage<Object> {

  /** 事件id */
  private Long eventId;
}
