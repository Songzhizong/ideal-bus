package com.zzsong.bus.broker.core.consumer;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2021/5/14
 */
public interface ConsumerManager {

  @Nonnull
  Consumer loadConsumer(long applicationId);
}
