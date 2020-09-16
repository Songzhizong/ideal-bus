package com.zzsong.bus.base.mongo.converter;

import com.zzsong.bus.base.domain.Subscriber;
import com.zzsong.bus.base.mongo.document.SubscriberMongoDo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@SuppressWarnings("DuplicatedCode")
public class SubscriberMongoDoConverter {

  @Nonnull
  public static SubscriberMongoDo fromSubscriber(@Nonnull Subscriber subscriber) {
    SubscriberMongoDo subscriberMongoDo = new SubscriberMongoDo();
    subscriberMongoDo.setSubscriberId(subscriber.getSubscriberId());
    subscriberMongoDo.setTitle(subscriber.getTitle());
    subscriberMongoDo.setDesc(subscriber.getDesc());
    subscriberMongoDo.setSubscriberType(subscriber.getSubscriberType());
    subscriberMongoDo.setAppName(subscriber.getAppName());
    subscriberMongoDo.setApplication(subscriber.getApplication());
    subscriberMongoDo.setReceiveUrl(subscriber.getReceiveUrl());
    return subscriberMongoDo;
  }

  public static Subscriber toSubscriber(@Nonnull SubscriberMongoDo subscriberMongoDo) {
    Subscriber subscriber = new Subscriber();
    subscriber.setSubscriberId(subscriberMongoDo.getSubscriberId());
    subscriber.setTitle(subscriberMongoDo.getTitle());
    subscriber.setDesc(subscriberMongoDo.getDesc());
    subscriber.setSubscriberType(subscriberMongoDo.getSubscriberType());
    subscriber.setAppName(subscriberMongoDo.getAppName());
    subscriber.setApplication(subscriberMongoDo.getApplication());
    subscriber.setReceiveUrl(subscriberMongoDo.getReceiveUrl());
    return subscriber;
  }
}
