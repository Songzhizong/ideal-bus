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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveredResult {

  @Nonnull
  private Long instanceId;

  /**
   * listener name -> ack
   * <pre>
   *   记录各个监听器的ack情况.
   *   如果为空或者全部为true则说明本次事件已完成了全部的投递任务
   * </pre>
   */
  private final Map<String, Boolean> ackMap = new HashMap<>();

  public void markAck(@Nonnull String listener, boolean ack) {
    ackMap.put(listener, ack);
  }
}
