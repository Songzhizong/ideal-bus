package com.zzsong.bus.storage.mongo.converter;

import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.storage.mongo.document.SubscriptionDo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
@SuppressWarnings("DuplicatedCode")
public final class SubscriptionDoConverter {

  @Nonnull
  public static Subscription toSubscription(@Nonnull SubscriptionDo mongoDo) {
    Subscription subscription = new Subscription();
    subscription.setSubscriptionId(mongoDo.getSubscriptionId());
    subscription.setSubscriberId(mongoDo.getSubscriberId());
    subscription.setTopic(mongoDo.getTopic());
    subscription.setCondition(mongoDo.getCondition());
    subscription.setBroadcast(mongoDo.isBroadcast());
    subscription.setRetryCount(mongoDo.getRetryCount());
    subscription.setStatus(mongoDo.getStatus());
    return subscription;
  }

  @Nonnull
  public static SubscriptionDo fromSubscription(@Nonnull Subscription subscription) {
    SubscriptionDo subscriptionDo = new SubscriptionDo();
    subscriptionDo.setSubscriptionId(subscription.getSubscriptionId());
    subscriptionDo.setSubscriberId(subscription.getSubscriberId());
    subscriptionDo.setTopic(subscription.getTopic());
    subscriptionDo.setCondition(subscription.getCondition());
    subscriptionDo.setBroadcast(subscription.isBroadcast());
    subscriptionDo.setRetryCount(subscription.getRetryCount());
    subscriptionDo.setStatus(subscription.getStatus());
    return subscriptionDo;
  }
}
