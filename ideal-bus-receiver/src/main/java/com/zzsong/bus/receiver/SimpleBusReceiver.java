package com.zzsong.bus.receiver;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import com.zzsong.bus.receiver.deliver.EventDeliverer;
import com.zzsong.bus.receiver.deliver.EventDelivererImpl;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nonnull;
import java.util.concurrent.*;

/**
 * @author 宋志宗 on 2020/9/18
 */
@Slf4j
public class SimpleBusReceiver implements BusReceiver {
  private final EventDeliverer eventDeliverer;

  public SimpleBusReceiver(int corePoolSize, int maximumPoolSize) {
    final ThreadPoolExecutor executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
        60, TimeUnit.SECONDS, new SynchronousQueue<>(),
        new ThreadFactoryBuilder().setNameFormat("event-pool-%d").build(),
        (r, executor) -> {
          log.error("任务执行线程池资源不足, 请尝试修改线程数配置, 当前核心线程数: {}, 最大线程数: {}, 队列长度: {}",
              corePoolSize, maximumPoolSize, 0);
          throw new RejectedExecutionException("Task " + r.toString() +
              " rejected from Job executor thread pool");
        });
    Scheduler scheduler = Schedulers.fromExecutor(executorService);
    this.eventDeliverer = new EventDelivererImpl(scheduler);
  }

  @Nonnull
  @Override
  public Mono<DeliveredResult> receive(@Nonnull DeliveredEvent event) {
    return eventDeliverer.deliver(event);
  }

  public void startReceiver() {
    initEventListeners();
  }

  protected void initEventListeners() {

  }
}
