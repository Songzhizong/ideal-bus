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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 持久化的事件队列
 *
 * @author 宋志宗 on 2021/5/18
 */
@Slf4j
public class PersistenceEventQueue implements EventQueue {
  private static final int BLOCKING_MILLS = 1_000;
  private static final int EXPECT_SIZE = 1_000;
  private static final int OFFLINE_SUSPEND_SECONDS = 10;
  private static final int UNREACHABLE_SUSPEND_SECONDS = 1;
  private final BlockingDeque<RouteInstance> queue = new LinkedBlockingDeque<>(EXPECT_SIZE << 2);
  private final AtomicInteger size = new AtomicInteger(0);
  private final AtomicBoolean hasWaiting = new AtomicBoolean(false);
  private final AtomicBoolean suspended = new AtomicBoolean(false);
  private final AtomicInteger suspendSeconds = new AtomicInteger(1);

  private final boolean init;
  private final int shardId;
  private final long subscriptionId;
  private final Consumer consumer;
  private final RouteInstanceStorage routeInstanceStorage;
  private volatile boolean started = false;

  public PersistenceEventQueue(boolean init, int shardId, long subscriptionId,
                               @Nonnull Consumer consumer,
                               @Nonnull RouteInstanceStorage routeInstanceStorage) {
    this.init = init;
    this.shardId = shardId;
    this.subscriptionId = subscriptionId;
    this.consumer = consumer;
    this.routeInstanceStorage = routeInstanceStorage;
    start();
    if (init) {
      hasWaiting.set(true);
    }
  }

  @Override
  public boolean test() {
    if (hasWaiting.get()) {
      return false;
    }
    if (size.get() >= EXPECT_SIZE) {
      hasWaiting.set(true);
      return false;
    }
    return true;
  }

  @Override
  public void offer(@Nonnull RouteInstance routeInstance) {
    size.incrementAndGet();
    queue.offerLast(routeInstance);
  }

  @Override
  public void destroy() {
    this.started = false;
  }

  private void giveBack(@Nonnull RouteInstance routeInstance) {
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

  public void start() {
    if (this.started) {
      return;
    }
    this.started = true;
    // 装载消息到内部队列
    if (init) {
      loadWaiting();
    }
    Thread thread = new Thread(() -> {
      while (started) {
        try {
          RouteInstance routeInstance = pollFirst();
          if (routeInstance != null) {
            consumer.deliverMessage(routeInstance)
                .doOnNext(status -> {
                  if (status == DeliverStatus.SUCCESS) {
                    this.suspended.set(false);
                  } else if (status == DeliverStatus.CHANNEL_BUSY) {
                    giveBack(routeInstance);
                  } else if (status == DeliverStatus.UNREACHABLE) {
                    giveBack(routeInstance);
                    this.suspended.set(true);
                    this.suspendSeconds.set(UNREACHABLE_SUSPEND_SECONDS);
                    long applicationId = consumer.getApplicationId();
                    log.debug("应用: {} 没有可用连接", applicationId);
                  } else if (status == DeliverStatus.OFFLINE) {
                    giveBack(routeInstance);
                    this.suspended.set(true);
                    this.suspendSeconds.set(OFFLINE_SUSPEND_SECONDS);
                    long applicationId = consumer.getApplicationId();
                    log.info("应用: {} 当前处于离线状态", applicationId);
                  } else {
                    giveBack(routeInstance);
                    log.error("Unknown DeliverStatus: {}", status);
                  }
                }).subscribe();
            if (this.suspended.get()) {
              try {
                TimeUnit.SECONDS.sleep(suspendSeconds.get());
                this.suspended.set(false);
              } catch (InterruptedException e) {
                // ignore
              }
            }
          } else {
            int set = size.getAndSet(0);
            if (set != 0) {
              log.warn("size.getAndSet(0) result is " + set);
            }
            // 队列中没有消息执行的操作
            if (this.hasWaiting.get()) {
              log.info("Has waiting, loading...");
              loadWaiting();
            }
          }
        } catch (Exception e) {
          log.warn("{} pool ex:", subscriptionId, e);
          try {
            TimeUnit.SECONDS.sleep(1);
          } catch (InterruptedException interruptedException) {
            // ignore
          }
        }
      }
    });
    thread.setName("queue-" + subscriptionId);
    thread.setDaemon(true);
    thread.start();
  }

  private void loadWaiting() {
    try {
      List<RouteInstance> waitingList = routeInstanceStorage
          .loadWaiting(EXPECT_SIZE, shardId, subscriptionId).block();
      if (waitingList == null) {
        return;
      }
      int size = waitingList.size();
      if (size == 0) {
        this.hasWaiting.set(false);
        return;
      }
      this.hasWaiting.set(size >= EXPECT_SIZE);

      log.info("从存储库读取 {} 条等待中的消息入列: {}", size, subscriptionId);
      Map<Integer, List<RouteInstance>> collect = waitingList.stream()
          .collect(Collectors.groupingBy(RouteInstance::getStatus));
      collect.forEach((s, is) -> {
        if (s == RouteInstance.STATUS_TEMPING) {
          offerTempingList(is).block();
        } else {
          for (RouteInstance instance : is) {
            offer(instance);
          }
        }
      });
    } catch (Exception e) {
      log.warn("loadWaiting ex: ", e);
    }
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
            offer(instance);
          }
        });
  }
}
