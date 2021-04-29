package com.zzsong.bus.client.impl;

import com.zzsong.bus.client.BusyListener;
import com.zzsong.bus.client.EventConsumer;
import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.message.DeliverResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 宋志宗 on 2021/4/29
 */
@Slf4j
public class SimpleEventConsumer implements EventConsumer {
  private final int maximumPoolSize;
  private final ThreadPoolExecutor primary;
  private final Scheduler primaryScheduler;
  private final AtomicBoolean primaryBusy = new AtomicBoolean(false);
  private Map<String, BusyListener> busyListeners = new ConcurrentHashMap<>();

  public SimpleEventConsumer(int corePoolSize, int maximumPoolSize) {
    this.maximumPoolSize = maximumPoolSize;
    this.primary = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
        60, TimeUnit.SECONDS, new SynchronousQueue<>(),
        new BasicThreadFactory.Builder().namingPattern("event-pool-%d").build(),
        (r, e) -> {
          log.warn("任务执行线程池资源不足, 请尝试修改线程数配置, 当前核心线程数: {}, 最大线程数: {}",
              corePoolSize, maximumPoolSize);
          primaryBusy.set(true);
          // 主线程池资源耗尽了, 发布忙碌通知
          busyListeners.forEach((id, listener) -> listener.busyNotice());
          if (!e.isShutdown()) {
            r.run();
          }
        });
    this.primaryScheduler = Schedulers.fromExecutor(primary);
  }

  @Override
  public Mono<DeliverResult> receive(@Nonnull DeliverEvent event) {
//    return Mono.just(event).publishOn(primaryScheduler);
    return Mono.empty();
  }

  @Override
  public void registerBusyListener(@Nonnull BusyListener busyListener) {
    busyListeners.put(busyListener.getListenerId(), busyListener);
  }

  @Override
  public void removeBusyListener(@Nonnull BusyListener busyListener) {
    busyListeners.remove(busyListener.getListenerId());
  }
}
