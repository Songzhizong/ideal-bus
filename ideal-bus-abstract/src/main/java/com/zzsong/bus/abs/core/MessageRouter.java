package com.zzsong.bus.abs.core;

import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.common.message.PublishResult;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * 负责接收客户端发送过来的事件
 *
 * @author 宋志宗 on 2020/9/17
 */
public interface MessageRouter {

  Mono<PublishResult> route(@Nonnull EventInstance message);
}
