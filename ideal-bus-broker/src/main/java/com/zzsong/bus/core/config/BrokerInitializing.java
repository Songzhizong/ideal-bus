package com.zzsong.bus.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
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
public class BrokerInitializing implements DisposableBean {
  private final String key;
  @Nonnull
  private final ReactiveStringRedisTemplate redisTemplate;

  public BrokerInitializing(@Nonnull BusProperties busProperties,
                            @Nonnull ReactiveStringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
    final int nodeId = busProperties.getNodeId();
    if (nodeId < 1) {
      log.error("nodeId不合法, 必须大于0且不重复: {}", nodeId);
      System.exit(0);
    }
    this.key = "ideal:register:bus:broker:node:" + nodeId;
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
  public void destroy() {
    redisTemplate.delete(this.key).block();
  }
}
