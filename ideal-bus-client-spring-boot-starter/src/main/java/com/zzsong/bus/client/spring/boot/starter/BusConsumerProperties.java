package com.zzsong.bus.client.spring.boot.starter;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
public class BusConsumerProperties {
  /**
   * 是启用消费
   */
  private boolean enabled = false;

  private String brokerAddresses = "";

  /**
   * 核心线程数
   */
  private int corePoolSize = 0;
  /**
   * 最大线程数
   */
  private int maximumPoolSize = 64;
}
