package com.zzsong.bus.base.mongo.converter;

import com.zzsong.bus.base.domain.Subscription;
import com.zzsong.bus.base.mongo.document.SubscriptionMongoDo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
public final class SubscriptionMongoDoConverter {

  @Nonnull
  public static Subscription toSubscription(@Nonnull SubscriptionMongoDo mongoDo) {
    Subscription subscription = new Subscription();
    subscription.setSubscriberId(mongoDo.getSubscriberId());
    subscription.setTopic(mongoDo.getTopic());
    subscription.setCondition(mongoDo.getCondition());
    subscription.setBroadcast(mongoDo.isBroadcast());
    subscription.setRetryCount(mongoDo.getRetryCount());
    return subscription;
  }

  @Nonnull
  public static SubscriptionMongoDo fromSubscription(@Nonnull Subscription subscription) {
    SubscriptionMongoDo subscriptionMongoDo = new SubscriptionMongoDo();
//    subscriptionMongoDo.setSubscriptionId();
    subscriptionMongoDo.setSubscriberId(subscription.getSubscriberId());
    subscriptionMongoDo.setTopic(subscription.getTopic());
    subscriptionMongoDo.setCondition(subscription.getCondition());
    subscriptionMongoDo.setBroadcast(subscription.isBroadcast());
    subscriptionMongoDo.setRetryCount(subscription.getRetryCount());
    return subscriptionMongoDo;
  }
}
