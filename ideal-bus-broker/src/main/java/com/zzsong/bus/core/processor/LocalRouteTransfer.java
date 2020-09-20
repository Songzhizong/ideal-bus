package com.zzsong.bus.core.processor;

import com.zzsong.bus.abs.constants.ApplicationTypeEnum;
import com.zzsong.bus.abs.core.MessagePusher;
import com.zzsong.bus.abs.core.RouteTransfer;
import com.zzsong.bus.abs.domain.RouteInfo;
import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.core.processor.pusher.DelivereChannel;
import com.zzsong.common.loadbalancer.LbFactory;
import com.zzsong.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/19 8:08 下午
 */
@Slf4j
public class LocalRouteTransfer implements RouteTransfer, ApplicationRunner, DisposableBean {
  private final ConcurrentMap<Long, BlockingQueue<RouteInfo>> queueMap = new ConcurrentHashMap<>();
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private final List<Thread> customerThreadList = new ArrayList<>();
  private volatile boolean startThread = true;
  private static final String FLUSH_FILE_NAME = "bus_flushed_route_info";

  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final MessagePusher messagePusher;
  @Nonnull
  private final LbFactory<DelivereChannel> lbFactory;

  public LocalRouteTransfer(@Nonnull LocalCache localCache,
                            @Nonnull MessagePusher messagePusher,
                            @Nonnull LbFactory<DelivereChannel> lbFactory) {
    this.localCache = localCache;
    this.messagePusher = messagePusher;
    this.lbFactory = lbFactory;
  }

  @Override
  public Mono<Boolean> submit(@Nonnull List<RouteInfo> routeInfoList) {
    for (RouteInfo routeInfo : routeInfoList) {
      long key = routeInfo.getSubscriptionId();
      BlockingQueue<RouteInfo> queue = queueMap.computeIfAbsent(key, k -> {
        LinkedBlockingQueue<RouteInfo> routeQueue = new LinkedBlockingQueue<>();
        createConsumeThread(routeQueue);
        return routeQueue;
      });
      queue.offer(routeInfo);
    }
    return Mono.just(true);
  }

  private void createConsumeThread(BlockingQueue<RouteInfo> queue) {
    Thread customerThread = new Thread(() -> {
      while (startThread) {
        try {
          RouteInfo routeInfo = queue.poll(5, TimeUnit.SECONDS);
          if (routeInfo != null) {
            push(routeInfo);
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

  /**
   * TODO: 客户端一直不上线将会导致队列中的消息堆积
   */
  private void push(@Nonnull RouteInfo routeInfo) throws InterruptedException {
    Long subscriptionId = routeInfo.getSubscriptionId();
    SubscriptionDetails subscription = localCache.getSubscription(subscriptionId);
    if (subscription == null) {
      log.error("订阅关系: {} 不存在", subscriptionId);
      TimeUnit.SECONDS.sleep(5L);
      push(routeInfo);
      return;
    }
    final long applicationId = subscription.getApplicationId();
    final ApplicationTypeEnum applicationType = subscription.getApplicationType();
    if (applicationType == ApplicationTypeEnum.INTERNAL) {
      List<DelivereChannel> servers = lbFactory.getReachableServers(applicationId + "");
      if (servers.isEmpty()) {
        log.info("应用: {} 当前没有在线实例", applicationId);
        // 如果当前没有在线的服务, 先睡眠5秒然后重试
        TimeUnit.SECONDS.sleep(5L);
        push(routeInfo);
        return;
      }
    }
    if (subscription.getConsumeType() == Subscription.CONSUME_TYPE_SERIAL) {
      // 串行消息以阻塞当前线程的方式执行推送
      messagePusher.push(routeInfo).block();
    } else {
      // 并行消息异步推送
      messagePusher.push(routeInfo).subscribe();
    }
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    readFlushedMessage();
  }

  @Override
  public void destroy() throws Exception {
    this.startThread = false;
    flushMessages();
  }

  /**
   * 将队列中未推送的消息刷盘
   */
  private void flushMessages() throws Exception {
    Collection<BlockingQueue<RouteInfo>> values = queueMap.values();
    List<RouteInfo> list = new ArrayList<>();
    for (BlockingQueue<RouteInfo> queue : values) {
      final RouteInfo[] objects = (RouteInfo[]) queue.toArray();
      if (objects.length > 0) {
        list.addAll(Arrays.asList(objects));
      }
    }
    if (!list.isEmpty()) {
      File file = new File(FLUSH_FILE_NAME);
      try (FileOutputStream fos = new FileOutputStream(file);
           OutputStreamWriter osw = new OutputStreamWriter(fos);
           BufferedWriter bw = new BufferedWriter(osw)) {
        for (RouteInfo routeInfo : list) {
          bw.write(JsonUtils.toJsonString(routeInfo));
          bw.newLine();
        }
        bw.flush();
      }
    }
  }

  private void readFlushedMessage() throws Exception {
    File file = new File(FLUSH_FILE_NAME);
    if (file.exists()) {
      try (FileInputStream fis = new FileInputStream(file);
           InputStreamReader isr = new InputStreamReader(fis);
           BufferedReader br = new BufferedReader(isr)) {
        List<RouteInfo> routeInfos = br.lines()
            .map(line -> JsonUtils.parseJson(line, RouteInfo.class))
            .collect(Collectors.toList());
        submit(routeInfos).subscribe();
        //noinspection ResultOfMethodCallIgnored
        file.delete();
      }
    }
  }
}
