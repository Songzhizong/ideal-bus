package com.zzsong.bus.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 宋志宗 on 2020/7/14
 */
@SpringBootApplication(scanBasePackages = "com.zzsong.bus")
public class BrokerApplication {
  public static void main(String[] args) {
    System.setProperty("reactor.netty.ioWorkerCount", "64");
    SpringApplication.run(BrokerApplication.class, args);
  }
}
