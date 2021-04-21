package com.zzsong.bus.broker.connect;

import com.google.common.collect.ImmutableList;
import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.message.DeliverResult;
import com.zzsong.bus.common.share.loadbalancer.LbFactory;
import com.zzsong.bus.common.share.loadbalancer.LbStrategyEnum;
import com.zzsong.bus.common.share.loadbalancer.SimpleLbFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.channels.ClosedChannelException;
import java.util.List;

/**
 * @author 宋志宗 on 2020/11/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionManagerImpl implements ConnectionManager {
  @Nonnull
  private final LbFactory<DelivererChannel> lbFactory = new SimpleLbFactory<>();

  @Override
  public void registerChannel(@Nonnull String appName, @Nonnull DelivererChannel channel) {
    lbFactory.addServers(appName, ImmutableList.of(channel));
  }

  @Override
  public void markChannelBusy(@Nonnull String appName, @Nonnull DelivererChannel channel) {
    lbFactory.markServerDown(appName, channel);
  }

  @Override
  public void markChannelDown(@Nonnull String appName, @Nonnull DelivererChannel channel) {
    lbFactory.markServerDown(appName, channel);
  }

  @Override
  public void markChannelReachable(@Nonnull String appName, @Nonnull DelivererChannel channel) {
    lbFactory.markServerReachable(appName, channel);
  }

  @Nonnull
  @Override
  public Mono<DeliverResult> deliver(@Nonnull RouteInstance routeInstance,
                                     @Nullable PreDeliverHandler handler) {
    Long eventId = routeInstance.getEventId();
    Long applicationId = routeInstance.getApplicationId();
    DeliverEvent deliveredEvent = createDeliverEvent(routeInstance);
    boolean broadcast = routeInstance.isBroadcast();
    if (broadcast) {
      List<DelivererChannel> channels = lbFactory
          .getReachableServers(applicationId + "");
      if (channels.isEmpty()) {
        log.warn("应用: {} 可用通道列表为空", applicationId);
        DeliverResult offLineResult
            = new DeliverResult(eventId, DeliverResult.Status.APP_OFFLINE, "");
        return Mono.just(offLineResult);
      }
      Mono<Boolean> doOnPreDeliver;
      if (handler == null) {
        doOnPreDeliver = Mono.just(true);
      } else {
        doOnPreDeliver = handler.doOnPreDeliver(routeInstance);
      }
      return doOnPreDeliver.flatMap(l ->
          Flux.fromIterable(channels)
              .flatMap(channel -> channel.deliver(deliveredEvent))
              .onErrorResume(throwable -> {
                log.error("交付异常: ", throwable);
                String message = throwable.getMessage();
                if (message == null) {
                  message = "";
                }
                DeliverResult offLineResult
                    = new DeliverResult(eventId, DeliverResult.Status.UN_ACK, message);
                return Mono.just(offLineResult);
              })
              .collectList()
              .map(list -> {
                DeliverResult deliveredResult = new DeliverResult();
                deliveredResult.setEventId(deliveredEvent.getEventId());
                deliveredResult.setStatus(DeliverResult.Status.ACK);
                return deliveredResult;
              })
      );
    }

    String aggregate = routeInstance.getAggregate();
    String topic = routeInstance.getTopic();
    DelivererChannel channel;
    if (StringUtils.isNotBlank(aggregate)) {
      channel = lbFactory.chooseServer(applicationId + "",
          aggregate, LbStrategyEnum.CONSISTENT_HASH);
    } else {
      channel = lbFactory.chooseServer(applicationId + "",
          topic, LbStrategyEnum.ROUND_ROBIN);
    }
    if (channel == null) {
      log.debug("应用: {} 可用通道列表为空", applicationId);
      DeliverResult offLineResult
          = new DeliverResult(eventId, DeliverResult.Status.APP_OFFLINE, "");
      return Mono.just(offLineResult);
    }
    Mono<Boolean> doOnPreDeliver;
    if (handler == null) {
      doOnPreDeliver = Mono.just(true);
    } else {
      doOnPreDeliver = handler.doOnPreDeliver(routeInstance);
    }
    return doOnPreDeliver.flatMap(l -> channel.deliver(deliveredEvent))
        .onErrorResume(throwable -> {
          DeliverResult deliverResult = new DeliverResult();
          deliverResult.setEventId(deliveredEvent.getEventId());
          if (throwable instanceof ClosedChannelException) {
            deliverResult.setStatus(DeliverResult.Status.CHANNEL_CLOSED);
          } else {
            log.error("交付出现未知异常: ", throwable);
            deliverResult.setStatus(DeliverResult.Status.UN_ACK);
            String message = throwable.getMessage();
            if (message != null) {
              deliverResult.setMessage(message);
            }
          }
          return Mono.just(deliverResult);
        });
  }

  @Nonnull
  private DeliverEvent createDeliverEvent(@Nonnull RouteInstance instance) {
    DeliverEvent deliveredEvent = new DeliverEvent();
    deliveredEvent.setRouteInstanceId(instance.getInstanceId());
    deliveredEvent.setSubscriptionId(instance.getSubscriptionId());
    deliveredEvent.setEventId(instance.getEventId());
    deliveredEvent.setTransactionId(instance.getTransactionId());
    deliveredEvent.setTopic(instance.getTopic());
    deliveredEvent.setHeaders(instance.getHeaders());
    deliveredEvent.setPayload(instance.getPayload());
    deliveredEvent.setTimestamp(instance.getTimestamp());
    deliveredEvent.setListener(instance.getListener());
    return deliveredEvent;
  }
}
