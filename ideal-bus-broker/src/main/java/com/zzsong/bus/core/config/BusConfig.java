package com.zzsong.bus.core.config;

import com.zzsong.bus.abs.generator.ReactiveRedisSnowFlakeFactory;
import com.zzsong.bus.core.admin.service.RouteInstanceService;
import com.zzsong.bus.core.processor.LocalCache;
import com.zzsong.bus.core.processor.LocalRouteTransfer;
import com.zzsong.bus.core.processor.pusher.DelivererChannel;
import com.zzsong.common.loadbalancer.LbFactory;
import com.zzsong.common.loadbalancer.SimpleLbFactory;
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
public class BusConfig {

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
  public LocalRouteTransfer localRouteTransfer(@Nonnull LocalCache localCache,
                                               @Nonnull BusProperties properties,
                                               @Nonnull LbFactory<DelivererChannel> lbFactory,
                                               @Nonnull RouteInstanceService routeInstanceService) {
    return new LocalRouteTransfer(
        localCache, properties, lbFactory, routeInstanceService);
  }

//  @Bean
//  public ExecutorService blockThreadPool(@Nonnull BusProperties busProperties) {
//    int processors = Runtime.getRuntime().availableProcessors();
//    ThreadPoolProperties properties = busProperties.getBlockPool();
//    int corePoolSize = properties.getCorePoolSize();
//    if (corePoolSize < 0) {
//      corePoolSize = processors << 1;
//    }
//    int maximumPoolSize = properties.getMaximumPoolSize();
//    if (maximumPoolSize < 1) {
//      maximumPoolSize = processors << 4;
//    }
//    BlockingQueue<Runnable> workQueue;
//    int workQueueSize = properties.getWorkQueueSize();
//    if (workQueueSize < 1) {
//      workQueue = new SynchronousQueue<>();
//    } else {
//      workQueue = new ArrayBlockingQueue<>(workQueueSize);
//    }
//    ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
//        60, TimeUnit.SECONDS, workQueue,
//        new BasicThreadFactory.Builder().namingPattern("job-callback-pool-%d").build(),
//        (r, executor) -> {
//          throw new RejectedExecutionException("Task " + r.toString() +
//              " rejected from jobCallbackThreadPool");
//        });
//    pool.allowCoreThreadTimeOut(true);
//    return pool;
//  }
//
//  @Bean
//  public Scheduler blockScheduler(ExecutorService blockThreadPool) {
//    return Schedulers.fromExecutorService(blockThreadPool, "blockScheduler");
//  }
}
