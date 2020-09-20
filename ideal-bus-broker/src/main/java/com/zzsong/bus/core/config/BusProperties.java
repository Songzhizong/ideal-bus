package com.zzsong.bus.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
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
   * 本地缓存的刷新间隔
   */
  private Duration refreshLocalCacheInterval = Duration.ofMinutes(10);

  @NestedConfigurationProperty
  private ThreadPoolProperties blockPool = new ThreadPoolProperties();
}
