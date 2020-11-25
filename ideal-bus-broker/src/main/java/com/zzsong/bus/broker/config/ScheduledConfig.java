package com.zzsong.bus.broker.config;

import com.zzsong.bus.broker.admin.service.RouteInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * @author 宋志宗 on 2020/11/20
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledConfig {
  @Nonnull
  private final BusProperties properties;
  @Nonnull
  private final StringRedisTemplate redisTemplate;
  @Nonnull
  private final RouteInstanceService routeInstanceService;

  /**
   * 定期删除过期的路由实例
   */
  @Scheduled(initialDelay = 15 * 60 * 1000, fixedDelay = 15 * 60 * 1000)
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
    long currentTimeMillis = System.currentTimeMillis();
    long time = currentTimeMillis - millis;
    Long block = routeInstanceService.deleteAllSucceedByCreateTimeLessThan(time).block();
    if (block != null && block > 0) {
      log.info("移除 {} 条过期路由实例, 过期天数为: {}, 执行耗时: {}",
          block, routeInstanceExpire.toDays(), System.currentTimeMillis() - currentTimeMillis);
    }
  }
}