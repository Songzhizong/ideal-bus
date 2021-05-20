package com.zzsong.bus.broker.core;

import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.broker.admin.service.SubscriptionService;
import com.zzsong.bus.broker.config.BusProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 本地缓存
 * <p>缓存订阅信息</p>
 *
 * @author 宋志宗 on 2020/9/17
 */
@Slf4j
@Component
public class LocalCache implements InitializingBean, DisposableBean {
  @Nonnull
  private final BusProperties properties;
  @Nonnull
  @Autowired
  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  private SubscriptionService subscriptionService;

  public LocalCache(@Nonnull BusProperties properties) {
    this.properties = properties;
  }

  private Thread refreshCacheThread;
  private boolean startRefreshCacheThread = true;
  private final BlockingQueue<Boolean> refreshCacheSignQueue = new ArrayBlockingQueue<>(1);

  // ------------------------------ 缓存相关数据 ~ ~ ~

  private Map<String, List<SubscriptionDetails>> topicSubscriptionMapping = Collections.emptyMap();

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
    List<SubscriptionDetails> detailsList = topicSubscriptionMapping.get(topic);
    if (detailsList == null) {
      return Collections.emptyList();
    }
    return detailsList;
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
    return subscriptionService.findAllEnabledSubscriptionDetails()
        .map(subscriptionDetails -> {
          this.topicSubscriptionMapping = subscriptionDetails.stream()
              .collect(Collectors.groupingBy(SubscriptionDetails::getTopic));
          return true;
        });
  }

  @Override
  public void destroy() {
    startRefreshCacheThread = false;
    refreshCacheThread.interrupt();
  }

  @Override
  public void afterPropertiesSet() {
    init();
  }
}
