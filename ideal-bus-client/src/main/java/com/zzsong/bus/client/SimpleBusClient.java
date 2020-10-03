package com.zzsong.bus.client;

import com.zzsong.bus.client.rsocket.RSocketEventChannel;
import com.zzsong.bus.common.message.*;
import com.zzsong.bus.common.transfer.AutoSubscribeArgs;
import com.zzsong.bus.common.transfer.SubscriptionArgs;
import com.zzsong.bus.client.listener.IEventListener;
import com.zzsong.bus.client.listener.ListenerFactory;
import com.zzsong.bus.common.share.loadbalancer.LbFactory;
import com.zzsong.bus.common.share.loadbalancer.LbStrategyEnum;
import com.zzsong.bus.common.share.loadbalancer.SimpleLbFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗 on 2020/9/19 11:43 下午
 */
@Slf4j
public class SimpleBusClient extends SimpleEventReceiver implements BusClient {
  public static final String BUS_BROKER_APP_NAME = "busBroker";
  private final LbFactory<EventChannel> lbFactory = new SimpleLbFactory<>();

  /**
   * 应用ID
   */
  @Setter
  private long applicationId;
  @Setter
  private String brokerAddresses;
  @Setter
  private String accessToken;
  @Setter
  private String clientIpPort;
  @Setter
  private boolean autoSubscribe;

  public SimpleBusClient(int corePoolSize, int maximumPoolSize) {
    super(corePoolSize, maximumPoolSize);
  }

  public void startClient() {
    super.startReceiver();
    if (StringUtils.isBlank(brokerAddresses)) {
      log.error("brokerAddresses为空...");
      return;
    }
    // 初始化和broker之间的连接
    final List<EventChannel> channels = initChannel();
    // 自动注册
    if (!channels.isEmpty() && autoSubscribe) {
      autoSubscribe(channels.get(0));
    }
  }

  @Nonnull
  private List<EventChannel> initChannel() {
    final String[] addresses = StringUtils
        .split(brokerAddresses, ",");
    List<EventChannel> busChannels = new ArrayList<>();
    for (String address : addresses) {
      String[] split = StringUtils.split(address, ":");
      if (split.length != 2) {
        log.error("RSocket地址配置错误: {}", address);
        throw new IllegalArgumentException("RSocket地址配置错误");
      }
      String ip = split[0];
      int port;
      try {
        port = Integer.parseInt(split[1]);
      } catch (NumberFormatException e) {
        log.error("RSocket地址配置错误: {}", address);
        throw new IllegalArgumentException("RSocket地址配置错误");
      }
      RSocketEventChannel channel = new RSocketEventChannel(
          ip, port, applicationId, clientIpPort, this, lbFactory);
      channel.setAccessToken(accessToken);
      channel.startChannel();
      busChannels.add(channel);
    }
    return busChannels;
  }

  private void autoSubscribe(@Nonnull EventChannel busChannel) {
    final Map<String, Map<String, IEventListener>> all = ListenerFactory.getAll();
    final AutoSubscribeArgs autoSubscribeArgs = new AutoSubscribeArgs();
    autoSubscribeArgs.setApplicationId(applicationId);
    final List<SubscriptionArgs> subscriptionArgsList = new ArrayList<>();
    all.forEach((topic, map) -> map.forEach((name, listener) -> {
      SubscriptionArgs args = new SubscriptionArgs();
      args.setTopic(topic);
      args.setListenerName(name);
      args.setDelayExp(listener.getDelayExp());
      args.setCondition(listener.getCondition());
      subscriptionArgsList.add(args);
    }));
    autoSubscribeArgs.setSubscriptionArgsList(subscriptionArgsList);
    while (!busChannel.heartbeat()) {
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    busChannel.autoSubscribe(autoSubscribeArgs).block();
  }

  @Nonnull
  private EventChannel getBusChannel(@Nonnull EventMessage<?> message) {
    final String key = message.getKey();
    final EventChannel channel;
    if (StringUtils.isNotBlank(key)) {
      channel = lbFactory.chooseServer(BUS_BROKER_APP_NAME, key, LbStrategyEnum.CONSISTENT_HASH);
    } else {
      channel = lbFactory.chooseServer(BUS_BROKER_APP_NAME, message.getTopic(), LbStrategyEnum.ROUND_ROBIN);
    }
    if (channel == null) {
      throw new RuntimeException("选取channel为空");
    }
    return channel;
  }

  @Override
  protected void idleNotice() {
    List<EventChannel> channelList = lbFactory.getReachableServers(BUS_BROKER_APP_NAME);
    Flux.fromIterable(channelList)
        .flatMap(channel -> channel.changeStates(ChannelInfo.STATUS_IDLE))
        .collectList()
        .subscribe();
  }

  @Override
  protected void busyNotice() {
    List<EventChannel> channelList = lbFactory.getReachableServers(BUS_BROKER_APP_NAME);
    Flux.fromIterable(channelList)
        .flatMap(channel -> channel.changeStates(ChannelInfo.STATUS_BUSY))
        .collectList()
        .subscribe();
  }

  @Nonnull
  @Override
  public Mono<PublishResult> publish(@Nonnull EventMessage<?> message) {
    final EventChannel channel = getBusChannel(message);
    return channel.publishEvent(message);
  }

  @Nonnull
  @Override
  public Flux<PublishResult> batchPublish(@Nonnull Collection<EventMessage<?>> messages) {
//    int batchSize = messages.size();
//    if (batchSize > 100) {
//      String message = String.format("超过批量发布上限100 -> %s", batchSize);
//      log.error(message);
//      return Flux.error(new RuntimeException(message));
//    }
    return Flux.fromIterable(messages).flatMap(this::publish);
  }
}
