package com.zzsong.bus.abs.converter;

import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.abs.transfer.SubscribeArgs;
import com.zzsong.bus.common.transfer.SubscriptionArgs;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@SuppressWarnings("DuplicatedCode")
public final class SubscriptionConverter {
  private SubscriptionConverter() {
  }

  @Nonnull
  public static Subscription fromSubscribeArgs(@Nonnull SubscribeArgs subscribeArgs) {
    Subscription subscription = new Subscription();
    subscription.setApplicationId(subscribeArgs.getApplicationId());
    subscription.setTopic(subscribeArgs.getTopic());
    subscription.setCondition(subscribeArgs.getCondition());
    subscription.setBroadcast(subscribeArgs.isBroadcast());
    subscription.setRetryCount(subscribeArgs.getRetryCount());
    subscription.setStatus(subscription.getStatus());
    return subscription;
  }

  @Nonnull
  public static Subscription fromSubscriptionArgs(@Nonnull SubscriptionArgs args) {
    Subscription subscription = new Subscription();
//    subscription.setSubscriptionId();
//    subscription.setApplicationId();
    subscription.setTopic(args.getTopic());
    subscription.setListenerName(args.getListenerName());
    String delayExp = args.getDelayExp();
    if (delayExp != null) {
      subscription.setDelayExp(delayExp);
    }
    String condition = args.getCondition();
    if (condition != null) {
      subscription.setCondition(condition);
    }
    Boolean broadcast = args.getBroadcast();
    if (broadcast != null) {
      subscription.setBroadcast(broadcast);
    }
    Integer retryCount = args.getRetryCount();
    if (retryCount != null) {
      subscription.setRetryCount(retryCount);
    }
//    subscription.setStatus();
    return subscription;
  }

  @Nonnull
  public static SubscriptionDetails toSubscriptionDetails(@Nonnull Subscription subscription) {
    SubscriptionDetails subscriptionDetails = new SubscriptionDetails();
    subscriptionDetails.setCondition(subscription.getCondition());
//      subscriptionDetails.setApplicationType();
//      subscriptionDetails.setExternalApp();
//      subscriptionDetails.setReceiveUrl();
//      subscriptionDetails.setConditionGroup();
    subscriptionDetails.setSubscriptionId(subscription.getSubscriptionId());
    subscriptionDetails.setApplicationId(subscription.getApplicationId());
    subscriptionDetails.setTopic(subscription.getTopic());
    subscriptionDetails.setListenerName(subscription.getListenerName());
    subscriptionDetails.setDelayExp(subscription.getDelayExp());
    subscriptionDetails.setCondition(subscription.getCondition());
    subscriptionDetails.setBroadcast(subscription.isBroadcast());
    subscriptionDetails.setRetryCount(subscription.getRetryCount());
    subscriptionDetails.setConsumeType(subscription.getConsumeType());
    subscriptionDetails.setStatus(subscription.getStatus());
    return subscriptionDetails;
  }
}
