package com.zzsong.bus.broker.core.consumer;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.broker.core.channel.Channel;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * 消费者接口
 *
 * @author 宋志宗 on 2021/5/19
 */
public interface Consumer {

  /**
   * @return 消费端应用id
   * @author 宋志宗 on 2021/5/24
   */
  long getApplicationId();

  /**
   * 交付消息到消费端
   *
   * @param routeInstance 消息
   * @return 交付结果
   * @author 宋志宗 on 2021/5/24
   */
  @Nonnull
  Mono<DeliverStatus> deliverMessage(@Nonnull RouteInstance routeInstance);

  /**
   * 新增交付通道
   *
   * @param channel 交付通道
   * @author 宋志宗 on 2021/5/24
   */
  void addChannel(@Nonnull Channel channel);

  /**
   * 移除交付通道
   *
   * @param channel 交付通道
   * @author 宋志宗 on 2021/5/24
   */
  void removeChannel(@Nonnull Channel channel);

  /**
   * 将通道标记为忙碌状态
   *
   * @param channelId 交付通道id
   * @author 宋志宗 on 2021/5/24
   */
  void markChannelBusy(@Nonnull String channelId);


  /**
   * 将通道标记为可用状态
   *
   * @param channelIds 交付通道id列表
   * @author 宋志宗 on 2021/5/24
   */
  void markChannelsAvailable(@Nonnull Collection<String> channelIds);
}
