package com.zzsong.bus.client.spring.boot.starter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zzsong.bus.client.SpringBusClient;
import com.zzsong.bus.receiver.SpringBusReceiver;
import com.zzsong.common.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({BusClientProperties.class, BusReceiveProperties.class})
public class BusClientAutoConfig {

  @Nonnull
  private final BusClientProperties properties;


  @Value("${spring.application.name:}")
  private String applicationName;

  @Value("${server.port:-1}")
  private Integer serverPort;

  public BusClientAutoConfig(@Nonnull BusClientProperties properties) {
    this.properties = properties;
  }

  @Bean
  public SpringBusReceiver springBusReceiver(BusReceiveProperties properties) {
    return new SpringBusReceiver(properties.getCorePoolSize(), properties.getMaximumPoolSize());
  }

  @Bean
  public SpringBusClient springBusClient(SpringBusReceiver springBusReceiver) {
    final SpringBusClient busClient = new SpringBusClient(springBusReceiver);
    busClient.setApplicationId(properties.getApplicationId());
    busClient.setBrokerAddresses(properties.getBrokerAddresses());
    busClient.setAccessToken(properties.getAccessToken());
    final String ip = IpUtil.getIp();
    final int port = serverPort == null ? 8080 : serverPort;
    busClient.setClientIpPort(ip + ":" + port);
    busClient.setAutoSubscribe(properties.isAutoSubscribe());
    return busClient;
  }
}
