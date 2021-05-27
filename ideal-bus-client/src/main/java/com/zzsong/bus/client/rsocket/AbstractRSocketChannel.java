package com.zzsong.bus.client.rsocket;

import com.zzsong.bus.common.constants.RSocketRoute;
import com.zzsong.bus.common.transfer.LoginArgs;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketClient;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗 on 2021/4/28
 */
@CommonsLog
public abstract class AbstractRSocketChannel extends Thread implements RSocketChannel {
  private static final int RESTART_DELAY = 10;

  private final BlockingQueue<Boolean> restartNoticeQueue = new ArrayBlockingQueue<>(1);
  @Nonnull
  private final String brokerIp;
  private final int brokerPort;
  protected final long applicationId;
  @Nonnull
  protected final String clientIpPort;
  @Nullable
  private final String accessToken;
  @Nonnull
  protected final String brokerAddress;

  protected volatile boolean destroyed = false;
  @Nullable
  protected RSocketRequester socketRequester = null;

  protected AbstractRSocketChannel(@Nonnull String brokerIp,
                                   int brokerPort,
                                   long applicationId,
                                   @Nonnull String clientIpPort,
                                   @Nullable String accessToken) {
    this.brokerIp = brokerIp;
    this.brokerPort = brokerPort;
    this.applicationId = applicationId;
    this.clientIpPort = clientIpPort;
    this.accessToken = accessToken;
    this.brokerAddress = brokerIp + ":" + brokerPort;
    this.setName(brokerAddress);
  }

  @MessageMapping(RSocketRoute.INTERRUPT)
  public Mono<String> interrupt(String status) {
    restartSocket("Broker: " + brokerAddress + " 服务中断: " + status + ", " + RESTART_DELAY + " 秒后尝试重连...");
    return Mono.just("received...");
  }

  @Override
  public void connect() {
    this.start();
  }

  @Override
  public void close() {
    if (destroyed) {
      return;
    }
    log.info("close channel: " + brokerAddress);
    destroyed = true;
    if (socketRequester != null) {
      RSocket rsocket = socketRequester.rsocket();
      if (rsocket != null) {
        rsocket.dispose();
      }
      RSocketClient rSocketClient = socketRequester.rsocketClient();
      rSocketClient.dispose();
    }
    this.interrupt();
    log.info("RSocketBusChannel destroy, broker address: " + brokerAddress);
  }

  private synchronized void doConnect() {
    log.info("doConnect...");
    RSocketStrategies rSocketStrategies = RSocketConfigure.R_SOCKET_STRATEGIES;
    RSocketRequester.Builder requesterBuilder = RSocketConfigure.R_SOCKET_REQUESTER_BUILDER;
    SocketAcceptor responder
        = RSocketMessageHandler.responder(rSocketStrategies, this);
    final LoginArgs message = new LoginArgs();
    message.setApplicationId(applicationId);
    message.setInstanceId(clientIpPort);
    message.setAccessToken(accessToken);
    final String messageString = message.toMessageString();
    if (socketRequester != null && !socketRequester.rsocketClient().isDisposed()) {
      socketRequester.rsocketClient().dispose();
      try {
        socketRequester = requesterBuilder
            .setupRoute(RSocketRoute.LOGIN)
            .setupData(messageString)
            .rsocketConnector(connector -> connector.acceptor(responder))
            .tcp(brokerIp, brokerPort);
      } catch (Exception e) {
        log.info("e: " + e.getMessage());
        restartSocket(null);
        return;
      }
    } else {
      try {
        socketRequester = requesterBuilder
            .setupRoute(RSocketRoute.LOGIN)
            .setupData(messageString)
            .rsocketConnector(connector -> connector.acceptor(responder))
            .tcp(brokerIp, brokerPort);
      } catch (Exception e) {
        log.info("e: " + e.getMessage());
        restartSocket(null);
        return;
      }
    }
    socketRequester.rsocketClient().source()
        .doOnNext(rSocket -> rSocket.onClose()
            .doOnError(error -> {
              String errMessage = error.getClass().getSimpleName() +
                  ": " + error.getMessage();
              log.info("Broker socket error: " + errMessage);
            })
            .doFinally(consumer -> {
              String msg = "Broker " + brokerAddress + " 连接断开: "
                  + consumer + ", " + RESTART_DELAY + " 秒后尝试重连...";
              restartSocket(msg);
            })
            .subscribe())
        .map(r -> true)
        .onErrorResume(error -> {
          String errMessage = error.getClass().getSimpleName() +
              ": " + error.getMessage();
          restartSocket("Broker socket connect failure: " + errMessage);
          return Mono.just(true);
        })
        .subscribe();
  }

  protected void restartSocket(@Nullable String message) {
    if (!destroyed) {
      if (message != null) {
        log.info(message);
      }
      restartNoticeQueue.offer(true);
    }
  }

  @Override
  public void run() {
    doConnect();
    while (!destroyed) {
      Boolean poll;
      try {
        poll = restartNoticeQueue.poll(5, TimeUnit.SECONDS);
        if (poll != null) {
          TimeUnit.SECONDS.sleep(RESTART_DELAY);
          log.info("Restart socket, broker address: " + brokerAddress);
          doConnect();
        }
      } catch (InterruptedException e) {
        log.info("restartNoticeQueue.poll interrupted");
      }
    }
  }
}
