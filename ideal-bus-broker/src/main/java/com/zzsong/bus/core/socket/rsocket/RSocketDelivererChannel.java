package com.zzsong.bus.core.socket.rsocket;

import com.zzsong.bus.common.constants.RSocketRoute;
import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import com.zzsong.bus.core.processor.pusher.DelivererChannel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/20 1:23 上午
 */
public class RSocketDelivererChannel implements DelivererChannel {
  private static final ParameterizedTypeReference<DeliveredResult> DELIVERED_RESULT_RES
      = new ParameterizedTypeReference<DeliveredResult>() {
  };
  @Nonnull
  private final String instanceId;
  @Nonnull
  private final RSocketRequester requester;

  public RSocketDelivererChannel(@Nonnull String instanceId,
                                 @Nonnull RSocketRequester requester) {
    this.instanceId = instanceId;
    this.requester = requester;
  }

  @Nonnull
  @Override
  public Mono<DeliveredResult> deliver(@Nonnull DeliveredEvent event) {
    return requester.route(RSocketRoute.CLIENT_RECEIVE)
        .data(event)
        .retrieveMono(DELIVERED_RESULT_RES);
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
