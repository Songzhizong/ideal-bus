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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author 宋志宗 on 2020/9/19 8:08 下午
 */
@Slf4j
public class LocalRouteTransfer implements RouteTransfer, ApplicationRunner, DisposableBean {
  /**
   * 单个队列上限
   */
  private static final int QUEUE_SIZE = 10_000;
  private static final int WAIT_SECONDS = 10;
  /**
   * 每个订阅关系一个队列
   */
  private final ConcurrentMap<Long, BlockingDeque<RouteInstance>> queueMap
      = new ConcurrentHashMap<>();
  /**
   * 记录offer失败的队列
   */
  private final ConcurrentMap<Long, Boolean> noSpaceMarkMap = new ConcurrentHashMap<>();
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private final List<Thread> customerThreadList = new ArrayList<>();
  private volatile boolean startThread = true;

  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final BusProperties properties;
  @Nonnull
  private final MessagePusher messagePusher;
  @Nonnull
  private final LbFactory<DelivererChannel> lbFactory;
  @Nonnull
  private final RouteInstanceService routeInstanceService;

  public LocalRouteTransfer(@Nonnull LocalCache localCache,
                            @Nonnull BusProperties properties,
                            @Nonnull MessagePusher messagePusher,
                            @Nonnull LbFactory<DelivererChannel> lbFactory,
                            @Nonnull RouteInstanceService routeInstanceService) {
    this.localCache = localCache;
    this.properties = properties;
    this.messagePusher = messagePusher;
    this.lbFactory = lbFactory;
    this.routeInstanceService = routeInstanceService;
  }

  @Override
  public Mono<Boolean> submit(@Nonnull List<RouteInstance> routeInstanceList) {
    for (RouteInstance routeInstance : routeInstanceList) {
      long key = routeInstance.getSubscriptionId();
      BlockingDeque<RouteInstance> queue = queueMap.computeIfAbsent(key, k -> {
        LinkedBlockingDeque<RouteInstance> routeQueue = new LinkedBlockingDeque<>(QUEUE_SIZE);
        createConsumeThread(key, routeQueue);
        return routeQueue;
      });
      Boolean mark = noSpaceMarkMap.get(key);
      if (mark == null || !mark) {
        boolean offer = queue.offerLast(routeInstance);
        if (!offer) {
          noSpaceMarkMap.put(key, true);
        }
      }
    }
    return Mono.just(true);
  }

  private void createConsumeThread(long key, BlockingDeque<RouteInstance> queue) {
    Thread customerThread = new Thread(() -> {
      while (startThread) {
        try {
          RouteInstance routeInstance = queue.pollFirst(5, TimeUnit.SECONDS);
          if (routeInstance == null) {
            Boolean mark = noSpaceMarkMap.get(key);
            if (mark != null && mark) {
              int nodeId = properties.getNodeId();
              routeInstanceService.loadWaiting(QUEUE_SIZE, nodeId)
                  .flatMap(list -> {
                    if (list.size() < QUEUE_SIZE) {
                      noSpaceMarkMap.remove(key);
                    }
                    return submit(list);
                  }).block();
            }
          } else {
            long subscriptionId = routeInstance.getSubscriptionId();
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
                log.info("应用: {} 当前没有在线实例", applicationId);
                queue.offerFirst(routeInstance);
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
              messagePusher.push(routeInstance).subscribe();
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

  @Override
  public void run(ApplicationArguments args) {
    // 按订阅关系从存储库中取出固定条数的消息加入队列
  }

  @Override
  public void destroy() {
    this.startThread = false;
  }
}
