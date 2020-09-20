package com.zzsong.bus.core.processor.pusher;

import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/20 11:48 上午
 */
@Component
public class ExternalDelivereChannel implements DelivereChannel {
  @Nonnull
  @Override
  public Mono<DeliveredResult> deliver(@Nonnull DeliveredEvent event) {
    return null;
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return "";
  }

  @Override
  public boolean heartbeat() {
    return true;
  }
}
