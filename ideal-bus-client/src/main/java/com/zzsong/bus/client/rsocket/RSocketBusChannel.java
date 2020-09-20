package com.zzsong.bus.client.rsocket;

import com.google.common.collect.ImmutableList;
import com.zzsong.bus.client.BusChannel;
import com.zzsong.bus.client.BusClient;
import com.zzsong.bus.client.SimpleBusClient;
import com.zzsong.bus.common.constants.RSocketRoute;
import com.zzsong.bus.common.message.*;
import com.zzsong.bus.common.transfer.AutoSubscribeArgs;
import com.zzsong.common.loadbalancer.LbFactory;
import com.zzsong.common.utils.JsonUtils;
import io.rsocket.SocketAcceptor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗 on 2020/9/19 11:45 下午
 */
public class RSocketBusChannel extends Thread implements BusChannel {
  private static final Logger log = LoggerFactory.getLogger(RSocketBusChannel.class);
  private static final int RESTART_DELAY = 10;
  private static final ParameterizedTypeReference<PublishResult> PUBLISH_RESULT_RES
      = new ParameterizedTypeReference<PublishResult>() {
  };

  private final BlockingQueue<Boolean> restartNoticeQueue = new ArrayBlockingQueue<>(1);

  @Nonnull
  private final String brokerIp;
  private final int brokerPort;
  @Nonnull
  private final String brokerAddress;
  private final long applicationId;
  @Nonnull
  private final String clientIpPort;
  @Nonnull
  private final BusClient busClient;
  @Nonnull
  private final LbFactory<BusChannel> lbFactory;
  @Setter
  private String accessToken;

  private volatile boolean running = false;
  private volatile boolean destroyed = false;

  private RSocketRequester rsocketRequester;

  public RSocketBusChannel(@Nonnull String brokerIp,
                           int brokerPort, long applicationId,
                           @Nonnull String clientIpPort,
                           @Nonnull BusClient busClient,
                           @Nonnull LbFactory<BusChannel> lbFactory) {
    this.brokerIp = brokerIp;
    this.brokerPort = brokerPort;
    this.applicationId = applicationId;
    this.clientIpPort = clientIpPort;
    this.busClient = busClient;
    this.lbFactory = lbFactory;
    this.brokerAddress = brokerIp + ":" + brokerPort;
  }

  public void startChannel() {
    lbFactory.addServers(SimpleBusClient.BUS_BROKER_APP_NAME, ImmutableList.of(this));
    this.start();
  }

  private synchronized void connect() {
    if (destroyed) {
      log.info("RSocketBusChannel is destroyed, brokerAddress: {}", brokerAddress);
      return;
    }
    if (running) {
      log.info("RSocketBusChannel is running, brokerAddress: {}", brokerAddress);
      return;
    }
    RSocketStrategies rSocketStrategies = RSocketConfigure.rsocketStrategies;
    RSocketRequester.Builder requesterBuilder = RSocketConfigure.rSocketRequesterBuilder;
    SocketAcceptor responder
        = RSocketMessageHandler.responder(rSocketStrategies, this);
    final LoginMessage message = new LoginMessage();
    message.setApplicationId(applicationId);
    message.setInstanceId(clientIpPort);
    message.setAccessToken(accessToken);
    final String messageString = message.toMessageString();

    if (rsocketRequester != null && !rsocketRequester.rsocket().isDisposed()) {
      try {
        this.rsocketRequester.rsocket().dispose();
        this.rsocketRequester = requesterBuilder
            .setupRoute(RSocketRoute.LOGIN)
            .setupData(messageString)
            .rsocketConnector(connector -> connector.acceptor(responder))
            .connectTcp(brokerIp, brokerPort)
            .doOnError(e -> log.warn("Broker {} Login fail: ", brokerAddress, e))
            .doOnNext(r -> log.info("Broker {} login success.", brokerAddress))
            .block();
      } catch (Exception e) {
        restartSocket();
        return;
      }
    } else {
      try {
        this.rsocketRequester = requesterBuilder
            .setupRoute(RSocketRoute.LOGIN)
            .setupData(messageString)
            .rsocketConnector(connector -> connector.acceptor(responder))
            .connectTcp(brokerIp, brokerPort)
            .doOnError(e -> log.warn("Broker {} Login fail: ", brokerAddress, e))
            .doOnNext(r -> log.info("Broker {} login success.", brokerAddress))
            .block();
      } catch (Exception e) {
        restartSocket();
        return;
      }
    }
    assert this.rsocketRequester != null;
    this.rsocketRequester.rsocket()
        .onClose()
        .doOnError(error -> {
          String errMessage = error.getClass().getSimpleName() +
              ": " + error.getMessage();
          log.info("Broker socket error: {}", errMessage);
        })
        .doFinally(consumer -> {
          log.info("Broker {} 连接断开: {}, {} 秒后尝试重连...",
              brokerAddress, consumer, RESTART_DELAY);
          restartSocket();
        })
        .subscribe();
    running = true;
    lbFactory.markServerReachable(SimpleBusClient.BUS_BROKER_APP_NAME, this);
  }

  private void restartSocket() {
    if (running) {
      lbFactory.markServerDown(SimpleBusClient.BUS_BROKER_APP_NAME, this);
    }
    running = false;
    restartNoticeQueue.offer(true);
  }

  @Override
  public void run() {
    connect();
    while (!destroyed) {
      final Boolean poll;
      try {
        poll = restartNoticeQueue.poll(5, TimeUnit.SECONDS);
        if (poll != null) {
          TimeUnit.SECONDS.sleep(RESTART_DELAY);
          log.info("Restart socket, broker address: {}", brokerAddress);
          connect();
        }
      } catch (InterruptedException e) {
        // Interrupted
      }
    }
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return brokerAddress;
  }

  @Override
  public boolean heartbeat() {
    return running
        && !destroyed
        && rsocketRequester != null
        && !rsocketRequester.rsocket().isDisposed();
  }

  @Override
  public void dispose() {
    if (destroyed) {
      return;
    }
    destroyed = true;
    rsocketRequester.rsocket().dispose();
    this.interrupt();
    log.info("RSocketBusChannel destroy, broker address: {}", brokerAddress);
  }

  @Override
  public Mono<PublishResult> publishEvent(EventMessage<?> message) {
    return rsocketRequester.route(RSocketRoute.PUBLISH)
        .data(message)
        .retrieveMono(PUBLISH_RESULT_RES)
        .doOnNext(res -> {
          if (log.isDebugEnabled()) {
            log.debug("Publish result: {}", JsonUtils.toJsonString(res));
          }
        });

  }

  @Override
  public Mono<Boolean> autoSubscribe(@Nonnull AutoSubscribeArgs autoSubscribeArgs) {
    return rsocketRequester.route(RSocketRoute.AUTO_SUBSCRIB)
        .data(autoSubscribeArgs)
        .retrieveMono(String.class)
        .doOnNext(res -> {
          if (log.isDebugEnabled()) {
            log.debug("autoSubscribe result: {}", res);
          }
        })
        .map(s -> true);
  }

  @Nonnull
  @Override
  @MessageMapping(RSocketRoute.CLIENT_RECEIVE)
  public Mono<DeliveredResult> receive(@Nonnull DeliveredEvent event) {
    return busClient.receive(event);
  }

  @MessageMapping(RSocketRoute.INTERRUPT)
  public Mono<String> interrupt(String status) {
    log.warn("Broker: {} 服务中断: {}, {} 秒后尝试重连...",
        brokerAddress, status, RESTART_DELAY);
    restartSocket();
    return Mono.just("received...");
  }
}
