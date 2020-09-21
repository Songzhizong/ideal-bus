package com.zzsong.bus.receiver.spring.boot.starter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zzsong.bus.receiver.SpringBusReceiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗 on 2020/9/18
 */
@Slf4j
@Configuration
public class BusReceiverAutoConfig {

  @Bean
  public ThreadPoolExecutor threadPoolExecutor() {
    int corePoolSize = 0;
    int maximumPoolSize = 100;
    return new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
        60, TimeUnit.SECONDS, new SynchronousQueue<>(),
        new ThreadFactoryBuilder().setNameFormat("event-pool-%d").build(),
        (r, executor) -> {
          log.error("任务执行线程池资源不足, 请尝试修改线程数配置, 当前核心线程数: {}, 最大线程数: {}, 队列长度: {}",
              corePoolSize, maximumPoolSize, 0);
          throw new RejectedExecutionException("Task " + r.toString() +
              " rejected from Job executor thread pool");
        });
  }

  @Bean
  public SpringBusReceiver springBusReceiver(ThreadPoolExecutor threadPoolExecutor) {
    return new SpringBusReceiver(threadPoolExecutor);
  }
}
