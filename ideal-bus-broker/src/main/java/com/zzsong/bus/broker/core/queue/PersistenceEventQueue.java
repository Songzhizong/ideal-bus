package com.zzsong.bus.broker.core.queue;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.storage.RouteInstanceStorage;
import com.zzsong.bus.broker.core.consumer.Consumer;
import com.zzsong.bus.broker.core.consumer.DeliverStatus;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 持久化的事件队列
 *
 * @author 宋志宗 on 2021/5/18
 */
@Slf4j
public class PersistenceEventQueue implements EventQueue {
  private static final int SUSPEND_SECONDS = 5;
  private static final int BLOCKING_MILLS = 1_000;
  private static final int EXPECT_SIZE = 1_000;
  private static final int LOAD_TEMPING_DELAY_SECONDS = 5;
  private final BlockingDeque<RouteInstance> queue
      = new LinkedBlockingDeque<>(EXPECT_SIZE << 2);
  private final AtomicInteger size = new AtomicInteger(0);
  private final Lock lock = new ReentrantLock();

  private final int shardId;
  private final long subscriptionId;
  private final Consumer consumer;
  private final RouteInstanceStorage routeInstanceStorage;

  /**
   * 用于控制投递过来的消息是否直接入队, true 代表前面还有消息为入列
   * 默认为true, 假定存储库中存在未入列的消息.
   * 当暂存队列写满时
   */
  private volatile boolean hasWaiting = true;
  private volatile boolean suspended = false;
  private volatile boolean started = false;


  public PersistenceEventQueue(int shardId, long subscriptionId,
                               @Nonnull Consumer consumer,
                               @Nonnull RouteInstanceStorage routeInstanceStorage,
                               @Nonnull ScheduledExecutorService scheduledExecutorService) {
    this.shardId = shardId;
    this.subscriptionId = subscriptionId;
    this.consumer = consumer;
    this.routeInstanceStorage = routeInstanceStorage;
    start();
    scheduledExecutorService.schedule(this::loadTemping, LOAD_TEMPING_DELAY_SECONDS, TimeUnit.SECONDS);
  }

  @Override
  public boolean offer(@Nonnull RouteInstance routeInstance) {
    if (hasWaiting) {
      return false;
    }
    if (size.get() >= EXPECT_SIZE) {
      hasWaiting = true;
      return false;
    } else {
      offerLast(routeInstance);
      return true;
    }
  }

  @Override
  public void destroy() {
    this.started = false;
  }

  private void offerLast(@Nonnull RouteInstance routeInstance) {
    size.incrementAndGet();
    queue.offerLast(routeInstance);
  }

  private void offerFirst(@Nonnull RouteInstance routeInstance) {
    size.incrementAndGet();
    queue.offerFirst(routeInstance);
  }

  @Nullable
  private RouteInstance pollFirst() throws InterruptedException {
    RouteInstance routeInstance = queue.pollFirst(BLOCKING_MILLS, TimeUnit.MILLISECONDS);
    if (routeInstance != null) {
      size.decrementAndGet();
    }
    return routeInstance;
  }

  public synchronized void start() {
    if (this.started) {
      return;
    }
    this.started = true;
    // 装载消息到内部队列
    loadWaiting();
    Thread thread = new Thread(() -> {
      while (started) {
        try {
          RouteInstance routeInstance = pollFirst();
          if (routeInstance != null) {
            consumer.deliverMessage(routeInstance)
                .doOnNext(status -> {
                  if (status == DeliverStatus.SUCCESS) {
                    this.suspended = false;
                  } else if (status == DeliverStatus.CHANNEL_BUSY) {
                    offerFirst(routeInstance);
                  } else if (status == DeliverStatus.UNREACHABLE) {
                    offerFirst(routeInstance);
                    this.suspended = true;
                  } else {
                    log.error("Unknown DeliverStatus: {}", status);
                  }
                }).subscribe();
            if (this.suspended) {
              try {
                TimeUnit.SECONDS.sleep(SUSPEND_SECONDS);
                this.suspended = false;
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
          } else {
            if (!this.hasWaiting) {
              try {
                TimeUnit.SECONDS.sleep(SUSPEND_SECONDS);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
            loadWaiting();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
          try {
            TimeUnit.SECONDS.sleep(1);
          } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
          }
        }
      }
    });
    thread.setName("queue-" + subscriptionId);
    thread.setDaemon(true);
    thread.start();
  }

  private void loadWaiting() {
    tryLock(() -> {
      List<RouteInstance> waitingList = routeInstanceStorage
          .loadWaiting(EXPECT_SIZE, shardId, subscriptionId).block();
      if (waitingList == null) {
        return;
      }
      int size = waitingList.size();
      if (size == 0) {
        this.hasWaiting = false;
        return;
      }
      log.info("从存储库读取 {} 条消息入列: {}", size, subscriptionId);
      Map<Integer, List<RouteInstance>> collect = waitingList.stream()
          .collect(Collectors.groupingBy(RouteInstance::getStatus));
      collect.forEach((s, is) -> {
        if (s == RouteInstance.STATUS_TEMPING) {
          offerTempingList(is).block();
        } else {
          for (RouteInstance instance : is) {
            offerLast(instance);
          }
        }
      });
    });
  }

  private void loadTemping() {
    tryLock(() -> {
      int count = EXPECT_SIZE - size.get();
      if (count > 0) {
        List<RouteInstance> instanceList = routeInstanceStorage
            .loadTemping(count, shardId, subscriptionId).block();
        if (instanceList == null) {
          return;
        }
        int size = instanceList.size();
        if (size == 0) {
          return;
        }
        log.info("从存储库读取 {} 条暂存消息入列: {}", size, subscriptionId);
        offerTempingList(instanceList).block();
      }
    });
  }


  @Nonnull
  private Mono<Long> offerTempingList(@Nonnull List<RouteInstance> instanceList) {
    List<Long> instanceIdList = instanceList.stream()
        .map(RouteInstance::getInstanceId)
        .collect(Collectors.toList());
    int status = RouteInstance.STATUS_QUEUING;
    String message = "queuing";
    return routeInstanceStorage.updateStatus(instanceIdList, status, message)
        .doOnNext(l -> {
          for (RouteInstance instance : instanceList) {
            offerLast(instance);
          }
        });
  }

  private void tryLock(@Nonnull Runnable runnable) {
    boolean tryLock = lock.tryLock();
    if (tryLock) {
      try {
        runnable.run();
      } finally {
        lock.unlock();
      }
    }
  }
}
