package com.zzsong.bus.broker.core.consumer;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.storage.RouteInstanceStorage;
import com.zzsong.bus.broker.core.channel.Channel;
import com.zzsong.bus.common.share.lb.LbStrategyEnum;
import com.zzsong.bus.common.share.lb.LoadBalancer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2021/5/19
 */
@Slf4j
public class SimpleConsumer implements Consumer {
  /** 忙碌状态channel回到可用状态的恢复时间 */
  private static final int RECOVER_MILLS = 30_000;
  private static final ScheduledExecutorService SCHEDULED
      = Executors.newSingleThreadScheduledExecutor();
  private final Map<String, Channel> availableChannelMap = new HashMap<>();
  private final Map<String, BusyChannelWrapper> busyChannelMap = new HashMap<>();
  private final LoadBalancer<Channel> loadBalancer
      = LoadBalancer.newLoadBalancer(LbStrategyEnum.RANDOM);
  private List<Channel> availableChannels = new ArrayList<>();
  private final long applicationId;
  private final RouteInstanceStorage routeInstanceStorage;

  public SimpleConsumer(long applicationId,
                        RouteInstanceStorage routeInstanceStorage) {
    this.applicationId = applicationId;
    this.routeInstanceStorage = routeInstanceStorage;
    SCHEDULED.schedule(() -> {
      long currentTimeMillis = System.currentTimeMillis();
      List<String> collect = busyChannelMap.values().stream()
          .filter(wrapper -> currentTimeMillis - wrapper.getTimestamp() >= RECOVER_MILLS)
          .map(wrapper -> wrapper.getChannel().getInstanceId())
          .collect(Collectors.toList());
      markChannelsAvailable(collect);
    }, 2, TimeUnit.SECONDS);
  }

  @Override
  public long getApplicationId() {
    return applicationId;
  }

  @Nonnull
  @Override
  public Mono<DeliverStatus> deliverMessage(@Nonnull RouteInstance routeInstance) {
    synchronized (this) {
      if (availableChannels.isEmpty()) {
        if (busyChannelMap.isEmpty()) {
          return Mono.just(DeliverStatus.OFFLINE);
        }
        return Mono.just(DeliverStatus.UNREACHABLE);
      }
    }
    Channel channel = loadBalancer.chooseServer(null, availableChannels);
    if (channel == null) {
      return Mono.just(DeliverStatus.UNREACHABLE);
    }
    return channel.deliverMessage(routeInstance)
        .onErrorResume(throwable -> {
          log.info("channel.deliverMessage(routeInstance) ex: {}", throwable.getMessage());
          return Mono.just(DeliverStatus.CHANNEL_BUSY);
        })
        .flatMap(status -> {
          if (status != DeliverStatus.SUCCESS) {
            String channelId = channel.getInstanceId();
            markChannelBusy(channelId);
            return Mono.just(status);
          } else {
            routeInstance.setStatus(RouteInstance.STATUS_RUNNING);
            routeInstance.setMessage("running");
            return routeInstanceStorage.save(routeInstance).map(r -> status);
          }
        });
  }

  @Override
  public synchronized void addChannel(@Nonnull Channel channel) {
    String instanceId = channel.getInstanceId();
    availableChannelMap.put(instanceId, channel);
    this.availableChannels = new ArrayList<>(availableChannelMap.values());
  }

  @Override
  public synchronized void removeChannel(@Nonnull Channel channel) {
    String instanceId = channel.getInstanceId();
    availableChannelMap.remove(instanceId);
    this.availableChannels = new ArrayList<>(availableChannelMap.values());
    busyChannelMap.remove(instanceId);
  }

  @Override
  public synchronized void markChannelBusy(@Nonnull String channelId) {
    Channel channel = availableChannelMap.remove(channelId);
    if (channel != null) {
      log.info("channel: {} busy", channelId);
      this.availableChannels = new ArrayList<>(availableChannelMap.values());
      BusyChannelWrapper wrapper = new BusyChannelWrapper(System.currentTimeMillis(), channel);
      busyChannelMap.put(channelId, wrapper);
    }
  }

  @Override
  public synchronized void markChannelsAvailable(@Nonnull Collection<String> channelIds) {
    int change = 0;
    for (String channelId : channelIds) {
      BusyChannelWrapper wrapper = busyChannelMap.remove(channelId);
      if (wrapper != null) {
        log.info("change channel: {} available", channelId);
        change++;
        Channel channel = wrapper.getChannel();
        availableChannelMap.put(channelId, channel);
      }
    }
    if (change > 0) {
      this.availableChannels = new ArrayList<>(availableChannelMap.values());
    }
  }

  @Getter
  @Setter
  @AllArgsConstructor
  private static class BusyChannelWrapper {
    private long timestamp;
    private Channel channel;
  }
}
