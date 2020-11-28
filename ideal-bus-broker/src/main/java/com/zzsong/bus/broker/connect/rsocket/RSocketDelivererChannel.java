package com.zzsong.bus.broker.connect.rsocket;

import com.zzsong.bus.common.constants.RSocketRoute;
import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.message.DeliverResult;
import com.zzsong.bus.broker.connect.DelivererChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author 宋志宗 on 2020/9/20 1:23 上午
 */
@Slf4j
public class RSocketDelivererChannel implements DelivererChannel {
  private static final ParameterizedTypeReference<DeliverResult> DELIVERED_RESULT_RES
      = new ParameterizedTypeReference<>() {
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
  public Mono<DeliverResult> deliver(@Nonnull DeliverEvent event) {
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
    return requester.rsocket() != null
        && !Objects.requireNonNull(requester.rsocket()).isDisposed();
  }
}
