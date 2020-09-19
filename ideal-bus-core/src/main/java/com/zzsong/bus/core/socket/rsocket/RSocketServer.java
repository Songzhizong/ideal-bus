package com.zzsong.bus.core.socket.rsocket;

import com.google.common.collect.ImmutableList;
import com.zzsong.bus.abs.domain.Application;
import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.common.constants.RSocketRoute;
import com.zzsong.bus.common.message.LoginMessage;
import com.zzsong.bus.common.message.PublishResult;
import com.zzsong.bus.common.transfer.AutoSubscribeArgs;
import com.zzsong.bus.core.admin.service.SubscriptionService;
import com.zzsong.bus.core.processor.LocalCache;
import com.zzsong.bus.core.processor.PublishService;
import com.zzsong.bus.core.processor.pusher.DelivereChannel;
import com.zzsong.common.loadbalancer.LbFactory;
import com.zzsong.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * @author 宋志宗 on 2020/9/19 5:48 下午
 */
@Controller
public class RSocketServer {
  private static final Logger log = LoggerFactory.getLogger(RSocketServer.class);
  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final PublishService publishService;
  @Nonnull
  private final SubscriptionService subscriptionService;
  @Nonnull
  private final LbFactory<DelivereChannel> lbFactory;

  public RSocketServer(@Nonnull LocalCache localCache,
                       @Nonnull PublishService publishService,
                       @Nonnull SubscriptionService subscriptionService,
                       @Nonnull LbFactory<DelivereChannel> lbFactory) {
    this.localCache = localCache;
    this.publishService = publishService;
    this.subscriptionService = subscriptionService;
    this.lbFactory = lbFactory;
  }


  @ConnectMapping(RSocketRoute.LOGIN)
  public void login(@Nonnull RSocketRequester requester, @Payload String loginMessage) {
    LoginMessage message = LoginMessage.parseMessage(loginMessage);
    long applicationId = message.getApplicationId();
    final String instanceId = message.getInstanceId();
    final String accessToken = message.getAccessToken();
    final String appName = applicationId + "";
    final Application application = localCache.getApplication(applicationId);
    DelivereChannel[] warp = new DelivereChannel[1];
    requester.rsocket()
        .onClose()
        .doFirst(() -> {
          if (application == null ||
              (StringUtils.isNotBlank(application.getAccessToken())
                  && Objects.equals(application.getAccessToken(), accessToken))) {
            String errMsg;
            if (application == null) {
              errMsg = "此应用不存在";
              log.info("应用: {} 不存在", applicationId);
            } else {
              errMsg = "accessToken不合法";
              log.info("{} 客户端: {} accessToken不合法", applicationId, instanceId);
            }
            requester.route(RSocketRoute.INTERRUPT)
                .data(errMsg)
                .retrieveMono(String.class)
                .doOnNext(log::info)
                .subscribe();
          } else {
            log.info("{} 客户端: {} 建立连接.", applicationId, instanceId);
            RSocketDelivereChannel channel
                = new RSocketDelivereChannel(applicationId, instanceId, requester);
            warp[0] = channel;
            lbFactory.addServers(appName, ImmutableList.of(channel));
          }
        })
        .doOnError(error -> {
          String errMessage = error.getClass().getName() +
              ": " + error.getMessage();
          log.info("socket error: {}", errMessage);
        })
        .doFinally(consumer -> {
          DelivereChannel channel = warp[0];
          if (channel != null) {
            lbFactory.markServerDown(appName, channel);
          }
          log.info("{} 客户端: {} 断开连接: {}", applicationId, instanceId, consumer);
        })
        .subscribe();
  }

  @MessageMapping(RSocketRoute.PUBLISH)
  public Mono<PublishResult> publish(@Nonnull EventInstance message) {
    return publishService.publish(message);
  }

  @MessageMapping(RSocketRoute.AUTO_SUBSCRIB)
  public Mono<String> autoSubscrib(@Nonnull AutoSubscribeArgs autoSubscribeArgs) {
    return subscriptionService.autoSubscrib(autoSubscribeArgs).map(JsonUtils::toJsonString);
  }
}
