package com.zzsong.bus.broker.core.consumer;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.storage.RouteInstanceStorage;
import com.zzsong.bus.broker.core.channel.Channel;
import com.zzsong.bus.common.share.lb.LbStrategyEnum;
import com.zzsong.bus.common.share.lb.LoadBalancer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2021/5/19
 */
@Slf4j
public class SimpleConsumer implements Consumer {
  /** 忙碌状态channel回到可用状态的恢复时间 */
  private static final int RECOVER_MILLS = 30_000;
  private static final ScheduledExecutorService SCHEDULED
      = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
  private final LoadBalancer<Channel> loadBalancer
      = LoadBalancer.newLoadBalancer(LbStrategyEnum.RANDOM);
  private final ChannelRegistry registry = new ChannelRegistry();

  private final long applicationId;
  private final RouteInstanceStorage routeInstanceStorage;

  public SimpleConsumer(long applicationId,
                        RouteInstanceStorage routeInstanceStorage) {
    this.applicationId = applicationId;
    this.routeInstanceStorage = routeInstanceStorage;
    SCHEDULED.schedule(() -> {
      long currentTimeMillis = System.currentTimeMillis();
      List<String> collect = registry.getBusyChannels().stream()
          .filter(wrapper -> currentTimeMillis - wrapper.getTimestamp() >= RECOVER_MILLS)
          .map(wrapper -> wrapper.getChannel().getChannelId())
          .collect(Collectors.toList());
      if (!collect.isEmpty()) {
        markChannelsAvailable(collect);
      }
    }, 5, TimeUnit.SECONDS);
  }

  @Override
  public long getApplicationId() {
    return applicationId;
  }

  @Nonnull
  @Override
  public Mono<DeliverStatus> deliverMessage(@Nonnull RouteInstance routeInstance) {
    AvailableResult available = registry.getAvailable();
    ConsumerStatus consumerStatus = available.getStatus();
    if (consumerStatus == ConsumerStatus.OFFLINE) {
      return Mono.just(DeliverStatus.OFFLINE);
    } else if (consumerStatus == ConsumerStatus.UNREACHABLE) {
      return Mono.just(DeliverStatus.UNREACHABLE);
    }
    String topic = routeInstance.getTopic();
    Channel channel = loadBalancer.chooseServer(topic, available.getChannels());
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
            String channelId = channel.getChannelId();
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
  public void addChannel(@Nonnull Channel channel) {
    registry.addChannel(channel);
  }

  @Override
  public void removeChannel(@Nonnull Channel channel) {
    registry.removeChannel(channel);
  }

  @Override
  public void markChannelBusy(@Nonnull String channelId) {
    registry.markChannelBusy(channelId);
  }

  @Override
  public void markChannelsAvailable(@Nonnull Collection<String> channelIds) {
    registry.markChannelsAvailable(channelIds);
  }

  @Getter
  @Setter
  @AllArgsConstructor
  private static class BusyChannelWrapper {
    private long timestamp;
    private Channel channel;
  }

  private static class ChannelRegistry {
    private final Map<String, Channel> availableChannelMap = new HashMap<>();
    private final Map<String, BusyChannelWrapper> busyChannelMap = new HashMap<>();
    private List<Channel> availableChannels = new ArrayList<>();
    private final Lock lock = new ReentrantLock();

    @Nonnull
    public AvailableResult getAvailable() {
      lock.lock();
      try {
        if (availableChannels.isEmpty()) {
          if (busyChannelMap.isEmpty()) {
            return new AvailableResult(ConsumerStatus.OFFLINE, Collections.emptyList());
          }
          return new AvailableResult(ConsumerStatus.UNREACHABLE, Collections.emptyList());
        }
        if (log.isDebugEnabled()) {
          String join = this.availableChannels.stream()
              .map(Channel::getChannelId)
              .collect(Collectors.joining(", "));
          log.debug("available channels: {}", join);
        }
        return new AvailableResult(ConsumerStatus.NORMAL, this.availableChannels);
      } finally {
        lock.unlock();
      }
    }

    @Nonnull
    public List<BusyChannelWrapper> getBusyChannels() {
      lock.lock();
      try {
        Collection<BusyChannelWrapper> values = busyChannelMap.values();
        if (log.isDebugEnabled()) {
          String join = values.stream()
              .map(wr -> wr.getChannel().getChannelId())
              .collect(Collectors.joining(", "));
          log.debug("busy channels: {}", join);
        }
        return new ArrayList<>(values);
      } finally {
        lock.unlock();
      }
    }

    public void addChannel(@Nonnull Channel channel) {
      lock.lock();
      try {
        String channelId = channel.getChannelId();
        availableChannelMap.put(channelId, channel);
        log.info("add channel: {}", channelId);
        this.availableChannels = new ArrayList<>(availableChannelMap.values());
      } finally {
        lock.unlock();
      }
    }

    public void removeChannel(@Nonnull Channel channel) {
      lock.lock();
      try {
        String channelId = channel.getChannelId();
        availableChannelMap.remove(channelId);
        this.availableChannels = new ArrayList<>(availableChannelMap.values());
        busyChannelMap.remove(channelId);
        log.info("remove channel: {}", channelId);
      } finally {
        lock.unlock();
      }
    }

    public void markChannelBusy(@Nonnull String channelId) {
      lock.lock();
      try {
        Channel channel = availableChannelMap.remove(channelId);
        if (channel != null) {
          log.debug("channel: {} busy", channelId);
          this.availableChannels = new ArrayList<>(availableChannelMap.values());
          BusyChannelWrapper wrapper = new BusyChannelWrapper(System.currentTimeMillis(), channel);
          busyChannelMap.put(channelId, wrapper);
        }
      } finally {
        lock.unlock();
      }
    }

    public void markChannelsAvailable(@Nonnull Collection<String> channelIds) {
      lock.lock();
      try {
        int change = 0;
        for (String channelId : channelIds) {
          BusyChannelWrapper wrapper = busyChannelMap.remove(channelId);
          if (wrapper != null) {
            log.debug("change channel: {} available", channelId);
            change++;
            Channel channel = wrapper.getChannel();
            availableChannelMap.put(channelId, channel);
          }
        }
        if (change > 0) {
          this.availableChannels = new ArrayList<>(availableChannelMap.values());
        }
      } finally {
        lock.unlock();
      }
    }
  }

  private enum ConsumerStatus {
    NORMAL,
    OFFLINE,
    UNREACHABLE,
  }

  @Getter
  @RequiredArgsConstructor
  private static class AvailableResult {
    private final ConsumerStatus status;
    private final List<Channel> channels;
  }
}
