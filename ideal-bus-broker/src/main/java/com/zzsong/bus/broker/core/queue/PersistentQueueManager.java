package com.zzsong.bus.broker.core.queue;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.storage.RouteInstanceStorage;
import com.zzsong.bus.broker.config.BusProperties;
import com.zzsong.bus.broker.core.consumer.Consumer;
import com.zzsong.bus.broker.core.consumer.ConsumerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * 持久化队列管理器
 *
 * @author 宋志宗 on 2021/5/14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersistentQueueManager implements QueueManager {
  private final Map<Long, EventQueue> queueMap = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduledExecutorService
      = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

  private final BusProperties properties;
  private final ConsumerManager consumerManager;
  private final RouteInstanceStorage routeInstanceStorage;

  @Override
  public Mono<Boolean> submit(@Nonnull List<RouteInstance> routeInstances) {
    List<RouteInstance> collect = routeInstances.stream().peek(routeInstance -> {
      long nextPushTime = routeInstance.getNextPushTime();
      if (nextPushTime > 0) {
        routeInstance.setStatus(RouteInstance.STATUS_DELAYING);
        routeInstance.setMessage("delaying");
      } else {
        Long applicationId = routeInstance.getApplicationId();
        Long subscriptionId = routeInstance.getSubscriptionId();
        Consumer consumer = consumerManager.loadConsumer(applicationId);
        EventQueue eventQueue = queueMap.computeIfAbsent(subscriptionId, k -> createQueue(k, consumer));
        boolean offer = eventQueue.offer(routeInstance);
        if (offer) {
          routeInstance.setStatus(RouteInstance.STATUS_QUEUING);
          routeInstance.setMessage("be queuing");
        } else {
          routeInstance.setStatus(RouteInstance.STATUS_TEMPING);
          routeInstance.setMessage("temping");
        }
      }
    }).collect(Collectors.toList());
    return routeInstanceStorage.saveAll(collect).map(r -> true);
  }

  @Nonnull
  private PersistenceEventQueue createQueue(long subscriptionId, @Nonnull Consumer consumer) {
    int nodeId = properties.getNodeId();
    return new PersistenceEventQueue(nodeId, subscriptionId,
        consumer, routeInstanceStorage, scheduledExecutorService);
  }
}
