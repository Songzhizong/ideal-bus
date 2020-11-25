package com.zzsong.bus.broker.core;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.broker.admin.service.RouteInstanceService;
import com.zzsong.bus.broker.cluster.ClusterApi;
import com.zzsong.bus.broker.config.BusProperties;
import com.zzsong.bus.broker.connect.ConnectionManager;
import com.zzsong.bus.broker.constants.DeliverResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 宋志宗 on 2020/9/19 8:08 下午
 */
@Slf4j
@Component
public class RouteTransfer implements DisposableBean {
  /**
   * 单个队列上限
   */
  private static final int QUEUE_SIZE_LIMIT = 10_000;
  private static final int LOAD_LIMIT = 1_000;
  private static final int WAIT_SECONDS = 10;
  /**
   * 读取队列的默认阻塞时间
   */
  private static final int DEFAULT_POLL_TIMEOUT = 5;
  /**
   * 每个订阅关系一个队列
   */
  private final ConcurrentMap<Long, BlockingDeque<RouteInstance>> queueMap
      = new ConcurrentHashMap<>();
  /**
   * 记录每个队列当前大小
   */
  private final ConcurrentMap<Long, AtomicInteger> queueSizeMap = new ConcurrentHashMap<>();
  /**
   * 记录offer失败的队列
   */
  private final ConcurrentMap<Long, Boolean> noSpaceMarkMap = new ConcurrentHashMap<>();
  /**
   * 存储每个队列的当前阻塞时间
   */
  private final ConcurrentMap<Long, Integer> pollTimeMap = new ConcurrentHashMap<>();
  private final Set<Long> offlineSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private volatile boolean startThread = true;

  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final ClusterApi clusterApi;
  @Nonnull
  private final BusProperties properties;
  @Nonnull
  private final ConnectionManager connectionManager;
  @Nonnull
  private final RouteInstanceService routeInstanceService;

  public RouteTransfer(@Nonnull LocalCache localCache,
                       @Nonnull ClusterApi clusterApi,
                       @Nonnull BusProperties properties,
                       @Nonnull ConnectionManager connectionManager,
                       @Nonnull RouteInstanceService routeInstanceService) {
    this.localCache = localCache;
    this.clusterApi = clusterApi;
    this.properties = properties;
    this.connectionManager = connectionManager;
    this.routeInstanceService = routeInstanceService;
  }

  public void init() {
    log.debug("Init BlockingDequeRouteTransfer ...");
    // 为每个订阅关系创建队列, 并设置为暂不可用状态
    final Collection<SubscriptionDetails> subscription = localCache.getAllSubscription();
    log.info("存在订阅关系 {}条", subscription.size());
    for (SubscriptionDetails details : subscription) {
      final Long subscriptionId = details.getSubscriptionId();
      loadQueue(subscriptionId);
      pollTimeMap.put(subscriptionId, 0);
      noSpaceMarkMap.put(subscriptionId, true);
    }
    log.info("Init BlockingDequeRouteTransfer completed.");
  }

  @Nonnull
  public Mono<Boolean> submit(@Nonnull List<RouteInstance> routeInstanceList) {
    return submit(routeInstanceList, false);
  }


  @Nonnull
  private Mono<Boolean> submit(@Nonnull List<RouteInstance> routeInstanceList, boolean force) {
    for (RouteInstance routeInstance : routeInstanceList) {
      long subscriptionId = routeInstance.getSubscriptionId();
      BlockingDeque<RouteInstance> queue = loadQueue(subscriptionId);
      Boolean mark = noSpaceMarkMap.get(subscriptionId);
      // 如果是强制提交或者队列没有被标记为暂停服务状态则向队列中添加消息
      if (force || mark == null || !mark) {
        AtomicInteger atomicInteger = queueSizeMap.get(subscriptionId);
        int queueSize = atomicInteger.incrementAndGet();
        if (force || queueSize < QUEUE_SIZE_LIMIT) {
          queue.offerLast(routeInstance);
        } else {
          log.warn("队列: {} 已满, topic: {}, application: {}",
              subscriptionId, routeInstance.getTopic(), routeInstance.getApplicationId());
          noSpaceMarkMap.put(subscriptionId, true);
          atomicInteger.decrementAndGet();
        }
      }
    }
    return Mono.just(true);
  }

  // ---------------------------------------- private methods ~

  private BlockingDeque<RouteInstance> loadQueue(long subscriptionId) {
    // 设置默认的队列读取超时时间
    pollTimeMap.put(subscriptionId, DEFAULT_POLL_TIMEOUT);
    return queueMap.computeIfAbsent(subscriptionId, k -> {
      queueSizeMap.put(subscriptionId, new AtomicInteger(0));
      LinkedBlockingDeque<RouteInstance> routeQueue
          = new LinkedBlockingDeque<>(QUEUE_SIZE_LIMIT << 2);
      createConsumeThread(subscriptionId, routeQueue);
      return routeQueue;
    });
  }

  /**
   * 为队列创建消费线程
   */
  private void createConsumeThread(long subscriptionId,
                                   @Nonnull BlockingDeque<RouteInstance> queue) {
    Thread customerThread = new Thread(() -> {
      while (startThread) {
        RouteInstance routeInstance;
        try {
          Integer timeout = pollTimeMap.get(subscriptionId);
          if (timeout == null || timeout < 1) {
            routeInstance = queue.pollFirst();
          } else {
            routeInstance = queue.pollFirst(timeout, TimeUnit.SECONDS);
          }
        } catch (InterruptedException e) {
          log.error("{} <- subscription queue Interrupted,", subscriptionId);
          break;
        }
        // 是否需要从存储库读取读取未消费掉的消息
        if (routeInstance == null) {
          supplementMessageQueue(subscriptionId);
          continue;
        }

        // 如果当前没有在线的服务, 先睡眠10秒然后重试
        Long applicationId = routeInstance.getApplicationId();
        boolean remove = offlineSet.remove(applicationId);
        if (remove) {
          int size = queue.size();
          queue.offerFirst(routeInstance);
          log.info("应用: {} 没有可用实例, 当前队列大小: {}", applicationId, size);
          try {
            TimeUnit.SECONDS.sleep(WAIT_SECONDS);
          } catch (InterruptedException e) {
            log.info("sleep Interrupted");
          }
          continue;
        }

        connectionManager.deliver(routeInstance)
            .flatMap(deliverResult -> handleDeliverResult(routeInstance, deliverResult))
            .doOnNext(b -> {
              if (b) {
                queueSizeMap.get(subscriptionId).decrementAndGet();
              } else {
                queue.offerFirst(routeInstance);
              }
            }).subscribe();
      }
      Thread.currentThread().interrupt();
    });
    customerThread.start();
  }

  /**
   * 填充传输队列
   *
   * @param subscriptionId 订阅关系id
   */
  private void supplementMessageQueue(long subscriptionId) {
    Boolean mark = noSpaceMarkMap.get(subscriptionId);
    if (mark != null && mark) {
      int nodeId = properties.getNodeId();
      routeInstanceService.loadWaiting(LOAD_LIMIT, nodeId, subscriptionId)
          .flatMap(list -> {
            final int size = list.size();
            log.info("队列: {} 从存储库读取 {}条消息", subscriptionId, size);
            if (size < LOAD_LIMIT) {
              noSpaceMarkMap.remove(subscriptionId);
              log.info("队列: {} 进入对外可用状态", subscriptionId);
              pollTimeMap.put(subscriptionId, DEFAULT_POLL_TIMEOUT);
            } else {
              pollTimeMap.put(subscriptionId, 0);
            }
            return submit(list, true);
          }).block();
    }
  }

  /**
   * 处理交付结果
   *
   * @author 宋志宗 on 2020/11/25
   */
  @Nonnull
  private Mono<Boolean> handleDeliverResult(@Nonnull RouteInstance routeInstance,
                                            @Nonnull DeliverResult deliverResult) {
    return switch (deliverResult) {
      case SUCCESS -> Mono.just(true);
      // 如果客户端没有实例在线, 则尝试委托集群中的代理节点执行交付
      case APP_OFFLINE -> clusterApi
          .entrustDeliver(routeInstance)
          .map(entrust ->
              switch (entrust) {
                case SUCCESS -> true;
                // 集群代理执行交付也失败了则将消息返还给队列
                case APP_OFFLINE -> {
                  offlineSet.add(routeInstance.getApplicationId());
                  yield false;
                }
                case CHANNEL_CLOSED,
                    UNKNOWN_EXCEPTION -> false;
              });
      // 通道关闭或未知异常均将消息返还给队列
      case CHANNEL_CLOSED,
          UNKNOWN_EXCEPTION -> Mono.just(false);
    };
  }

  @Override
  public void destroy() {
    this.startThread = false;
  }
}
