package com.zzsong.bus.core.processor;

import com.zzsong.bus.abs.constants.ApplicationTypeEnum;
import com.zzsong.bus.abs.core.MessagePusher;
import com.zzsong.bus.abs.core.RouteTransfer;
import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.core.admin.service.RouteInstanceService;
import com.zzsong.bus.core.config.BusProperties;
import com.zzsong.bus.core.processor.pusher.DelivererChannel;
import com.zzsong.common.loadbalancer.LbFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author 宋志宗 on 2020/9/19 8:08 下午
 */
@Slf4j
public class LocalRouteTransfer implements RouteTransfer, DisposableBean {
  /**
   * 单个队列上限
   */
  private static final int QUEUE_SIZE = 1_000;
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
   * 记录offer失败的队列
   */
  private final ConcurrentMap<Long, Boolean> noSpaceMarkMap = new ConcurrentHashMap<>();
  /**
   * 存储每个队列的当前阻塞时间
   */
  private final ConcurrentMap<Long, Integer> pollTimeMap = new ConcurrentHashMap<>();
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private final List<Thread> customerThreadList = new ArrayList<>();
  private volatile boolean startThread = true;

  @Nonnull
  @Autowired
  private MessagePusher messagePusher;

  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final BusProperties properties;
  @Nonnull
  private final LbFactory<DelivererChannel> lbFactory;
  @Nonnull
  private final RouteInstanceService routeInstanceService;

  public LocalRouteTransfer(@Nonnull LocalCache localCache,
                            @Nonnull BusProperties properties,
                            @Nonnull LbFactory<DelivererChannel> lbFactory,
                            @Nonnull RouteInstanceService routeInstanceService) {
    this.localCache = localCache;
    this.properties = properties;
    this.lbFactory = lbFactory;
    this.routeInstanceService = routeInstanceService;
  }

  @Override
  public Mono<Boolean> submit(@Nonnull List<RouteInstance> routeInstanceList, boolean force) {
    for (RouteInstance routeInstance : routeInstanceList) {
      long subscriptionId = routeInstance.getSubscriptionId();
      BlockingDeque<RouteInstance> queue = loadQueue(subscriptionId);
      Boolean mark = noSpaceMarkMap.get(subscriptionId);
      // 如果是强制提交或者队列没有被标记为暂停服务状态则向队列中添加消息
      if (force || mark == null || !mark) {
        boolean offer = queue.offerLast(routeInstance);
        if (!offer) {
          log.warn("队列: {} 已满, topic: {}, application: {}",
              subscriptionId, routeInstance.getTopic(), routeInstance.getApplicationId());
          noSpaceMarkMap.put(subscriptionId, true);
        }
      }
    }
    return Mono.just(true);
  }

  private BlockingDeque<RouteInstance> loadQueue(long subscriptionId) {
    // 设置默认的队列读取超时时间
    pollTimeMap.put(subscriptionId, DEFAULT_POLL_TIMEOUT);
    return queueMap.computeIfAbsent(subscriptionId, k -> {
      LinkedBlockingDeque<RouteInstance> routeQueue = new LinkedBlockingDeque<>(QUEUE_SIZE);
      createConsumeThread(subscriptionId, routeQueue);
      return routeQueue;
    });
  }

  /**
   * 向队列头归还消息
   */
  @Override
  public Mono<Boolean> giveBack(@Nonnull RouteInstance routeInstance) {
    Long subscriptionId = routeInstance.getSubscriptionId();
    BlockingDeque<RouteInstance> deque = queueMap.get(subscriptionId);
    if (deque != null) {
      deque.offerFirst(routeInstance);
      return Mono.just(true);
    }
    return Mono.just(false);
  }

  /**
   * 为队列创建消费线程
   */
  private void createConsumeThread(long subscriptionId, BlockingDeque<RouteInstance> queue) {
    Thread customerThread = new Thread(() -> {
      while (startThread) {
        try {
          RouteInstance routeInstance = queue
              .pollFirst(pollTimeMap.get(subscriptionId), TimeUnit.SECONDS);
          // 是否需要从存储库读取读取未消费掉的消息
          if (routeInstance == null) {
            Boolean mark = noSpaceMarkMap.get(subscriptionId);
            if (mark != null && mark) {
              int nodeId = properties.getNodeId();
              routeInstanceService.loadWaiting(QUEUE_SIZE, nodeId, subscriptionId)
                  .flatMap(list -> {
                    final int size = list.size();
                    log.info("队列: {} 从存储库读取 {}条消息", subscriptionId, size);
                    if (size < QUEUE_SIZE) {
                      noSpaceMarkMap.remove(subscriptionId);
                      log.info("队列: {} 进入对外可用状态", subscriptionId);
                      pollTimeMap.put(subscriptionId, DEFAULT_POLL_TIMEOUT);
                    } else {
                      pollTimeMap.put(subscriptionId, 0);
                    }
                    return submit(list, true);
                  }).block();
            }
          } else {
            SubscriptionDetails subscription = localCache.getSubscription(subscriptionId);
            if (subscription == null) {
              log.error("订阅关系: {} 不存在", subscriptionId);
              queue.offerFirst(routeInstance);
              TimeUnit.SECONDS.sleep(WAIT_SECONDS);
              continue;
            }
            final long applicationId = subscription.getApplicationId();
            final ApplicationTypeEnum applicationType = subscription.getApplicationType();
            if (applicationType == ApplicationTypeEnum.INTERNAL) {
              List<DelivererChannel> servers = lbFactory
                  .getReachableServers(applicationId + "");
              if (servers.isEmpty()) {
                int size = queue.size();
                queue.offerFirst(routeInstance);
                log.info("应用: {} 没有可用实例, 当前队列大小: {}", applicationId, size);
                // 如果当前没有在线的服务, 先睡眠10秒然后重试
                TimeUnit.SECONDS.sleep(WAIT_SECONDS);
                continue;
              }
            }
            if (subscription.getConsumeType() == Subscription.CONSUME_TYPE_SERIAL) {
              // 串行消息以阻塞当前线程的方式执行推送
              messagePusher.push(routeInstance).block();
            } else {
              // 并行消息异步推送
              messagePusher.push(routeInstance)
                  .onErrorResume(throwable -> {
                    log.info("ex: ", throwable);
                    if (throwable instanceof ClosedChannelException) {
                      queue.offerFirst(routeInstance);
                    }
                    return Mono.empty();
                  })
                  .subscribe();
            }
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      Thread.currentThread().interrupt();
    });
    customerThread.start();
    synchronized (customerThreadList) {
      customerThreadList.add(customerThread);
    }
  }

  public void init() {
    log.debug("Init LocalRouteTransfer ...");
    // 本地缓存必须完成初始化才能进行下一步的操作
//    while (!localCache.isInitialized()) {
//      try {
//        TimeUnit.MILLISECONDS.sleep(10);
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
//    }
    // 为每个订阅关系创建队列, 并设置为暂不可用状态
    final Collection<SubscriptionDetails> subscription = localCache.getAllSubscription();
    log.info("存在订阅关系 {}条", subscription.size());
    for (SubscriptionDetails details : subscription) {
      final Long subscriptionId = details.getSubscriptionId();
      loadQueue(subscriptionId);
      pollTimeMap.put(subscriptionId, 0);
      noSpaceMarkMap.put(subscriptionId, true);
    }
    log.debug("Init LocalRouteTransfer completed.");
  }

  @Override
  public void destroy() {
    this.startThread = false;
  }

}
