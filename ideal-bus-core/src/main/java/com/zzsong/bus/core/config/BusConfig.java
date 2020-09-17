package com.zzsong.bus.core.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zzsong.bus.abs.generator.ReactiveRedisSnowFlakeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nonnull;
import java.util.concurrent.*;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Configuration
public class BusConfig {
  private static final Logger log = LoggerFactory.getLogger(BusConfig.class);

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

  @Bean
  public ExecutorService blockThreadPool(@Nonnull BusProperties busProperties) {
    int processors = Runtime.getRuntime().availableProcessors();
    ThreadPoolProperties properties = busProperties.getBlockPool();
    int corePoolSize = properties.getCorePoolSize();
    if (corePoolSize < 0) {
      corePoolSize = processors << 1;
    }
    int maximumPoolSize = properties.getMaximumPoolSize();
    if (maximumPoolSize < 1) {
      maximumPoolSize = processors << 4;
    }
    BlockingQueue<Runnable> workQueue;
    int workQueueSize = properties.getWorkQueueSize();
    if (workQueueSize < 1) {
      workQueue = new SynchronousQueue<>();
    } else {
      workQueue = new ArrayBlockingQueue<>(workQueueSize);
    }
    ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
        60, TimeUnit.SECONDS, workQueue,
        new ThreadFactoryBuilder().setNameFormat("job-callback-pool-%d").build(),
        (r, executor) -> {
          throw new RejectedExecutionException("Task " + r.toString() +
              " rejected from jobCallbackThreadPool");
        });
    pool.allowCoreThreadTimeOut(true);
    return pool;
  }

  @Bean
  public Scheduler blockScheduler(ExecutorService blockThreadPool) {
    return Schedulers.fromExecutorService(blockThreadPool, "blockScheduler");
  }
}
