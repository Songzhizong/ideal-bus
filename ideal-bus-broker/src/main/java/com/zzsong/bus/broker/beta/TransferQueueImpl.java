package com.zzsong.bus.broker.beta;

import com.zzsong.bus.abs.domain.RouteInstance;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 宋志宗 on 2020/11/26
 */
public class TransferQueueImpl implements TransferQueue {
  /**
   * 单个队列上限
   */
  private static final int QUEUE_SIZE_LIMIT = 10_000;
  /**
   * 每个订阅关系一个组队列
   */
  private final ConcurrentMap<Long, List<BlockingDeque<RouteInstance>>> queueMap
      = new ConcurrentHashMap<>();
  private final ConcurrentMap<Long, AtomicLong> indexCalculatorMap = new ConcurrentHashMap<>();

  private final int signalQueueSize = Runtime.getRuntime().availableProcessors();
  private List<LinkedBlockingQueue<Signal>> signalQueues;
  private final AtomicLong signalCalculator = new AtomicLong(0);
  @Nonnull
  private final TransferQueueListener listener;

  public TransferQueueImpl(@Nonnull TransferQueueListener listener) {
    this.listener = listener;
    init();
  }

  @Override
  public boolean submit(@Nonnull RouteInstance instance, int quantity) {
    long subscriptionId = instance.getSubscriptionId();
    String aggregate = instance.getAggregate();
    List<BlockingDeque<RouteInstance>> queueGroup = queueMap
        .computeIfAbsent(subscriptionId, k -> {
          List<BlockingDeque<RouteInstance>> list = new ArrayList<>();
          for (int i = 0; i < quantity; i++) {
            LinkedBlockingDeque<RouteInstance> routeQueue
                = new LinkedBlockingDeque<>(QUEUE_SIZE_LIMIT);
            list.add(routeQueue);
          }
          return Collections.unmodifiableList(list);
        });
    int index;
    if (StringUtils.isNotBlank(aggregate)) {
      index = aggregate.hashCode() % queueGroup.size();
    } else {
      AtomicLong atomicLong = indexCalculatorMap
          .computeIfAbsent(subscriptionId, k -> new AtomicLong(0));
      index = Math.toIntExact(Math.abs(atomicLong.getAndIncrement() % queueGroup.size()));
    }
    BlockingDeque<RouteInstance> deque = queueGroup.get(index);
    boolean offer = deque.offer(instance);
    if (offer) {
      Signal signal = new Signal(subscriptionId, index);
      long increment = signalCalculator.getAndIncrement();
      int abs = Math.toIntExact(Math.abs(increment % signalQueueSize));
      LinkedBlockingQueue<Signal> queue = signalQueues.get(abs);
      queue.offer(signal);
    }
    return offer;
  }

  private void init() {
    List<LinkedBlockingQueue<Signal>> list = new ArrayList<>();
    for (int i = 0; i < signalQueueSize; i++) {
      LinkedBlockingQueue<Signal> queue = loadSignalQueue();
      list.add(queue);
    }
    signalQueues = Collections.unmodifiableList(list);
  }

  @Nonnull
  private LinkedBlockingQueue<Signal> loadSignalQueue() {
    return null;
  }

  @Getter
  @RequiredArgsConstructor
  private static class Signal {
    private final long subscriptionId;
    private final int index;
  }
}
