package com.zzsong.bus.client.spring.boot.starter;

import com.zzsong.bus.client.BusClient;
import com.zzsong.bus.client.DefaultBusClient;
import com.zzsong.bus.client.SpringBusClient;
import com.zzsong.bus.common.share.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({BusClientProperties.class, BusReceiveProperties.class})
public class BusClientAutoConfig {

  @Nonnull
  private final BusClientProperties clientProperties;
  @Nonnull
  private final BusReceiveProperties receiveProperties;

  @Value("${server.port:-1}")
  private Integer serverPort;

  public BusClientAutoConfig(@Nonnull BusClientProperties clientProperties,
                             @Nonnull BusReceiveProperties receiveProperties) {
    this.clientProperties = clientProperties;
    this.receiveProperties = receiveProperties;
  }

  @Bean
  public BusClient busCLient() {
    if (clientProperties.isEnabled()) {
      final int corePoolSize = receiveProperties.getCorePoolSize();
      final int maximumPoolSize = receiveProperties.getMaximumPoolSize();
      final SpringBusClient busClient = new SpringBusClient(corePoolSize, maximumPoolSize);
      busClient.setApplicationId(clientProperties.getApplicationId());
      busClient.setBrokerAddresses(clientProperties.getBrokerAddresses());
      busClient.setAccessToken(clientProperties.getAccessToken());
      final String ip = IpUtil.getIp();
      final int port = serverPort == null ? 8080 : serverPort;
      busClient.setClientIpPort(ip + ":" + port);
      busClient.setAutoSubscribe(clientProperties.isAutoSubscribe());
      return busClient;
    } else {
      log.warn("ideal bus is disabled");
      return new DefaultBusClient();
    }
  }
}
