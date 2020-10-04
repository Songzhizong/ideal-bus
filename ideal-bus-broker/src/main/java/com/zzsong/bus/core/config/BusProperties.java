package com.zzsong.bus.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@Component
@ConfigurationProperties("ideal.bus")
public class BusProperties {
  /**
   * 当前节点的nodeId, 集群部署每个节点都必须有自己的id
   */
  private int nodeId = -1;
  /**
   * 本地缓存的刷新间隔
   */
  private Duration refreshLocalCacheInterval = Duration.ofMinutes(10);
}
