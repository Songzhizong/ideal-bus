package com.zzsong.bus.broker.port.scheduling;

import com.zzsong.bus.abs.generator.SnowFlake;
import com.zzsong.bus.abs.storage.EventInstanceStorage;
import com.zzsong.bus.abs.storage.RouteInstanceStorage;
import com.zzsong.bus.broker.admin.service.RouteInstanceService;
import com.zzsong.bus.broker.config.BusProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * @author 宋志宗 on 2021/5/26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrokerScheduler {
  private final BusProperties properties;
  private final EventInstanceStorage eventInstanceStorage;
  private final RouteInstanceStorage routeInstanceStorage;
  private final RouteInstanceService routeInstanceService;

  /**
   * 定期删除过期的事件实例
   */
  @Scheduled(initialDelay = 60 * 60_000, fixedDelay = 60 * 60_000)
  public void deleteExpireEventInstance() {
    boolean enableExpireScheduler = properties.isEnableStorageScheduling();
    if (!enableExpireScheduler) {
      return;
    }
    List<BusProperties.EventInstanceExpire> expires = properties.getEventInstanceExpires();
    for (BusProperties.EventInstanceExpire expire : expires) {
      Set<String> topics = expire.getTopics();
      if (topics == null || topics.isEmpty()) {
        continue;
      }
      Duration expireDuration = expire.getExpire();
      long expireMillis = expireDuration.toMillis();
      long currentTimeMillis = System.currentTimeMillis();
      long time = currentTimeMillis - expireMillis;
      long maxId = SnowFlake.calculateMinId(time);
      eventInstanceStorage.deleteByIdLessThenAndTopicIn(maxId, topics)
          .subscribe(count -> {
            if (count > 0) {
              String join = StringUtils.join(topics, ", ");
              long consuming = System.currentTimeMillis() - currentTimeMillis;
              log.info("从 {} 移除 {} 条过期事件实例, 过期天数为: {}, 执行耗时: {}",
                  join, count, expireDuration.toDays(), consuming);
            }
          });
    }
  }

  /**
   * 定期删除过期的路由实例
   */
  @Scheduled(initialDelay = 60 * 60_000, fixedDelay = 60 * 60_000)
  public void deleteExpireRouteInstance() {
    boolean enableExpireScheduler = properties.isEnableStorageScheduling();
    if (!enableExpireScheduler) {
      return;
    }
    Duration routeInstanceExpire = properties.getRouteInstanceExpire();
    long millis = routeInstanceExpire.toMillis();
    long currentTimeMillis = System.currentTimeMillis();
    long time = currentTimeMillis - millis;
    routeInstanceService.deleteAllSucceedByCreateTimeLessThan(time)
        .subscribe(count -> {
          if (count > 0) {
            log.info("移除 {} 条过期路由实例, 过期天数为: {}, 执行耗时: {}",
                count, routeInstanceExpire.toDays(), System.currentTimeMillis() - currentTimeMillis);
          }
        });
  }


  /**
   * 定期将长时间处在running状态的消息修改为等待状态以尝试重新推送
   */
  @Scheduled(initialDelay = 10_000, fixedDelay = 10_000)
  public void updateRunningToDelaying() {
    boolean enableExpireScheduler = properties.isEnableStorageScheduling();
    if (!enableExpireScheduler) {
      return;
    }
    long currentTimeMillis = System.currentTimeMillis();
    long maxRunningStatusTime = currentTimeMillis - 60_000;
    long nextPushTime = currentTimeMillis + 10_000;
    routeInstanceStorage.updateRunningToDelaying(maxRunningStatusTime, nextPushTime)
        .subscribe(count -> {
          if (count > 0) {
            log.info("更新 {} 条running 消息状态为 delaying", count);
          }
        });
  }
}
