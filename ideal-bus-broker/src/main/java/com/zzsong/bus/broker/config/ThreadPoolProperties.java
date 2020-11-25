package com.zzsong.bus.broker.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 宋志宗 on 2020/8/25
 */
@Getter
@Setter
public class ThreadPoolProperties {
  private int corePoolSize = -1;
  private int maximumPoolSize = -1;
  private int workQueueSize = 200;
}
