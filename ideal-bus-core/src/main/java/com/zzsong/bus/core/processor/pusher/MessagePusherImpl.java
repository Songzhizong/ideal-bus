package com.zzsong.bus.core.processor.pusher;

import com.zzsong.bus.abs.core.MessagePusher;
import com.zzsong.bus.abs.domain.RouteInfo;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/19 9:44 下午
 */
@Component
public class MessagePusherImpl implements MessagePusher {

  @Override
  public Mono<Boolean> push(@Nonnull RouteInfo routeInfo) {
    return null;
  }
}
