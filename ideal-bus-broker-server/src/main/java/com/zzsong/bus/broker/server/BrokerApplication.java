package com.zzsong.bus.broker.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 宋志宗 on 2020/7/14
 */
@SpringBootApplication(scanBasePackages = "com.zzsong.bus")
public class BrokerApplication {
  private static final Logger log = LoggerFactory.getLogger(BrokerApplication.class);
  public static void main(String[] args) {
    // 将 reactor 线程数量设置为 cpu * 4
    int availableProcessors = Runtime.getRuntime().availableProcessors();
    int ioWorkerCount = availableProcessors << 2;
    System.setProperty("reactor.netty.ioWorkerCount", ioWorkerCount + "");
    SpringApplication.run(BrokerApplication.class, args);
    log.info("reactor.netty.ioWorkerCount = {}", ioWorkerCount);
  }
}
