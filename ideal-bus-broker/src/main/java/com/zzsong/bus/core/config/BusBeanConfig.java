package com.zzsong.bus.core.config;

import com.zzsong.bus.abs.generator.ReactiveRedisSnowFlakeFactory;
import com.zzsong.bus.core.admin.service.RouteInstanceService;
import com.zzsong.bus.core.processor.LocalCache;
import com.zzsong.bus.core.processor.BlockingDequeRouteTransfer;
import com.zzsong.bus.core.processor.pusher.DelivererChannel;
import com.zzsong.bus.common.share.loadbalancer.LbFactory;
import com.zzsong.bus.common.share.loadbalancer.SimpleLbFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Slf4j
@Configuration
public class BusBeanConfig {

  @Value("${spring.application.name}")
  private String applicationName;

  @Getter
  @Setter
  private boolean initialized;

  @Bean
  public ReactiveRedisSnowFlakeFactory reactiveRedisSnowFlakeFactory(
      ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
    return new ReactiveRedisSnowFlakeFactory(applicationName, reactiveStringRedisTemplate);
  }

  @Bean
  public LbFactory<DelivererChannel> lbFactory() {
    return new SimpleLbFactory<>();
  }

  @Bean
  @ConditionalOnMissingBean
  public BlockingDequeRouteTransfer localRouteTransfer(@Nonnull LocalCache localCache,
                                                       @Nonnull BusProperties properties,
                                                       @Nonnull LbFactory<DelivererChannel> lbFactory,
                                                       @Nonnull RouteInstanceService routeInstanceService) {
    return new BlockingDequeRouteTransfer(
        localCache, properties, lbFactory, routeInstanceService);
  }
}
