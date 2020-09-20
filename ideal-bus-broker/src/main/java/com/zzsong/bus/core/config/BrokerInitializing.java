package com.zzsong.bus.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Configuration
public class BrokerInitializing implements InitializingBean {
  private static final Logger log = LoggerFactory.getLogger(BrokerInitializing.class);
  @Nonnull
  private final BusProperties busProperties;
  @Nonnull
  private final ReactiveStringRedisTemplate redisTemplate;

  public BrokerInitializing(@Nonnull BusProperties busProperties,
                            @Nonnull ReactiveStringRedisTemplate redisTemplate) {
    this.busProperties = busProperties;
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void afterPropertiesSet() {
    final int nodeId = busProperties.getNodeId();
    if (nodeId < 1) {
      log.error("nodeId不合法, 必须大于0且不重复: {}", nodeId);
      System.exit(0);
    }
    String key = "ideal:register:bus:broker:node:" + nodeId;
    final Boolean block = redisTemplate.opsForValue()
        .setIfAbsent(key, "1", Duration.ofMinutes(5))
        .block();
    if (block == null || !block) {
      log.error("nodeId重复: {} ", nodeId);
      System.exit(0);
    }
    Executors.newSingleThreadScheduledExecutor()
        .scheduleAtFixedRate(() -> redisTemplate
                .expire(key, Duration.ofMinutes(5))
                .doOnError(throwable -> {
                  String errMsg = throwable.getClass().getName() +
                      ": " + throwable.getMessage();
                  log.error("NodeId automatically renewed exception: {}",
                      errMsg);
                }).subscribe(),
            1, 1, TimeUnit.MINUTES);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> redisTemplate.delete(key).block()));
  }
}
