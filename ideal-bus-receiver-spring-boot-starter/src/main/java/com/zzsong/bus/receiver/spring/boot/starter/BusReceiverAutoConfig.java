package com.zzsong.bus.receiver.spring.boot.starter;

import com.zzsong.bus.receiver.SpringBusReceiver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 宋志宗 on 2020/9/18
 */
@Configuration
@EnableConfigurationProperties({BusReceiveProperties.class})
public class BusReceiverAutoConfig {

  @Bean
  public SpringBusReceiver springBusReceiver(BusReceiveProperties properties) {
    return new SpringBusReceiver(properties.getCorePoolSize(), properties.getMaximumPoolSize());
  }
}
