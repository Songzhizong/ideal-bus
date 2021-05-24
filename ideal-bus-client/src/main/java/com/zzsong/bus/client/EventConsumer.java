package com.zzsong.bus.client;

import com.zzsong.bus.common.message.DeliverEvent;

import javax.annotation.Nonnull;

/**
 * 消费者
 *
 * @author 宋志宗 on 2021/4/29
 */
public interface EventConsumer {

  void onMessage(@Nonnull DeliverEvent event, @Nonnull Channel channel);
}
