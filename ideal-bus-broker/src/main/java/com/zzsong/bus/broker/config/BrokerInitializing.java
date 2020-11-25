package com.zzsong.bus.broker.config;

import com.zzsong.bus.broker.core.LocalCache;
import com.zzsong.bus.broker.core.RouteTransfer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗 on 2020/9/20 5:52 下午
 */
@Slf4j
@Configuration
public class BrokerInitializing implements DisposableBean, InitializingBean, ApplicationRunner {
  private final String key;
  private final int nodeId;
  @Nonnull
  private final BusBeanConfig busConfig;
  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final RouteTransfer blockingDequeRouteTransfer;
  @Nonnull
  private final ReactiveStringRedisTemplate redisTemplate;

  public BrokerInitializing(@Nonnull BusBeanConfig busConfig,
                            @Nonnull LocalCache localCache,
                            @Nonnull BusProperties busProperties,
                            @Nonnull RouteTransfer blockingDequeRouteTransfer,
                            @Nonnull ReactiveStringRedisTemplate redisTemplate) {
    this.busConfig = busConfig;
    this.localCache = localCache;
    this.blockingDequeRouteTransfer = blockingDequeRouteTransfer;
    this.redisTemplate = redisTemplate;
    this.nodeId = busProperties.getNodeId();
    this.key = "ideal:register:bus:broker:node:" + nodeId;
  }

  @Override
  public void destroy() {
    log.debug("release node id: {}", this.key);
    redisTemplate.delete(this.key).block();
  }

  @Override
  public void afterPropertiesSet() {
    if (nodeId < 1) {
      log.error("nodeId不合法, 必须大于0且不重复: {}", nodeId);
      System.exit(0);
    }
    final Boolean block = redisTemplate.opsForValue()
        .setIfAbsent(this.key, "1", Duration.ofMinutes(1))
        .block();
    if (block == null || !block) {
      log.error("nodeId重复: {} ", nodeId);
      System.exit(0);
    }
    Executors.newSingleThreadScheduledExecutor()
        .scheduleAtFixedRate(() -> redisTemplate
                .opsForValue()
                .set(this.key, "1", Duration.ofMinutes(1))
                .doOnError(throwable -> {
                  String errMsg = throwable.getClass().getName() +
                      ": " + throwable.getMessage();
                  log.error("NodeId automatically renewed exception: {}",
                      errMsg);
                }).subscribe(),
            15, 15, TimeUnit.SECONDS);

  }

  @Override
  public void run(ApplicationArguments args) {
    localCache.init();
    blockingDequeRouteTransfer.init();
    busConfig.setInitialized(true);
  }
}
