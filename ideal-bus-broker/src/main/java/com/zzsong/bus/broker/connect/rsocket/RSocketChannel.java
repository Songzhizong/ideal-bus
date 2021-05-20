package com.zzsong.bus.broker.connect.rsocket;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.broker.core.channel.Channel;
import com.zzsong.bus.broker.core.consumer.DeliverStatus;
import com.zzsong.bus.common.constants.RSocketRoute;
import com.zzsong.bus.common.message.DeliverEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2021/5/19
 */
@Slf4j
@RequiredArgsConstructor
public class RSocketChannel implements Channel {
  private final String channelId;
  private final RSocketRequester requester;

  @Nonnull
  @Override
  public Mono<DeliverStatus> deliverMessage(@Nonnull RouteInstance routeInstance) {
    DeliverEvent deliverEvent = createDeliverEvent(routeInstance);
    return requester.route(RSocketRoute.CLIENT_RECEIVE).data(deliverEvent)
        .retrieveMono(DeliverStatus.class);
  }

  @Nonnull
  @Override
  public String getChannelId() {
    return channelId;
  }

  @Nonnull
  private DeliverEvent createDeliverEvent(@Nonnull RouteInstance instance) {
    DeliverEvent deliveredEvent = new DeliverEvent();
    deliveredEvent.setRouteInstanceId(instance.getInstanceId());
    deliveredEvent.setSubscriptionId(instance.getSubscriptionId());
    deliveredEvent.setEventId(instance.getEventId());
    String transactionId = instance.getTransactionId();
    if (transactionId == null) {
      transactionId = "";
    }
    deliveredEvent.setTransactionId(transactionId);
    deliveredEvent.setTopic(instance.getTopic());
    deliveredEvent.setHeaders(instance.getHeaders());
    deliveredEvent.setPayload(instance.getPayload());
    deliveredEvent.setTimestamp(instance.getTimestamp());
    deliveredEvent.setListener(instance.getListener());
    return deliveredEvent;
  }
}
