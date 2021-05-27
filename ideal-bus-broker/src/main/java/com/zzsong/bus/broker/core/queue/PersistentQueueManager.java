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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 持久化队列管理器
 *
 * @author 宋志宗 on 2021/5/14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersistentQueueManager implements QueueManager, SmartInitializingSingleton, DisposableBean {
  private static final long RETRY_INTERVAL = 10_000L;
  private final Map<Long, EventQueue> queueMap = new ConcurrentHashMap<>();

  private final BusProperties properties;
  private final ConsumerManager consumerManager;
  private final SubscriptionStorage subscriptionStorage;
  private final RouteInstanceStorage routeInstanceStorage;

  @Override
  public Mono<Boolean> submit(@Nonnull List<RouteInstance> routeInstances) {
    if (routeInstances.isEmpty()) {
      return Mono.just(true);
    }
    Flux<RouteInstance> flux = Flux.fromIterable(routeInstances)
        .doOnNext(routeInstance -> {
          long nextPushTime = routeInstance.getNextPushTime();
          if (nextPushTime > 0) {
            routeInstance.setStatus(RouteInstance.STATUS_DELAYING);
            routeInstance.setMessage("delaying");
          } else {
            Long applicationId = routeInstance.getApplicationId();
            Long subscriptionId = routeInstance.getSubscriptionId();
            EventQueue eventQueue = loadQueue(applicationId, subscriptionId, false);
            boolean offer = eventQueue.test();
            if (offer) {
              routeInstance.setStatus(RouteInstance.STATUS_QUEUING);
              routeInstance.setMessage("be queuing");
            } else {
              routeInstance.setStatus(RouteInstance.STATUS_TEMPING);
              routeInstance.setMessage("temping");
            }
          }
        });
    return routeInstanceStorage.saveAll(flux)
        .doOnNext(routeInstanceList -> {
          for (RouteInstance routeInstance : routeInstanceList) {
            int status = routeInstance.getStatus();
            if (status == RouteInstance.STATUS_QUEUING) {
              Long applicationId = routeInstance.getApplicationId();
              Long subscriptionId = routeInstance.getSubscriptionId();
              EventQueue eventQueue = loadQueue(applicationId, subscriptionId, false);
              eventQueue.offer(routeInstance);
            }
          }
        })
        .map(r -> true);
  }

  @Override
  public Mono<Boolean> ack(long routeInstanceId) {
    int success = RouteInstance.STATUS_SUCCESS;
    log.debug("message: {} ack", routeInstanceId);
    return routeInstanceStorage.updateStatus(routeInstanceId, success, "success").map(l -> true);
  }

  @Override
  public Mono<Boolean> reject(long routeInstanceId, @Nullable String message) {
    return routeInstanceStorage.findById(routeInstanceId)
        .flatMap(opt -> {
          if (opt.isPresent()) {
            String saveMessage = message != null ? message : "";
            RouteInstance routeInstance = opt.get();
            int retryLimit = routeInstance.getRetryLimit();
            int retryCount = routeInstance.getRetryCount() + 1;

            routeInstance.setRetryCount(retryCount);
            if (retryCount < retryLimit) {
              routeInstance.setStatus(RouteInstance.STATUS_DELAYING);
              routeInstance.setMessage(saveMessage);
              routeInstance.setNextPushTime(System.currentTimeMillis() + RETRY_INTERVAL);
              log.debug("message: {} reject, current retry count: {}", routeInstanceId, retryCount);
            } else {
              routeInstance.setStatus(RouteInstance.STATUS_FAILURE);
              routeInstance.setMessage("Reach the retry limit: " + saveMessage);
              routeInstance.setNextPushTime(-1);
              log.info("message: {} reject and reach the retry limit: {}", routeInstanceId, retryLimit);
            }
            return routeInstanceStorage.save(routeInstance).map(i -> true);
          } else {
            return Mono.just(true);
          }
        });
  }

  @Nonnull
  private EventQueue loadQueue(Long applicationId, Long subscriptionId, boolean init) {
    return queueMap.computeIfAbsent(subscriptionId, k -> {
      Consumer consumer = consumerManager.loadConsumer(applicationId, routeInstanceStorage);
      int nodeId = properties.getNodeId();
      log.info("初始化队列: {}", subscriptionId);
      return new PersistenceEventQueue(init, nodeId, subscriptionId, consumer, routeInstanceStorage);
    });
  }

  @Override
  public void afterSingletonsInstantiated() {
    List<Subscription> block = subscriptionStorage.findAll().block();
    assert block != null;
    for (Subscription subscription : block) {
      long applicationId = subscription.getApplicationId();
      Long subscriptionId = subscription.getSubscriptionId();
      loadQueue(applicationId, subscriptionId, true);
    }
  }

  @Override
  public void destroy() {
    for (EventQueue value : queueMap.values()) {
      value.destroy();
    }
  }
}
