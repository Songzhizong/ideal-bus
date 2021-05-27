package com.zzsong.bus.client.impl;

import com.zzsong.bus.client.Channel;
import com.zzsong.bus.client.ConsumerExecutor;
import com.zzsong.bus.client.EventConsumer;
import com.zzsong.bus.client.ExecutorListener;
import com.zzsong.bus.common.message.DeliverEvent;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 宋志宗 on 2021/5/24
 */
@CommonsLog
public class ThreadPoolConsumerExecutor implements ConsumerExecutor {
  private final int corePoolSize;
  private final int maximumPoolSize;
  private final ThreadPoolExecutor executor;
  private final AtomicInteger counter = new AtomicInteger(0);
  private final EventConsumer consumer;
  private final List<ExecutorListener> listeners = new ArrayList<>();
  private final AtomicBoolean busy = new AtomicBoolean(false);
  private final AtomicBoolean running = new AtomicBoolean(true);

  public ThreadPoolConsumerExecutor(int corePoolSize,
                                    int maximumPoolSize,
                                    @Nonnull EventConsumer consumer) {
    if (corePoolSize > maximumPoolSize) {
      throw new IllegalArgumentException("corePoolSize must be less then maximumPoolSize");
    }
    this.corePoolSize = corePoolSize;
    this.maximumPoolSize = maximumPoolSize;
    this.consumer = consumer;
    this.executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
        60, TimeUnit.SECONDS, new SynchronousQueue<>(),
        new BasicThreadFactory.Builder().namingPattern("event-pool-%d").build(),
        (r, e) -> {
          log.debug("任务执行线程池资源不足, 核心线程数: "
              + corePoolSize + ", 最大线程数: " + maximumPoolSize);
          throw new RejectedExecutionException();
        });
    Thread hook = new Thread(() -> {
      long timeMillis = System.currentTimeMillis();
      this.running.set(false);
      this.executor.shutdown();
      try {
        //noinspection ResultOfMethodCallIgnored
        this.executor.awaitTermination(60, TimeUnit.SECONDS);
        log.info("ThreadPoolConsumerExecutor shutdown, consuming: "
            + (System.currentTimeMillis() - timeMillis));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });
    hook.setName("ThreadPoolConsumerExecutor-shutdown");
    Runtime.getRuntime().addShutdownHook(hook);
  }

  @Override
  public boolean submit(@Nonnull DeliverEvent event, @Nonnull Channel channel) {
    boolean flag = true;
    if (running.get()) {
      try {
        executor.execute(() -> {
          int incrementAndGet = counter.incrementAndGet();
          if (incrementAndGet == maximumPoolSize) {
            log.debug("队列已满");
          }
          try {
            consumer.onMessage(event, channel);
          } finally {
            int decrementAndGet = counter.decrementAndGet();
            if ((decrementAndGet < corePoolSize || decrementAndGet == 0)
                && this.busy.get() && this.running.get()) {
              this.busy.set(false);
              for (ExecutorListener listener : listeners) {
                listener.onIdle();
              }
            }
          }
        });
      } catch (RejectedExecutionException e) {
        flag = false;
      } catch (Exception e) {
        log.error("Unknown exception: ", e);
        flag = false;
      }
    } else {
      flag = false;
    }
    if (!flag && !this.busy.get()) {
      this.busy.set(true);
      for (ExecutorListener listener : listeners) {
        listener.onBusy();
      }
    }
    return flag;
  }

  @Override
  public synchronized void addListener(@Nonnull ExecutorListener listener) {
    listeners.add(listener);
  }
}
