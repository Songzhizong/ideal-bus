package com.zzsong.bus.base.converter;

import com.zzsong.bus.base.domain.Subscriber;
import com.zzsong.bus.base.transfer.CreateSubscriberArgs;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@SuppressWarnings("DuplicatedCode")
public final class SubscriberConverter {

  @Nonnull
  public static Subscriber fromCreateSubscriberArgs(@Nonnull CreateSubscriberArgs args) {
    Subscriber subscriber = new Subscriber();
//      subscriber.setSubscriberId();
    subscriber.setTitle(args.getTitle());
    subscriber.setDesc(args.getDesc());
    subscriber.setSubscriberType(args.getSubscriberType());
    subscriber.setAppName(args.getAppName());
    subscriber.setApplication(args.getApplication());
    subscriber.setReceiveUrl(args.getReceiveUrl());
    return subscriber;
  }
}
