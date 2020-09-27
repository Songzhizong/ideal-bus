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
  /**
   * 未路由, 不存在满足条件的订阅关系
   */
  public static final int NOT_ROUTED = 0;
  /**
   * 存在满足条件的订阅关系, 并对消息进行了分发
   */
  public static final int ROUTED = 1;

  /**
   * 状态
   */
  private int status = 0;
}
