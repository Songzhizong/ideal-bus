package com.zzsong.bus.broker.core.consumer;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.broker.core.channel.Channel;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author 宋志宗 on 2021/5/19
 */
public interface Consumer {

  @Nonnull
  Mono<DeliverStatus> deliverMessage(@Nonnull RouteInstance routeInstance);

  void addChannel(@Nonnull Channel channel);

  void removeChannel(@Nonnull Channel channel);

  void markChannelBusy(@Nonnull String channelId);

  void markChannelsAvailable(@Nonnull Collection<String> channelIds);
}
