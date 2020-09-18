package com.zzsong.bus.abs.storage;

import com.zzsong.bus.abs.domain.Subscriber;
import com.zzsong.bus.abs.transfer.QuerySubscriberArgs;
import com.zzsong.bus.abs.share.Paging;
import com.zzsong.bus.abs.share.Res;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface SubscriberStorage {
  @Nonnull
  Mono<Subscriber> save(@Nonnull Subscriber subscriber);

  @Nonnull
  Mono<Long> delete(long subscriberId);

  @Nonnull
  Mono<Optional<Subscriber>> findById(long subscriberId);

  @Nonnull
  Mono<List<Subscriber>> findAll();

  @Nonnull
  Mono<List<Subscriber>> findAll(@Nonnull Collection<Long> subscriberIdList);


  Mono<Res<List<Subscriber>>> query(@Nullable QuerySubscriberArgs args,
                                    @Nonnull Paging paging);
}
