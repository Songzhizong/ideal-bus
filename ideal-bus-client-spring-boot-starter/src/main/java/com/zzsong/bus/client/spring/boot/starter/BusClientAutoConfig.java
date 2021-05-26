package com.zzsong.bus.client.spring.boot.starter;

import com.zzsong.bus.client.*;
import com.zzsong.bus.client.impl.*;
import com.zzsong.bus.client.rsocket.ReceiveRSocketChannel;
import com.zzsong.bus.client.rsocket.ReceiveRSocketChannelImpl;
import com.zzsong.bus.common.share.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({BusClientProperties.class})
public class BusClientAutoConfig {

  private final BusClientProperties properties;

  @Value("${server.port:8080}")
  private Integer serverPort;

  public BusClientAutoConfig(@Nonnull BusClientProperties properties) {
    this.properties = properties;
  }

  @Bean
  public WebClient busClientWebClient(@Nullable ReactorLoadBalancerExchangeFilterFunction lbFunction) {
    String brokerHttpBaseUrl = properties.getBrokerHttpBaseUrl();
    if (StringUtils.isBlank(brokerHttpBaseUrl)) {
      throw new IllegalArgumentException("ideal.bus.broker-http-base-url must be not blank");
    }
    WebClient.Builder builder = WebClient.builder();
    if (brokerHttpBaseUrl.startsWith("lb://")) {
      if (lbFunction == null) {
        throw new IllegalArgumentException("ReactorLoadBalancerExchangeFilterFunction is null");
      }
      builder.filter(lbFunction);
    }
    return builder.build();
  }

  @Bean
  public BusAdmin busAdmin(@Nonnull WebClient busClientWebClient) {
    String brokerHttpBaseUrl = properties.getBrokerHttpBaseUrl();
    return new BusAdminImpl(brokerHttpBaseUrl, busClientWebClient);
  }

  @Bean
  public EventPublisher eventPublisher(@Nonnull WebClient busClientWebClient) {
    BusPublishProperties publish = properties.getPublish();
    String brokerHttpBaseUrl = properties.getBrokerHttpBaseUrl();
    if (publish.isEnabled()) {
      return new HttpEventPublisher(brokerHttpBaseUrl, busClientWebClient);
    } else {
      return new NoneEventPublisher();
    }
  }

  @Bean
  public ListenerInitializer listenerInitializer() {
    return new SpringListenerInitializer();
  }

  @Bean
  @ConditionalOnMissingBean
  public EventConsumer eventConsumer() {
    return new AutoDeliverEventConsumer();
  }

  @Bean
  public ConsumerExecutor consumerExecutor(EventConsumer eventConsumer) {
    BusConsumerProperties consumer = properties.getConsumer();
    int corePoolSize = consumer.getCorePoolSize();
    int maximumPoolSize = consumer.getMaximumPoolSize();
    if (corePoolSize < 1) {
      corePoolSize = 1;
    }
    return new ThreadPoolConsumerExecutor(corePoolSize, maximumPoolSize, eventConsumer);
  }

  @Bean
  public List<ReceiveRSocketChannel> receiveRSocketChannels(ConsumerExecutor consumerExecutor) {
    BusConsumerProperties consumer = properties.getConsumer();
    if (!consumer.isEnabled()) {
      log.info("Event consumer disabled");
      return Collections.emptyList();
    }
    String ip = IpUtil.getIp();
    long applicationId = properties.getApplicationId();
    String accessToken = properties.getAccessToken();
    String brokerAddresses = consumer.getBrokerAddresses();
    String[] addresses = StringUtils.split(brokerAddresses, ",");
    List<ReceiveRSocketChannel> channels = new ArrayList<>();
    for (String address : addresses) {
      String[] split = StringUtils.split(address, ":");
      if (split.length != 2) {
        throw new IllegalArgumentException("Illegal broker address: " + address);
      }
      String brokerIp = split[0];
      String brokerPortStr = split[1];
      int brokerPort;
      try {
        brokerPort = Integer.parseInt(brokerPortStr);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Illegal broker address: " + address);
      }
      ReceiveRSocketChannelImpl channel = new ReceiveRSocketChannelImpl(brokerIp, brokerPort,
          applicationId, ip + ":" + serverPort, accessToken, consumerExecutor);
      channels.add(channel);
    }
    return channels;
  }
}
