package com.zzsong.bus.broker.core.queue;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.abs.storage.RouteInstanceStorage;
import com.zzsong.bus.abs.storage.SubscriptionStorage;
import com.zzsong.bus.broker.config.BusProperties;
import com.zzsong.bus.broker.core.consumer.Consumer;
import com.zzsong.bus.broker.core.consumer.ConsumerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 持久化队列管理器
 *
 * @author 宋志宗 on 2021/5/14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersistentQueueManager implements QueueManager, SmartInitializingSingleton {
  private final Map<Long, EventQueue> queueMap = new ConcurrentHashMap<>();

  private final BusProperties properties;
  private final ConsumerManager consumerManager;
  private final SubscriptionStorage subscriptionStorage;
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
        EventQueue eventQueue = loadQueue(applicationId, subscriptionId);
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
  private EventQueue loadQueue(Long applicationId, Long subscriptionId) {
    return queueMap.computeIfAbsent(subscriptionId, k -> {
      Consumer consumer = consumerManager.loadConsumer(applicationId);
      int nodeId = properties.getNodeId();
      log.info("初始化队列: {}", subscriptionId);
      return new PersistenceEventQueue(nodeId, subscriptionId, consumer, routeInstanceStorage);
    });
  }

  @Override
  public void afterSingletonsInstantiated() {
    List<Subscription> block = subscriptionStorage.findAll().block();
    assert block != null;
    for (Subscription subscription : block) {
      long applicationId = subscription.getApplicationId();
      Long subscriptionId = subscription.getSubscriptionId();
      loadQueue(applicationId, subscriptionId);
    }
  }
}
