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
          log.info("任务执行线程池资源不足, 核心线程数: "
              + corePoolSize + ", 最大线程数: " + maximumPoolSize);
          throw new RejectedExecutionException();
        });
  }

  @Override
  public boolean submit(@Nonnull DeliverEvent event, @Nonnull Channel channel) {
    boolean flag = true;
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
          if (decrementAndGet < corePoolSize || decrementAndGet == 0) {
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
    if (!flag) {
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
