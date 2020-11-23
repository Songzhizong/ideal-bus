package com.zzsong.bus.core.config;

import com.zzsong.bus.abs.domain.Event;
import com.zzsong.bus.core.admin.service.EventInstanceService;
import com.zzsong.bus.core.admin.service.EventService;
import com.zzsong.bus.core.admin.service.RouteInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 宋志宗 on 2020/11/20
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledConfig implements InitializingBean {
  @Nonnull
  private static List<Event> deletableEvents = new ArrayList<>();
  @Nonnull
  private final BusProperties properties;
  @Nonnull
  private final EventService eventService;
  @Nonnull
  private final StringRedisTemplate redisTemplate;
  @Nonnull
  private final EventInstanceService eventInstanceService;
  @Nonnull
  private final RouteInstanceService routeInstanceService;

  @Override
  public void afterPropertiesSet() {
    loadDeletableEvents();
  }

  @Scheduled(initialDelay = 60 * 60 * 1000, fixedDelay = 60 * 60 * 1000)
  public void reloadDeletableEvents() {
    loadDeletableEvents();
  }

  /**
   * 定时删除过期的事件实例
   */
  @Scheduled(initialDelay = 15 * 60 * 1000, fixedDelay = 15 * 60 * 1000)
  public void deleteExpireEvents() {
    String deleteExpireEventLock = "ideal:bus:timer:event:expire";
    Boolean lock = redisTemplate.opsForValue()
        .setIfAbsent(deleteExpireEventLock, "1", Duration.ofMinutes(10));
    if (lock == null || !lock) {
      log.debug("删除过期事件任务已在其他节点进行");
      return;
    }
    for (Event event : deletableEvents) {
      long expire = event.getExpire();
      if (expire > 0) {
        long time = System.currentTimeMillis() - expire;
        eventInstanceService.deleteAllByTopicAndTimestampLte(event.getTopic(), time).block();
      }
    }
  }

  /**
   * 定期删除过期的路由实例
   */
  @Scheduled(initialDelay = 10 * 1000, fixedDelay = 15 * 60 * 1000)
  public void deleteExpireRouteInstance() {
    String deleteExpireEventLock = "ideal:bus:timer:route:expire";
    Boolean lock = redisTemplate.opsForValue()
        .setIfAbsent(deleteExpireEventLock, "1", Duration.ofMinutes(10));
    if (lock == null || !lock) {
      log.debug("删除过期路由记录任务已在其他节点进行");
      return;
    }
    Duration routeInstanceExpire = properties.getRouteInstanceExpire();
    long millis = routeInstanceExpire.toMillis();
    long time = System.currentTimeMillis() - millis;
    Long block = routeInstanceService.deleteAllSucceedByCreateTimeLessThan(time).block();
    if (block != null && block > 0) {
      log.info("移除 {} 条过期路由实例, 过期天数为: {}", block, routeInstanceExpire.toDays());
    }
  }

  public void loadDeletableEvents() {
    List<Event> eventList = eventService.findDeletableEvents().block();
    if (eventList != null) {
      ScheduledConfig.deletableEvents = eventList;
    }
  }
}