package com.zzsong.bus.core.admin.service;

import com.zzsong.bus.base.converter.SubscriberConverter;
import com.zzsong.bus.base.domain.Subscriber;
import com.zzsong.bus.base.storage.SubscriberStorage;
import com.zzsong.bus.base.transfer.CreateSubscriberArgs;
import com.zzsong.bus.base.transfer.QuerySubscriberArgs;
import com.zzsong.bus.base.transfer.UpdateSubscriberArgs;
import com.zzsong.bus.common.exception.VisibleException;
import com.zzsong.bus.common.transfer.CommonResMsg;
import com.zzsong.bus.common.transfer.Paging;
import com.zzsong.bus.common.transfer.Res;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Service
public class SubscriberService {
  @Nonnull
  private final SubscriberStorage subscriberStorage;

  public SubscriberService(@Nonnull SubscriberStorage subscriberStorage) {
    this.subscriberStorage = subscriberStorage;
  }

  @Nonnull
  public Mono<Subscriber> create(@Nonnull CreateSubscriberArgs args) {
    Subscriber subscriber = SubscriberConverter.fromCreateSubscriberArgs(args);
    return subscriberStorage.save(subscriber);
  }

  @Nonnull
  public Mono<Subscriber> update(@Nonnull UpdateSubscriberArgs args) {
    long subscriberId = args.getSubscriberId();
    return subscriberStorage.findById(subscriberId)
        .flatMap(opt -> {
          if (!opt.isPresent()) {
            return Mono.error(new VisibleException(CommonResMsg.NOT_FOUND));
          }
          Subscriber subscriber = opt.get();
          subscriber.setTitle(args.getTitle());
          subscriber.setDesc(args.getDesc());
          subscriber.setSubscriberType(args.getSubscriberType());
          subscriber.setAppName(args.getAppName());
          subscriber.setApplication(args.getApplication());
          subscriber.setReceiveUrl(args.getReceiveUrl());
          return subscriberStorage.save(subscriber);
        });
  }

  @Nonnull
  public Mono<Long> delete(long subscriberId) {
    return subscriberStorage.delete(subscriberId);
  }

  public Mono<Res<List<Subscriber>>> query(@Nullable QuerySubscriberArgs args,
                                           @Nonnull Paging paging) {
    return subscriberStorage.query(args, paging);
  }
}
