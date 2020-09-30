package com.zzsong.bus.client;

import com.zzsong.bus.common.message.EventMessage;
import com.zzsong.bus.common.message.PublishResult;
import com.zzsong.bus.common.transfer.AutoSubscribeArgs;
import com.zzsong.bus.receiver.BusReceiver;
import com.zzsong.bus.common.share.loadbalancer.LbServer;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/19 11:15 下午
 */
public interface BusChannel extends BusReceiver, LbServer {

  Mono<PublishResult> publishEvent(@Nonnull EventMessage<?> message);

  Mono<Boolean> changeStates(int status);

  Mono<Boolean> autoSubscribe(@Nonnull AutoSubscribeArgs autoSubscribeArgs);
}
