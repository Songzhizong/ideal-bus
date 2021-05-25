package com.zzsong.bus.client.impl;

import com.zzsong.bus.client.Channel;
import com.zzsong.bus.client.EventConsumer;
import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.share.utils.JsonUtils;
import lombok.extern.apachecommons.CommonsLog;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2021/5/25
 */
@CommonsLog
public class AutoDeliverEventConsumer implements EventConsumer {
  @Override
  public void onMessage(@Nonnull DeliverEvent event, @Nonnull Channel channel) {
    log.info(JsonUtils.toJsonStringIgnoreNull(event));
    channel.ack(event.getRouteInstanceId());
  }
}
