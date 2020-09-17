package com.zzsong.bus.core.processor;

import com.zzsong.bus.abs.converter.SubscriptionConverter;
import com.zzsong.bus.abs.domain.Event;
import com.zzsong.bus.abs.domain.Subscriber;
import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.core.admin.service.EventService;
import com.zzsong.bus.core.admin.service.SubscriberService;
import com.zzsong.bus.core.admin.service.SubscriptionService;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.core.config.BusProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 本地缓存
 * <p>缓存订阅信息</p>
 *
 * @author 宋志宗 on 2020/9/17
 */
@Component
public class LocalCache implements InitializingBean, DisposableBean {
  private static final Logger log = LoggerFactory.getLogger(LocalCache.class);
  private ScheduledExecutorService scheduledExecutor;
  @Nonnull
  private final BusProperties properties;
  @Nonnull
  private final EventService eventService;
  @Nonnull
  private final SubscriberService subscriberService;
  @Nonnull
  private final SubscriptionService subscriptionService;

  public LocalCache(@Nonnull BusProperties properties,
                    @Nonnull EventService eventService,
                    @Nonnull SubscriberService subscriberService,
                    @Nonnull SubscriptionService subscriptionService) {
    this.properties = properties;
    this.eventService = eventService;
    this.subscriberService = subscriberService;
    this.subscriptionService = subscriptionService;
  }

  private Thread refreshCacheThread;
  private boolean startRefreshCacheThread = true;
  private final BlockingQueue<Boolean> refreshCacheSignQueue = new ArrayBlockingQueue<>(1);

  // ------------------------------ 缓存相关数据 ~ ~ ~

  private Map<String, Event> eventMapping = new HashMap<>();
  private Map<Long, Subscriber> subscriberMapping = new HashMap<>();
  private Map<String, List<SubscriptionDetails>> subscriptionMapping = new HashMap<>();

  /**
   * 通知当前节点刷新本地缓存
   */
  public void refreshCache() {
    refreshCacheSignQueue.offer(true);
  }

  /**
   * 获取主题的订阅关系
   *
   * @param topic 主题
   * @return 订阅关系列表
   */
  @Nonnull
  public List<SubscriptionDetails> getTopicSubscription(@Nonnull String topic) {
    List<SubscriptionDetails> detailsList = subscriptionMapping.get(topic);
    if (detailsList == null) {
      return Collections.emptyList();
    }
    return detailsList;
  }

  /**
   * 获取event详情
   *
   * @param topic 主题
   * @return event信息
   */
  @Nullable
  public Event getEvent(@Nonnull String topic) {
    return eventMapping.get(topic);
  }

  @Override
  public void afterPropertiesSet() {
    refreshLocalCache().subscribe();
    Duration duration = properties.getRefreshLocalCacheInterval();
    long interval = Math.max(duration.toMillis(), 60 * 1000);
    scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    scheduledExecutor.scheduleAtFixedRate(() -> refreshLocalCache().subscribe(),
        interval, interval, TimeUnit.MILLISECONDS);
    refreshCacheThread = new Thread(() -> {
      while (startRefreshCacheThread) {
        Boolean poll;
        try {
          poll = refreshCacheSignQueue.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          log.info("refreshCacheThread interrupted");
          break;
        }
        if (poll != null) {
          refreshLocalCache().subscribe();
        }
      }
    });
    refreshCacheThread.setDaemon(true);
    refreshCacheThread.start();
  }

  @Nonnull
  private Mono<Boolean> refreshLocalCache() {
    Mono<List<Event>> eventListMono = eventService.findAll();
    Mono<List<Subscriber>> subscriberListMono = subscriberService.findAll();
    Mono<List<Subscription>> enabledSubscriptionMono = subscriptionService.findAllEnabled();
    return Mono.zip(eventListMono, subscriberListMono, enabledSubscriptionMono)
        .map(tuple3 -> {
          List<Event> events = tuple3.getT1();
          List<Subscriber> subscribers = tuple3.getT2();
          List<Subscription> subscriptions = tuple3.getT3();
          this.eventMapping = events.stream()
              .collect(Collectors.toMap(Event::getTopic, e -> e));
          this.subscriberMapping = subscribers.stream()
              .collect(Collectors.toMap(Subscriber::getSubscriberId, s -> s));
          this.subscriptionMapping = subscriptions.stream()
              .map(s -> {
                long subscriberId = s.getSubscriberId();
                Subscriber subscriber = this.subscriberMapping.get(subscriberId);
                if (subscriber == null) {
                  return null;
                }
                SubscriptionDetails details = SubscriptionConverter.toSubscriptionDetails(s);
                details.setSubscriberType(subscriber.getSubscriberType());
                details.setApplication(subscriber.getApplication());
                details.setReceiveUrl(subscriber.getReceiveUrl());
                return details;
              }).filter(Objects::nonNull)
              .collect(Collectors.groupingBy(SubscriptionDetails::getTopic));
          return true;
        });
  }

  @Override
  public void destroy() {
    refreshCacheThread.interrupt();
    startRefreshCacheThread = false;
    scheduledExecutor.shutdown();
  }
}
