package com.zzsong.bus.abs.converter;

import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.abs.transfer.SubscribeArgs;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@SuppressWarnings("DuplicatedCode")
public final class SubscriptionConverter {

  @Nonnull
  public static Subscription fromSubscribeArgs(@Nonnull SubscribeArgs subscribeArgs) {
    Subscription subscription = new Subscription();
    subscription.setSubscriberId(subscribeArgs.getSubscriberId());
    subscription.setTopic(subscribeArgs.getTopic());
    subscription.setCondition(subscribeArgs.getCondition());
    subscription.setBroadcast(subscribeArgs.isBroadcast());
    subscription.setRetryCount(subscribeArgs.getRetryCount());
    subscription.setStatus(subscription.getStatus());
    return subscription;
  }

  @Nonnull
  public static SubscriptionDetails toSubscriptionDetails(@Nonnull Subscription subscription) {
    SubscriptionDetails subscriptionDetails = new SubscriptionDetails();
//    subscriptionDetails.setSubscriberType();
//    subscriptionDetails.setApplication();
//    subscriptionDetails.setReceiveUrl();
    subscriptionDetails.setSubscriberId(subscription.getSubscriberId());
    subscriptionDetails.setTopic(subscription.getTopic());
    subscriptionDetails.setCondition(subscription.getCondition());
    subscriptionDetails.setBroadcast(subscription.isBroadcast());
    subscriptionDetails.setRetryCount(subscription.getRetryCount());
    subscriptionDetails.setStatus(subscription.getStatus());
    return subscriptionDetails;
  }
}
