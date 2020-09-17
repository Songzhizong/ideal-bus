package com.zzsong.bus.abs.mongo.converter;

import com.zzsong.bus.abs.domain.Subscriber;
import com.zzsong.bus.abs.mongo.document.SubscriberDo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@SuppressWarnings("DuplicatedCode")
public final class SubscriberDoConverter {

  @Nonnull
  public static SubscriberDo fromSubscriber(@Nonnull Subscriber subscriber) {
    SubscriberDo subscriberDo = new SubscriberDo();
    subscriberDo.setSubscriberId(subscriber.getSubscriberId());
    subscriberDo.setTitle(subscriber.getTitle());
    subscriberDo.setDesc(subscriber.getDesc());
    subscriberDo.setSubscriberType(subscriber.getSubscriberType());
    subscriberDo.setAppName(subscriber.getAppName());
    subscriberDo.setApplication(subscriber.getApplication());
    subscriberDo.setReceiveUrl(subscriber.getReceiveUrl());
    return subscriberDo;
  }

  public static Subscriber toSubscriber(@Nonnull SubscriberDo subscriberDo) {
    Subscriber subscriber = new Subscriber();
    subscriber.setSubscriberId(subscriberDo.getSubscriberId());
    subscriber.setTitle(subscriberDo.getTitle());
    subscriber.setDesc(subscriberDo.getDesc());
    subscriber.setSubscriberType(subscriberDo.getSubscriberType());
    subscriber.setAppName(subscriberDo.getAppName());
    subscriber.setApplication(subscriberDo.getApplication());
    subscriber.setReceiveUrl(subscriberDo.getReceiveUrl());
    return subscriber;
  }
}
