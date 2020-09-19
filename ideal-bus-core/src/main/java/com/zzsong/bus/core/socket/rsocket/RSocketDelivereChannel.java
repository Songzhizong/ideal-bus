package com.zzsong.bus.core.socket.rsocket;

import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import com.zzsong.bus.core.processor.pusher.DelivereChannel;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/20 1:23 上午
 */
public class RSocketDelivereChannel implements DelivereChannel {
  private final long applicationId;
  @Nonnull
  private final String instanceId;
  @Nonnull
  private final RSocketRequester requester;

  public RSocketDelivereChannel(long applicationId,
                                @Nonnull String instanceId,
                                @Nonnull RSocketRequester requester) {
    this.applicationId = applicationId;
    this.instanceId = instanceId;
    this.requester = requester;
  }

  @Nonnull
  @Override
  public Mono<DeliveredResult> receive(@Nonnull DeliveredEvent event) {
    return null;
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public boolean heartbeat() {
    return !requester.rsocket().isDisposed();
  }
}
