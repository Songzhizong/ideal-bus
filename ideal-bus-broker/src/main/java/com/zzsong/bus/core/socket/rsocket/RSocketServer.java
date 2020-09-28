package com.zzsong.bus.core.socket.rsocket;

import com.google.common.collect.ImmutableList;
import com.zzsong.bus.abs.domain.Application;
import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.common.constants.RSocketRoute;
import com.zzsong.bus.common.message.ChannelInfo;
import com.zzsong.bus.common.message.LoginMessage;
import com.zzsong.bus.common.message.PublishResult;
import com.zzsong.bus.common.transfer.AutoSubscribeArgs;
import com.zzsong.bus.core.admin.service.SubscriptionService;
import com.zzsong.bus.core.config.BusConfig;
import com.zzsong.bus.core.processor.LocalCache;
import com.zzsong.bus.core.processor.EventExchanger;
import com.zzsong.bus.core.processor.pusher.DelivererChannel;
import com.zzsong.common.loadbalancer.LbFactory;
import com.zzsong.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 宋志宗 on 2020/9/19 5:48 下午
 */
@Slf4j
@Controller
public class RSocketServer {
  @Nonnull
  private final BusConfig busConfig;
  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final EventExchanger eventExchanger;
  @Nonnull
  private final SubscriptionService subscriptionService;
  @Nonnull
  private final LbFactory<DelivererChannel> lbFactory;
  @Nonnull
  private final ConcurrentMap<String, DelivererChannel> channelMap = new ConcurrentHashMap<>();

  public RSocketServer(@Nonnull BusConfig busConfig,
                       @Nonnull LocalCache localCache,
                       @Nonnull EventExchanger eventExchanger,
                       @Nonnull SubscriptionService subscriptionService,
                       @Nonnull LbFactory<DelivererChannel> lbFactory) {
    this.busConfig = busConfig;
    this.localCache = localCache;
    this.eventExchanger = eventExchanger;
    this.subscriptionService = subscriptionService;
    this.lbFactory = lbFactory;
  }


  @ConnectMapping(RSocketRoute.LOGIN)
  public void login(@Nonnull RSocketRequester requester, @Payload String loginMessage) {
    LoginMessage message = LoginMessage.parseMessage(loginMessage);
    long applicationId = message.getApplicationId();
    final String instanceId = message.getInstanceId();
    final String accessToken = message.getAccessToken();
    int socketType = message.getSocketType();
    final String appName = applicationId + "";
    final Application application = localCache.getApplication(applicationId);
    DelivererChannel[] warp = new DelivererChannel[1];
    requester.rsocket()
        .onClose()
        .doFirst(() -> {
          String errMsg = null;
          if (application == null) {
            errMsg = "此应用不存在";
            log.info("应用: {} 不存在", applicationId);
          } else if (StringUtils.isNotBlank(application.getAccessToken())
              && Objects.equals(application.getAccessToken(), accessToken)) {
            errMsg = "accessToken不合法";
            log.info("{} 客户端: {}-{} accessToken不合法", applicationId, instanceId, socketType);
          } else if (!busConfig.isInitialized()) {
            errMsg = "broker尚未准备就绪";
            log.info("{} 客户端: {}-{} 建立连接失败, broker尚未准备就绪",
                applicationId, instanceId, socketType);
          } else {
            log.info("{} 客户端: {}-{} 建立连接.", applicationId, instanceId, socketType);
            if (socketType == LoginMessage.SOCKET_TYPE_RECEIVE) {
              RSocketDelivererChannel channel
                  = new RSocketDelivererChannel(instanceId, requester);
              warp[0] = channel;
              String channelKey = buildChannelKey(appName, instanceId);
              channelMap.put(channelKey, channel);
              lbFactory.addServers(appName, ImmutableList.of(channel));
            }
          }
          if (errMsg != null) {
            requester.route(RSocketRoute.INTERRUPT)
                .data(errMsg)
                .retrieveMono(String.class)
                .doOnNext(log::info)
                .subscribe();
          }
        })
        .doOnError(error -> {
          String errMessage = error.getClass().getName() +
              ": " + error.getMessage();
          log.info("socket error: {}", errMessage);
        })
        .doFinally(consumer -> {
          DelivererChannel channel = warp[0];
          if (channel != null) {
            lbFactory.markServerDown(appName, channel);
          }
          log.info("{} 客户端: {}-{} 断开连接: {}", applicationId, instanceId, socketType, consumer);
        })
        .subscribe();
  }

  @MessageMapping(RSocketRoute.PUBLISH)
  public Mono<PublishResult> publish(@Nonnull EventInstance message) {
    return eventExchanger.publish(message);
  }

  @MessageMapping(RSocketRoute.AUTO_SUBSCRIBE)
  public Mono<String> autoSubscribe(@Nonnull AutoSubscribeArgs autoSubscribeArgs) {
    return subscriptionService.autoSubscribe(autoSubscribeArgs).map(JsonUtils::toJsonString);
  }

  /**
   * 将通道标记为忙碌状态
   *
   * @param channelInfo 通道信息
   */
  @MessageMapping(RSocketRoute.CHANNEL_CHANGE)
  public Mono<Boolean> markChannelBusy(@Nonnull ChannelInfo channelInfo) {
    String instanceId = channelInfo.getInstanceId();
    String appName = channelInfo.getAppName();
    int status = channelInfo.getStatus();
    String channelKey = buildChannelKey(appName, instanceId);
    DelivererChannel channel = channelMap.get(channelKey);
    if (channel == null) {
      log.error("channel: {} 不存在", channelKey);
    } else {
      if (status == ChannelInfo.STATUS_BUSY) {
        lbFactory.markServerDown(appName, channel);
      } else if (status == ChannelInfo.STATUS_IDLE) {
        lbFactory.markServerReachable(appName, channel);
      } else {
        log.warn("未知的通道状态: {}", status);
      }
    }
    return Mono.just(true);
  }

  @Nonnull
  private String buildChannelKey(@Nonnull String appName, @Nonnull String instanceId) {
    return appName + "-" + instanceId;
  }
}
