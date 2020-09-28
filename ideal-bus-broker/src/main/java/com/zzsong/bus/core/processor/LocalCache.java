package com.zzsong.bus.core.processor;

import com.google.common.collect.Maps;
import com.zzsong.bus.abs.converter.SubscriptionConverter;
import com.zzsong.bus.abs.domain.Event;
import com.zzsong.bus.abs.domain.Application;
import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.abs.storage.ApplicationStorage;
import com.zzsong.bus.abs.storage.EventStorage;
import com.zzsong.bus.abs.storage.SubscriptionStorage;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.core.config.BusProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
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
@Slf4j
@Component
public class LocalCache implements DisposableBean {
  @Nonnull
  private final BusProperties properties;
  @Nonnull
  private final EventStorage eventStorage;
  @Nonnull
  private final ApplicationStorage applicationService;
  @Nonnull
  private final SubscriptionStorage subscriptionService;

  public LocalCache(@Nonnull BusProperties properties,
                    @Nonnull EventStorage eventStorage,
                    @Nonnull ApplicationStorage applicationService,
                    @Nonnull SubscriptionStorage subscriptionService) {
    this.properties = properties;
    this.eventStorage = eventStorage;
    this.applicationService = applicationService;
    this.subscriptionService = subscriptionService;
  }

  private Thread refreshCacheThread;
  private boolean startRefreshCacheThread = true;
  private final BlockingQueue<Boolean> refreshCacheSignQueue = new ArrayBlockingQueue<>(1);

  // ------------------------------ 缓存相关数据 ~ ~ ~

  private Map<String, Event> eventMapping = Collections.emptyMap();
  private ConcurrentMap<Long, Application> applicationMapping = Maps.newConcurrentMap();
  private Map<Long, SubscriptionDetails> subscriptionMapping = Collections.emptyMap();
  private Map<String, List<SubscriptionDetails>> topicSubscriptionMapping = Collections.emptyMap();

  /**
   * 通知当前节点刷新本地缓存
   */
  public void refreshCache() {
    refreshCacheSignQueue.offer(true);
  }

  @Nullable
  public SubscriptionDetails getSubscription(long subscriptionId) {
    return subscriptionMapping.get(subscriptionId);
  }

  @Nonnull
  public Collection<SubscriptionDetails> getAllSubscription() {
    return subscriptionMapping.values();
  }

  /**
   * 获取主题的订阅关系
   *
   * @param topic 主题
   * @return 订阅关系列表
   */
  @Nonnull
  public List<SubscriptionDetails> getTopicSubscription(@Nonnull String topic) {
    List<SubscriptionDetails> detailsList = topicSubscriptionMapping.get(topic);
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

  @Nullable
  public Application getApplication(long applicationId) {
    return applicationMapping.get(applicationId);
  }

  public void init() {
    log.info("Init local cache...");
    refreshLocalCache().block();
    Duration duration = properties.getRefreshLocalCacheInterval();
    // 自动更新本地缓存的间隔时间至少1分钟
    long intervalSeconds = Math.max(duration.getSeconds(), 60);
    refreshCacheThread = new Thread(() -> {
      long timeout = 5L;
      long times = 0;
      while (startRefreshCacheThread) {
        Boolean poll;
        try {
          poll = refreshCacheSignQueue.poll(timeout, TimeUnit.SECONDS);
          times++;
        } catch (InterruptedException e) {
          log.info("refreshCacheThread interrupted");
          break;
        }
        if (poll != null || times >= intervalSeconds / timeout) {
          refreshLocalCache().doOnNext(b -> log.info("已更新本地缓存...")).subscribe();
          times = 0;
        }
      }
    });
    refreshCacheThread.setDaemon(true);
    refreshCacheThread.start();
    log.info("Init local cache completed.");
  }

  @Nonnull
  private Mono<Boolean> refreshLocalCache() {
    Mono<List<Event>> eventListMono = eventStorage.findAll();
    Mono<List<Application>> applicationListMono = applicationService.findAll();
    Mono<List<Subscription>> enabledSubscriptionMono = subscriptionService.findAllEnabled();
    return Mono.zip(eventListMono, applicationListMono, enabledSubscriptionMono)
        .map(tuple3 -> {
          List<Event> events = tuple3.getT1();
          List<Application> applications = tuple3.getT2();
          List<Subscription> subscriptions = tuple3.getT3();
          this.eventMapping = events.stream()
              .collect(Collectors.toMap(Event::getTopic, e -> e));
          this.applicationMapping = applications.stream()
              .collect(Collectors.toConcurrentMap(Application::getApplicationId, s -> s));
          this.subscriptionMapping = new HashMap<>();
          this.topicSubscriptionMapping = subscriptions.stream()
              .map(s -> {
                long applicationId = s.getApplicationId();
                Application application = this.applicationMapping.get(applicationId);
                if (application == null) {
                  return null;
                }
                SubscriptionDetails details = SubscriptionConverter.toSubscriptionDetails(s);
                details.setApplicationType(application.getApplicationType());
                details.setExternalApp(application.getExternalApp());
                details.setReceiveUrl(application.getReceiveUrl());
                subscriptionMapping.put(s.getSubscriptionId(), details);
                return details;
              }).filter(Objects::nonNull)
              .collect(Collectors.groupingBy(SubscriptionDetails::getTopic));
          return true;
        });
  }

  @Override
  public void destroy() {
    startRefreshCacheThread = false;
    refreshCacheThread.interrupt();
  }
}
