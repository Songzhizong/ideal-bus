package com.zzsong.bus.broker.config;

import com.zzsong.bus.abs.generator.ReactiveRedisSnowFlakeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Slf4j
@Configuration
public class BusBeanConfig {

  @Value("${spring.application.name}")
  private String applicationName;

  @Bean
  public ReactiveRedisSnowFlakeFactory reactiveRedisSnowFlakeFactory(
      ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
    return new ReactiveRedisSnowFlakeFactory(applicationName, reactiveStringRedisTemplate);
  }
}
