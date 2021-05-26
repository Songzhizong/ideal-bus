package com.zzsong.bus.broker.core.exchanger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author 宋志宗 on 2021/5/19
 */
@Slf4j
@Component
public class DelayMessageScheduler implements ApplicationRunner {
  private static final int PRE_READ_COUNT = 1000;
  private static final long PRE_READ_MILLS = 5000L;

  private void start() {

  }

  private void stop() {

  }

  @Override
  public void run(ApplicationArguments args) {
    this.start();
    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
  }
}
