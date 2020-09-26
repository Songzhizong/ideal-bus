package com.zzsong.bus.core.config;

import com.zzsong.bus.core.admin.service.CacheService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/26 8:10 下午
 */
@Configuration
public class RedisPubSubConfig {
  private final StringRedisTemplate stringRedisTemplate;

  public RedisPubSubConfig(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Bean
  public RedisMessageListenerContainer redisContainer(@Nonnull CacheService cacheService) {
    RedisConnectionFactory connectionFactory
        = stringRedisTemplate.getConnectionFactory();
    RedisMessageListenerContainer container
        = new RedisMessageListenerContainer();
    assert connectionFactory != null;
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(cacheService, new ChannelTopic(CacheService.refreshCacheTopic));
    return container;
  }
}
