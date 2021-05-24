package com.zzsong.bus.broker.core.exchanger;

import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.common.message.ExchangeResult;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 交换器
 *
 * @author 宋志宗 on 2021/5/14
 */
public interface Exchanger {

  Mono<ExchangeResult> exchange(@Nonnull List<EventInstance> events);
}
