package com.zzsong.bus.broker.core;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.broker.admin.service.RouteInstanceService;
import com.zzsong.bus.broker.config.BusProperties;
import com.zzsong.bus.broker.core.transfer.RouteTransfer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

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
  private static final int PRE_READ_COUNT = 1000;
  private static final long PRE_READ_MILLS = 5000L;

  private final ConcurrentMap<Integer, List<RouteInstance>> ringData
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
  private final RouteInstanceService routeInstanceService;

  public TimingScheduler(@Nonnull BusProperties busProperties,
                         @Nonnull RouteTransfer routeTransfer,
                         @Nonnull RouteInstanceService routeInstanceService) {
    this.busProperties = busProperties;
    this.routeTransfer = routeTransfer;
    this.routeInstanceService = routeInstanceService;
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
        long maxNextTime = nowTime + PRE_READ_MILLS;
        int nodeId = busProperties.getNodeId();
        boolean preReadSuc = true;
        try {
          List<RouteInstance> routeInstanceList = routeInstanceService
              .loadDelayed(maxNextTime, PRE_READ_COUNT, nodeId)
              .block();
          if (routeInstanceList != null && routeInstanceList.size() > 0) {
            log.debug("loadDelayed: {}", routeInstanceList.size());
            List<RouteInstance> submitList = new ArrayList<>();
            for (RouteInstance routeInstance : routeInstanceList) {
              long nextPushTime = routeInstance.getNextPushTime();
              routeInstance.setNextPushTime(-1);
              if (nowTime >= nextPushTime) {
                submitList.add(routeInstance);
              } else {
                int ringSecond = (int) (nextPushTime / 1000 % 60);
                pushTimeRing(ringSecond, routeInstance);
              }
            }
            if (submitList.size() > 0) {
              Flux.fromIterable(submitList)
                  .flatMap(routeTransfer::submit)
                  .collectList()
                  .subscribe();
            }
          } else {
            preReadSuc = false;
          }
        } catch (Exception e) {
          log.warn("", e);
          continue;
        }
        long cost = System.currentTimeMillis() - start;
        if (cost < 1000) {
          // 如果未来preReadMills秒都没有数据, 那就休眠最多preReadMills秒, 反之最多休眠1秒
          long sleepMills = (preReadSuc ? 1000 : PRE_READ_MILLS)
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
          List<RouteInstance> tmpData = ringData.remove(second);
          if (tmpData != null) {
            Flux.fromIterable(tmpData)
                .flatMap(routeTransfer::submit)
                .collectList()
                .subscribe();
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


  private void pushTimeRing(int ringSecond, RouteInstance routeInstance) {
    List<RouteInstance> routeInstances
        = ringData.computeIfAbsent(ringSecond, k -> new ArrayList<>());
    routeInstances.add(routeInstance);
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
        List<RouteInstance> viewList = ringData.get(second);
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
