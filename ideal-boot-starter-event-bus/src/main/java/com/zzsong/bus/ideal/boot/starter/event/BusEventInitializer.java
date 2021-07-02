package com.zzsong.bus.ideal.boot.starter.event;

import cn.idealframework.event.listener.EventHandler;
import cn.idealframework.event.listener.EventHandlerFactory;
import cn.idealframework.event.listener.EventListenerInitializedListener;
import com.zzsong.bus.client.BusAdmin;
import com.zzsong.bus.client.spring.boot.starter.BusClientProperties;
import com.zzsong.bus.common.transfer.ResubscribeArgs;
import com.zzsong.bus.common.transfer.SubscriptionArgs;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 宋志宗 on 2021/7/2
 */
@RequiredArgsConstructor
public class BusEventInitializer implements EventListenerInitializedListener {
  private final BusAdmin admin;
  private final BusClientProperties busClientProperties;
  private final IdealEventBusProperties idealEventBusProperties;

  @Override
  public void completed() {
    boolean subscribe = idealEventBusProperties.isAutoSubscribe();
    Map<String, Map<String, EventHandler>> all = EventHandlerFactory.getAll();
    if (subscribe) {
      long applicationId = busClientProperties.getApplicationId();
      ResubscribeArgs resubscribeArgs = new ResubscribeArgs();
      resubscribeArgs.setApplicationId(applicationId);
      final List<SubscriptionArgs> subscriptionArgsList = new ArrayList<>();
      all.forEach((topic, map) -> map.forEach((name, listener) -> {
        SubscriptionArgs subscriptionArgs = new SubscriptionArgs();
        subscriptionArgs.setTopic(topic);
        subscriptionArgs.setListenerName(name);
        subscriptionArgs.setDelayExp(null);
        subscriptionArgs.setCondition(listener.getCondition().toExpression());
        subscriptionArgsList.add(subscriptionArgs);
      }));
      resubscribeArgs.setSubscriptionArgsList(subscriptionArgsList);
      try {
        admin.resubscribe(resubscribeArgs);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }
}
