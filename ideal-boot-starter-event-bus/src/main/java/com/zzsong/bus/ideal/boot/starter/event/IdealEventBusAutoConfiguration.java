package com.zzsong.bus.ideal.boot.starter.event;

import cn.idealframework.event.listener.EventDeliverer;
import cn.idealframework.event.listener.EventListenerInitializer;
import cn.idealframework.event.publisher.EventPublisher;
import com.zzsong.bus.client.BusAdmin;
import com.zzsong.bus.client.EventConsumer;
import com.zzsong.bus.client.spring.boot.starter.BusClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 宋志宗 on 2021/6/2
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({IdealEventBusProperties.class})
public class IdealEventBusAutoConfiguration {
  private final BusAdmin admin;
  private final BusClientProperties busClientProperties;
  private final IdealEventBusProperties idealEventBusProperties;

  @Bean
  public BusEventInitializer busEventInitializer(EventListenerInitializer eventListenerInitializer) {
    BusEventInitializer busEventInitializer = new BusEventInitializer(admin, busClientProperties, idealEventBusProperties);
    eventListenerInitializer.addCompleteListener(busEventInitializer);
    return busEventInitializer;
  }

  @Bean
  public EventConsumer eventConsumer(EventDeliverer eventDeliverer) {
    return new BusEventConsumer(eventDeliverer);
  }

  @Bean("eventPublisher")
  public EventPublisher eventPublisher(com.zzsong.bus.client.EventPublisher busEventPublisher) {
    return new BusEventPublisherImpl(busEventPublisher);
  }
}
