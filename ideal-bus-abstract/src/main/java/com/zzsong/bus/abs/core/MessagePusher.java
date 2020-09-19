package com.zzsong.bus.abs.core;

import com.zzsong.bus.abs.domain.RouteInfo;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * 消息推送处理器
 *
 * @author 宋志宗 on 2020/9/19 9:43 下午
 */
public interface MessagePusher {

  Mono<Boolean> push(@Nonnull RouteInfo routeInfo);
}
