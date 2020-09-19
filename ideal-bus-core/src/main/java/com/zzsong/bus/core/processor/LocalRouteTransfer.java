package com.zzsong.bus.core.processor;

import com.zzsong.bus.abs.core.MessagePusher;
import com.zzsong.bus.abs.core.RouteTransfer;
import com.zzsong.bus.abs.domain.RouteInfo;
import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.common.utils.JsonUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/19 8:08 下午
 */
@Component
public class LocalRouteTransfer implements RouteTransfer, ApplicationRunner, DisposableBean {
  private final ConcurrentMap<Long, BlockingQueue<RouteInfo>> queueMap = new ConcurrentHashMap<>();
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private final List<Thread> customerThreadList = new ArrayList<>();
  private volatile boolean startThread = true;
  private static final String FLUSH_FILE_NAME = "bus_flushd_route_info";

  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final MessagePusher messagePusher;

  public LocalRouteTransfer(@Nonnull LocalCache localCache,
                            @Nonnull MessagePusher messagePusher) {
    this.localCache = localCache;
    this.messagePusher = messagePusher;
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
      while (startThread && queue.isEmpty()) {
        try {
          RouteInfo routeInfo = queue.poll(5, TimeUnit.SECONDS);
          if (routeInfo != null) {
            SubscriptionDetails subscription = localCache.getSubscription(routeInfo.getSubscriptionId());
            if (subscription != null
                && subscription.getConsumeType() == Subscription.CONSUME_TYPE_SERIAL) {
              // 串行消息以阻塞当前线程的方式执行推送
              messagePusher.push(routeInfo).block();
            } else {
              // 并行消息异步推送
              messagePusher.push(routeInfo).subscribe();
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
    File file = new File(FLUSH_FILE_NAME);
    try (FileOutputStream fos = new FileOutputStream(file);
         OutputStreamWriter osw = new OutputStreamWriter(fos);
         BufferedWriter bw = new BufferedWriter(osw)) {
      for (BlockingQueue<RouteInfo> queue : values) {
        Object[] objects = queue.toArray();
        for (Object object : objects) {
          bw.write(JsonUtils.toJsonString(object));
          bw.newLine();
        }
      }
      bw.flush();
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
      }
    }
  }
}
