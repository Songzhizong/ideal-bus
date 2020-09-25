package com.zzsong.bus.receiver;

import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import com.zzsong.bus.receiver.deliver.EventDeliverer;
import com.zzsong.bus.receiver.deliver.EventDelivererImpl;
import com.zzsong.bus.receiver.listener.ListenerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 宋志宗 on 2020/9/18
 */
@Slf4j
public class SimpleBusReceiver implements BusReceiver {
  private final int maximumPoolSize;
  private final EventDeliverer eventDeliverer;
  private final ThreadPoolExecutor primary;
  private ThreadPoolExecutor spareTire;
  private final AtomicBoolean primaryBusy = new AtomicBoolean(false);

  public SimpleBusReceiver(int corePoolSize, int maximumPoolSize) {
    this.maximumPoolSize = maximumPoolSize;
    this.primary = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
        60, TimeUnit.SECONDS, new SynchronousQueue<>(),
        new BasicThreadFactory.Builder().namingPattern("event-pool-%d").build(),
        (r, e) -> {
          log.warn("任务执行线程池资源不足, 请尝试修改线程数配置, 当前核心线程数: {}, 最大线程数: {}",
              corePoolSize, maximumPoolSize);
          spareTire.execute(r);
          primaryBusy.set(true);
          // 主线程池资源耗尽了, 发布忙碌通知
          busyNotice();
        });
    Scheduler primaryScheduler = Schedulers.fromExecutor(primary);
    this.eventDeliverer = new EventDelivererImpl(primaryScheduler);
  }

  @Nonnull
  @Override
  public Mono<DeliveredResult> receive(@Nonnull DeliveredEvent event) {
    return eventDeliverer.deliver(event).doOnNext(res -> {
      if (primaryBusy.get()) {
        int activeCount = primary.getActiveCount();
        log.info("activeCount = {}", activeCount);
        // 如果有超过 75%的线程空闲了就通知所有broker将当前节点标记为可用状态
        if (activeCount <= maximumPoolSize >> 2) {
          // 主线程空闲了,发布空闲通知
          idleNotice();
          primaryBusy.set(false);
        }
      }
    });
  }

  protected void idleNotice() {
    log.debug("主线程池进入空闲状态");
  }

  protected void busyNotice() {
    log.debug("主线程池已满");
  }

  public void startReceiver() {
    initEventListeners();
    initSpareTire();
  }

  protected void initEventListeners() {

  }

  private void initSpareTire() {
    int processors = Runtime.getRuntime().availableProcessors();
    // 获取监听器总数
    int maxListener = ListenerFactory.getAll().values().stream().mapToInt(Map::size).sum();
    // 备用线程池大小上限 max(监听器总数x4, cpu核心数x4)
    int spareTireSize = Math.max(maxListener << 2, processors << 2);
    this.spareTire = new ThreadPoolExecutor(0, spareTireSize,
        60, TimeUnit.SECONDS, new SynchronousQueue<>(),
        new BasicThreadFactory.Builder().namingPattern("spare-pool-%d").build(),
        (r, executor) -> {
          log.error("备用线程池已满: {}", spareTireSize);
          if (!executor.isShutdown()) {
            r.run();
          }
        });
  }
}
