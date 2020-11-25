package com.zzsong.bus.broker.admin.service;

import com.zzsong.bus.broker.core.LocalCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/21
 */
@Slf4j
@Service
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class CacheService implements MessageListener {
  public static final String BUS_REFRESH_CACHE = "bus_refresh_cache";

  @Nonnull
  @Autowired
  private LocalCache localCache;

  private final ReactiveStringRedisTemplate template;

  public CacheService(ReactiveStringRedisTemplate template) {
    this.template = template;
  }

  /**
   * 通知各个节点更新缓存
   */
  public Mono<Long> notificationRefreshCache() {
    return template.convertAndSend(BUS_REFRESH_CACHE, "1");
  }

  public Mono<Boolean> refreshLocalCache() {
    localCache.refreshCache();
    return Mono.just(true);
  }

  @Override
  public void onMessage(@Nonnull Message message, byte[] bytes) {
    log.info("接收到刷新缓存消息: {} -> {}", new String(bytes), message.toString());
    localCache.refreshCache();
  }
}
