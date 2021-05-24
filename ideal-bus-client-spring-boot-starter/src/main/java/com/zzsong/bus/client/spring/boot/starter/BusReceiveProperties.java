package com.zzsong.bus.client.spring.boot.starter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "ideal.bus.receive")
public class BusReceiveProperties {
  /**
   * 是启用消费
   */
  private boolean enabled = false;
  /**
   * 核心线程数
   */
  private int corePoolSize = 0;
  /**
   * 最大线程数
   */
  private int maximumPoolSize = 64;
}
