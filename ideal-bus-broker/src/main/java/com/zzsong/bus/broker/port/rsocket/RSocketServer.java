package com.zzsong.bus.broker.port.rsocket;

import com.zzsong.bus.abs.domain.Application;
import com.zzsong.bus.abs.storage.RouteInstanceStorage;
import com.zzsong.bus.broker.admin.service.ApplicationService;
import com.zzsong.bus.broker.core.channel.Channel;
import com.zzsong.bus.broker.core.consumer.Consumer;
import com.zzsong.bus.broker.core.consumer.ConsumerManager;
import com.zzsong.bus.broker.core.queue.QueueManager;
import com.zzsong.bus.common.constants.RSocketRoute;
import com.zzsong.bus.common.transfer.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/19 5:48 下午
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class RSocketServer {
  private final QueueManager queueManager;
  private final ConsumerManager consumerManager;
  private final ApplicationService applicationService;
  private final RouteInstanceStorage routeInstanceStorage;

  /**
   * 客户端登录
   */
  @ConnectMapping(RSocketRoute.LOGIN)
  public void login(@Nonnull RSocketRequester requester, @Payload String loginMessage) {
    LoginArgs message = LoginArgs.parseMessage(loginMessage);
    long applicationId = message.getApplicationId();
    final String instanceId = message.getInstanceId();
    final String accessToken = message.getAccessToken();
    Mono<Optional<Application>> optionalMono = applicationService.loadById(applicationId);
    optionalMono.doOnNext(optional -> {
      Application application = optional.orElse(null);
      Channel[] warp = new Channel[1];
      Objects.requireNonNull(requester.rsocket())
          .onClose()
          .doFirst(() -> {
            String errMsg = null;
            if (application == null) {
              errMsg = "此应用不存在";
              log.info("应用: {} 不存在", applicationId);
            } else if (StringUtils.isNotBlank(application.getAccessToken())
                && Objects.equals(application.getAccessToken(), accessToken)) {
              errMsg = "accessToken不合法";
              log.info("{} 客户端: {} accessToken不合法", applicationId, instanceId);
            } else {
              log.info("{} 客户端: {} 建立连接.", applicationId, instanceId);
              String channelInstanceId = buildChannelId(applicationId, instanceId);
              RSocketChannel channel = new RSocketChannel(channelInstanceId, requester);
              Consumer consumer = consumerManager.loadConsumer(applicationId, routeInstanceStorage);
              consumer.addChannel(channel);
              warp[0] = channel;
            }
            if (errMsg != null) {
              requester.route(RSocketRoute.INTERRUPT)
                  .data(errMsg)
                  .retrieveMono(String.class)
                  .doOnNext(log::info)
                  .subscribe();
            } else {
              requester.route(RSocketRoute.CONNECTED)
                  .data("connected")
                  .retrieveMono(String.class)
                  .subscribe();
            }
          })
          .doOnError(error -> {
            String errMessage = error.getClass().getName() +
                ": " + error.getMessage();
            log.info("socket error: {}", errMessage);
          })
          .doFinally(consumer -> {
            Channel channel = warp[0];
            if (channel != null) {
              Consumer loadConsumer = consumerManager.loadConsumer(applicationId, routeInstanceStorage);
              loadConsumer.removeChannel(channel);
            }
            log.info("{} 客户端: {} 断开连接: {}", applicationId, instanceId, consumer);
          })
          .subscribe();
    }).subscribe();
  }

  /**
   * 客户端变更通道状态
   *
   * @param channelArgs 通道信息
   */
  @MessageMapping(RSocketRoute.CHANNEL_CHANGE_STATUS)
  public Mono<Boolean> channelChangeStatus(@Nonnull ChannelArgs channelArgs) {
    long applicationId = channelArgs.getApplicationId();
    String instanceId = channelArgs.getInstanceId();
    int status = channelArgs.getStatus();
    String channelId = buildChannelId(applicationId, instanceId);
    Consumer consumer = consumerManager.loadConsumer(applicationId, routeInstanceStorage);
    if (status == ChannelArgs.STATUS_IDLE) {
      consumer.markChannelsAvailable(Collections.singleton(channelId));
    } else {
      consumer.markChannelBusy(channelId);
    }
    return Mono.just(true);
  }

  /**
   * 客户端签收消息
   */
  @MessageMapping(RSocketRoute.MESSAGE_ACK)
  public Mono<Boolean> ack(@Nonnull AckArgs args) {
    long routeInstanceId = args.getRouteInstanceId();
    return queueManager.ack(routeInstanceId);
  }

  /**
   * 客户端拒绝消息
   */
  @MessageMapping(RSocketRoute.MESSAGE_REJECT)
  public Mono<Boolean> reject(@Nonnull RejectArgs args) {
    long routeInstanceId = args.getRouteInstanceId();
    String message = args.getMessage();
    return queueManager.reject(routeInstanceId, message);
  }

  @MessageMapping(RSocketRoute.HEARTBEAT)
  public Mono<Boolean> heartbeat(@Nonnull HeartbeatArgs args) {
    long applicationId = args.getApplicationId();
    String instanceId = args.getInstanceId();
    log.debug("客户端: {}-{} 发送心跳", applicationId, instanceId);
    return Mono.just(true);
  }

  @Nonnull
  private String buildChannelId(long applicationId, @Nonnull String instanceId) {
    return applicationId + "-" + instanceId;
  }
}
