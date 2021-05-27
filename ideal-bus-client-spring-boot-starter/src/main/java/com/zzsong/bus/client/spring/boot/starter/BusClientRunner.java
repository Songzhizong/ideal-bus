package com.zzsong.bus.client.spring.boot.starter;

import com.zzsong.bus.client.BusAdmin;
import com.zzsong.bus.client.ListenerInitializer;
import com.zzsong.bus.client.listener.IEventListener;
import com.zzsong.bus.client.listener.ListenerFactory;
import com.zzsong.bus.client.rsocket.ReceiveRSocketChannel;
import com.zzsong.bus.common.transfer.ResubscribeArgs;
import com.zzsong.bus.common.transfer.SubscriptionArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 宋志宗 on 2021/5/25
 */
@CommonsLog
@RequiredArgsConstructor
public class BusClientRunner implements ApplicationRunner, DisposableBean {
  private final BusAdmin admin;
  private final ListenerInitializer listenerInitializer;
  private final BusClientProperties properties;
  private final List<ReceiveRSocketChannel> receiveRSocketChannels;

  @Override
  public void run(ApplicationArguments args) {
    BusConsumerProperties consumer = properties.getConsumer();
    boolean enabled = consumer.isEnabled();
    if (!enabled) {
      return;
    }

    // 1. 初始化监听器
    listenerInitializer.init();
    Map<String, Map<String, IEventListener>> all = ListenerFactory.getAll();
    Set<String> strings = all.keySet();
    log.info("Listen topics: " + StringUtils.join(strings, ", "));

    // 2. 更新订阅关系
    boolean subscribe = properties.isAutoSubscribe();
    if (subscribe) {
      long applicationId = properties.getApplicationId();
      ResubscribeArgs resubscribeArgs = new ResubscribeArgs();
      resubscribeArgs.setApplicationId(applicationId);
      final List<SubscriptionArgs> subscriptionArgsList = new ArrayList<>();
      all.forEach((topic, map) -> map.forEach((name, listener) -> {
        SubscriptionArgs subscriptionArgs = new SubscriptionArgs();
        subscriptionArgs.setTopic(topic);
        subscriptionArgs.setListenerName(name);
        subscriptionArgs.setDelayExp(listener.getDelayExp());
        subscriptionArgs.setCondition(listener.getCondition());
        subscriptionArgsList.add(subscriptionArgs);
      }));
      resubscribeArgs.setSubscriptionArgsList(subscriptionArgsList);
      admin.resubscribe(resubscribeArgs);
    }

    // 3. 建立消费通道
    for (ReceiveRSocketChannel channel : receiveRSocketChannels) {
      channel.connect();
    }
  }

  @Override
  public void destroy() {
    // 3. 建立消费通道
    for (ReceiveRSocketChannel channel : receiveRSocketChannels) {
      channel.close();
    }
  }
}
