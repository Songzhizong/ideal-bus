package com.zzsong.bus.broker.connect;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.common.message.DeliverResult;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 连接管理器
 *
 * @author 宋志宗 on 2020/11/25
 */
public interface ConnectionManager {

  /**
   * 注册连接通道
   *
   * @param appName 应用名称
   * @param channel 通道
   * @author 宋志宗 on 2020/11/25
   */
  void registerChannel(@Nonnull String appName, @Nonnull DelivererChannel channel);

  /**
   * 标记通道为忙碌状态
   *
   * @param appName 应用名称
   * @param channel 通道
   * @author 宋志宗 on 2020/11/25
   */
  void markChannelBusy(@Nonnull String appName, @Nonnull DelivererChannel channel);

  /**
   * 标记通道为离线
   *
   * @param appName 应用名称
   * @param channel 通道
   * @author 宋志宗 on 2020/11/25
   */
  void markChannelDown(@Nonnull String appName, @Nonnull DelivererChannel channel);

  /**
   * 标记通道为可用
   *
   * @param appName 应用名称
   * @param channel 通道
   * @author 宋志宗 on 2020/11/25
   */
  void markChannelReachable(@Nonnull String appName, @Nonnull DelivererChannel channel);

  /**
   * 交付
   *
   * @param routeInstance 路由实例
   * @return 交付结果
   * @author 宋志宗 on 2020/11/25
   */
  @Nonnull
  Mono<DeliverResult> deliver(@Nonnull RouteInstance routeInstance,
                              @Nullable PreDeliverHandler handler);
}
