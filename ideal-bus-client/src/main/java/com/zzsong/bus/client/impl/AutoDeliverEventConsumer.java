package com.zzsong.bus.client.impl;

import com.zzsong.bus.client.Channel;
import com.zzsong.bus.client.EventConsumer;
import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.share.utils.JsonUtils;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2021/5/25
 */
public class AutoDeliverEventConsumer implements EventConsumer {
  @Override
  public void onMessage(@Nonnull DeliverEvent event, @Nonnull Channel channel) {
    System.out.println(JsonUtils.toJsonString(event, true, true));
    channel.ack(event.getRouteInstanceId());
  }
}
