package com.zzsong.bus.common.message;

import lombok.*;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveredResult {

  @Nonnull
  private String eventId;

  private boolean success;

  /**
   * listener name -> ack
   * <pre>
   *   记录各个监听器的ack情况.
   *   如果为null则代表本次失败, 需要重新交付
   *   如果为空或者全部为true则说明本次事件已完成了全部的投递任务
   * </pre>
   */
  @Nonnull
  private final Map<String, Boolean> ackMap = new HashMap<>();

  public void markAck(@Nonnull String listener, boolean ack) {
    ackMap.put(listener, ack);
  }
}
