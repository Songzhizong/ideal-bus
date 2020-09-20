package com.zzsong.bus.client;

import com.zzsong.bus.client.rsocket.RSocketBusChannel;
import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import com.zzsong.bus.common.message.EventMessage;
import com.zzsong.bus.common.message.PublishResult;
import com.zzsong.bus.common.transfer.AutoSubscribeArgs;
import com.zzsong.bus.common.transfer.SubscriptionArgs;
import com.zzsong.bus.receiver.BusReceiver;
import com.zzsong.bus.receiver.listener.IEventListener;
import com.zzsong.bus.receiver.listener.ListenerFactory;
import com.zzsong.common.loadbalancer.LbFactory;
import com.zzsong.common.loadbalancer.LbStrategyEnum;
import com.zzsong.common.loadbalancer.SimpleLbFactory;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/19 11:43 下午
 */
public class SimpleBusClient implements BusClient {
  private static final Logger log = LoggerFactory.getLogger(SimpleBusClient.class);
  public static final String BUS_BROKER_APP_NAME = "busBroker";
  private final BusReceiver busReceiver;
  private final LbFactory<BusChannel> lbFactory = new SimpleLbFactory<>();

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

  public SimpleBusClient(BusReceiver busReceiver) {
    this.busReceiver = busReceiver;
  }

  public void startClient() {
    if (StringUtils.isBlank(brokerAddresses)) {
      log.error("brokerAddresses为空...");
      return;
    }
    // 初始化和broker之间的连接
    final List<BusChannel> channels = initChannel();
    // 自动注册
    if (!channels.isEmpty()) {
      autoSubscrib(channels.get(0));
    }
  }

  @Nonnull
  private List<BusChannel> initChannel() {
    final String[] addresses = StringUtils
        .split(brokerAddresses, ",");
    List<BusChannel> busChannels = new ArrayList<>();
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
      RSocketBusChannel channel = new RSocketBusChannel(
          ip, port, applicationId, clientIpPort, this, lbFactory);
      channel.setAccessToken(accessToken);
      channel.startChannel();
      busChannels.add(channel);
    }
    return busChannels;
  }

  private void autoSubscrib(@Nonnull BusChannel busChannel) {
    final Map<String, Map<String, IEventListener>> all = ListenerFactory.getAll();
    final AutoSubscribeArgs autoSubscribeArgs = new AutoSubscribeArgs();
    autoSubscribeArgs.setApplicationId(applicationId);
    Set<String> topicSet = all.keySet();
    final List<SubscriptionArgs> subscriptionArgsList = topicSet.stream()
        .map(topic -> {
          final SubscriptionArgs subscriptionArgs = new SubscriptionArgs();
          subscriptionArgs.setTopic(topic);
          return subscriptionArgs;
        }).collect(Collectors.toList());
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
  private BusChannel getBusChannel(@Nonnull EventMessage<?> message) {
    final String key = message.getKey();
    final BusChannel channel;
    if (StringUtils.isNotBlank(key)) {
      channel = lbFactory.chooseServer(BUS_BROKER_APP_NAME, key, LbStrategyEnum.CONSISTENT_HASH);
    } else {
      channel = lbFactory.chooseServer(BUS_BROKER_APP_NAME, message.getTopic());
    }
    if (channel == null) {
      throw new RuntimeException("选取channel为空");
    }
    return channel;
  }

  @Nonnull
  @Override
  public Mono<PublishResult> publish(@Nonnull EventMessage<?> message) {
    final BusChannel channel = getBusChannel(message);
    return channel.publishEvent(message);
  }

  @Nonnull
  @Override
  public Flux<PublishResult> batchPublish(@Nonnull Collection<EventMessage<?>> messages) {
    return Flux.fromIterable(messages).flatMap(this::publish);
  }

  @Nonnull
  @Override
  public Mono<DeliveredResult> receive(@Nonnull DeliveredEvent event) {
    return busReceiver.receive(event);
  }
}
