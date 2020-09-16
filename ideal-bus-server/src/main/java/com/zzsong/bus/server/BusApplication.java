package com.zzsong.bus.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author 宋志宗 on 2020/7/14
 */
@EnableDiscoveryClient
@EnableTransactionManagement
@SpringBootApplication(scanBasePackages = "com.zzsong.bus")
public class BusApplication {
  public static void main(String[] args) {
    SpringApplication.run(BusApplication.class, args);
  }
}
