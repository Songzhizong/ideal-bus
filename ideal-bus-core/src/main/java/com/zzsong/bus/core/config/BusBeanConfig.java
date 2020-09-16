package com.zzsong.bus.core.config;

import com.zzsong.bus.common.generator.ReactiveRedisSnowFlakeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Configuration
public class BusBeanConfig {
  private static final Logger log = LoggerFactory.getLogger(BusBeanConfig.class);

  @Value("${spring.application.name}")
  private String applicationName;
  
  @Bean
  public ReactiveRedisSnowFlakeFactory reactiveRedisSnowFlakeFactory(
      ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
    ReactiveRedisSnowFlakeFactory snowFlakeFactory
        = new ReactiveRedisSnowFlakeFactory(applicationName, reactiveStringRedisTemplate);
    log.debug("Test SnowFlake: {}", snowFlakeFactory.generate());
    return snowFlakeFactory;
  }
}
