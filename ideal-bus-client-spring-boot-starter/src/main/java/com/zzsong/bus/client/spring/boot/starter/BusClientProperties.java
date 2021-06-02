package com.zzsong.bus.client.spring.boot.starter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "ideal.bus")
public class BusClientProperties {

  /** 应用id */
  private long applicationId;

  /** 服务端http基础访问地址 */
  private String brokerHttpBaseUrl;

  private String accessToken = "";

  /** 是否开启自动刷新订阅关系 */
  private boolean autoSubscribe = false;

  /** 发布配置 */
  @NestedConfigurationProperty
  private BusPublishProperties publish = new BusPublishProperties();

  /** 消费配置 */
  @NestedConfigurationProperty
  private BusConsumerProperties consumer = new BusConsumerProperties();
}
