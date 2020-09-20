package com.zzsong.bus.core.processor;

import com.zzsong.bus.abs.core.RouteTransfer;
import com.zzsong.bus.abs.domain.RouteInfo;
import com.zzsong.bus.core.admin.service.RouteInfoService;
import com.zzsong.bus.core.config.BusProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗 on 2020/9/20 7:50 下午
 */
@Slf4j
@Component
public class TimingScheduler implements SmartInitializingSingleton {
  private static final int preReadCount = 2000;
  private static final long preReadMills = 5000L;

  private final ConcurrentMap<Integer, List<RouteInfo>> ringData
      = new ConcurrentHashMap<>();
  private Thread scheduleThread;
  private Thread ringThread;
  private volatile boolean scheduleThreadToStop = false;
  private volatile boolean ringThreadToStop = false;

  @Nonnull
  private final BusProperties busProperties;
  @Nonnull
  private final RouteTransfer routeTransfer;
  @Nonnull
  private final RouteInfoService routeInfoService;

  public TimingScheduler(@Nonnull BusProperties busProperties,
                         @Nonnull RouteTransfer routeTransfer,
                         @Nonnull RouteInfoService routeInfoService) {
    this.busProperties = busProperties;
    this.routeTransfer = routeTransfer;
    this.routeInfoService = routeInfoService;
  }

  private void start() {
    scheduleThread = new Thread(() -> {
      try {
        TimeUnit.MILLISECONDS.sleep(5000 - (System.currentTimeMillis() % 1000));
      } catch (InterruptedException e) {
        if (!scheduleThreadToStop) {
          String errMsg = e.getClass().getName() + ": " + e.getMessage();
          log.error("schedule-thread exception: {}", errMsg);
        }
      }
      while (!scheduleThreadToStop) {
        long start = System.currentTimeMillis();
        long nowTime = System.currentTimeMillis();
        long maxNextTime = nowTime + preReadMills;
        int nodeId = busProperties.getNodeId();
        boolean preReadSuc = true;
        List<RouteInfo> routeInfoList = routeInfoService
            .loadDelayed(maxNextTime, preReadCount, nodeId)
            .block();
        if (routeInfoList != null && routeInfoList.size() > 0) {
          log.debug("loadDelayed: {}", routeInfoList.size());
          List<RouteInfo> submitList = new ArrayList<>();
          for (RouteInfo routeInfo : routeInfoList) {
            long nextPushTime = routeInfo.getNextPushTime();
            if (nowTime > nextPushTime + preReadMills) {
              log.warn("过期任务: {}", routeInfo.getInstanceId());
            } else if (nowTime >= nextPushTime) {
              submitList.add(routeInfo);
            } else {
              int ringSecond = (int) (nextPushTime / 1000 % 60);
              pushTimeRing(ringSecond, routeInfo);
            }
          }
          if (submitList.size() > 0) {
            routeTransfer.submit(submitList).subscribe();
          }
        } else {
          log.debug("loadDelayed: empty");
          preReadSuc = false;
        }
        long cost = System.currentTimeMillis() - start;
        if (cost < 1000) {
          // 如果未来preReadMills秒都没有数据, 那就休眠最多preReadMills秒, 反之最多休眠1秒
          long sleepMills = (preReadSuc ? 1000 : preReadMills)
              - (System.currentTimeMillis() % 1000);
          try {
            TimeUnit.MILLISECONDS.sleep(sleepMills);
          } catch (InterruptedException e) {
            if (!scheduleThreadToStop) {
              String errMsg = e.getClass().getName() + ": " + e.getMessage();
              log.error("schedule-trigger-thread exception: {}", errMsg);
            }
          }
        }
      }
      log.info("schedule-thread stop");
    });
    scheduleThread.setDaemon(true);
    scheduleThread.setName("schedule-thread");
    scheduleThread.start();

    ringThread = new Thread(() -> {
      try {
        TimeUnit.MILLISECONDS.sleep(1000 - (System.currentTimeMillis() % 1000));
      } catch (InterruptedException e) {
        if (!scheduleThreadToStop) {
          String errMsg = e.getClass().getName() + ": " + e.getMessage();
          log.error("ring-thread exception: {}", errMsg);
        }
      }
      while (!ringThreadToStop) {
        int nowSecond = Calendar.getInstance().get(Calendar.SECOND);
        for (int i = 0; i < 2; i++) {
          int second = (nowSecond + 60 - i) % 60;
          List<RouteInfo> tmpData = ringData.remove(second);
          if (tmpData != null) {
            routeTransfer.submit(tmpData).subscribe();
          }
        }
        try {
          TimeUnit.MILLISECONDS.sleep(1000 - (System.currentTimeMillis() % 1000));
        } catch (InterruptedException e) {
          if (!scheduleThreadToStop) {
            String errMsg = e.getClass().getName() + ": " + e.getMessage();
            log.error("ring-thread exception: {}", errMsg);
          }
        }
      }
      log.info("ring-thread stop");
    });
    ringThread.setDaemon(true);
    ringThread.setName("ring-thread");
    ringThread.start();
  }


  private void pushTimeRing(int ringSecond, RouteInfo routeInfo) {
    List<RouteInfo> routeInfos
        = ringData.computeIfAbsent(ringSecond, k -> new ArrayList<>());
    routeInfos.add(routeInfo);
  }

  public void stop() {
    if (scheduleThreadToStop || ringThreadToStop) {
      return;
    }
    // 1、stop schedule
    scheduleThreadToStop = true;
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      log.error("TimingSchedule stop exception: Interrupted");
    }
    if (scheduleThread != null
        && scheduleThread.getState() != Thread.State.TERMINATED) {
      scheduleThread.interrupt();
      try {
        scheduleThread.join();
      } catch (InterruptedException e) {
        log.error("TimingSchedule stop exception: scheduleThread.join interrupted");
      }
    }

    // if has ring data
    boolean hasRingData = false;
    if (!ringData.isEmpty()) {
      for (Integer second : ringData.keySet()) {
        List<RouteInfo> viewList = ringData.get(second);
        if (viewList != null && viewList.size() > 0) {
          hasRingData = true;
          break;
        }
      }
    }
    if (hasRingData) {
      try {
        TimeUnit.SECONDS.sleep(8);
      } catch (InterruptedException e) {
        log.error("TimingSchedule stop exception: Interrupted");
      }
    }

    ringThreadToStop = true;
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      log.error("TimingSchedule stop exception: Interrupted");
    }
    if (ringThread != null
        && ringThread.getState() != Thread.State.TERMINATED) {
      ringThread.interrupt();
      try {
        ringThread.join();
      } catch (InterruptedException e) {
        log.error("TimingSchedule stop exception: ringThread.join interrupted");
      }
    }
    log.info("TimingSchedule stop");
  }

  @Override
  public void afterSingletonsInstantiated() {
    this.start();
    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
  }
}
