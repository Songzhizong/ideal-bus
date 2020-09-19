package com.zzsong.bus.abs.storage;

import com.zzsong.bus.abs.domain.Application;
import com.zzsong.bus.abs.transfer.QueryApplicationArgs;
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
public interface ApplicationStorage {
  @Nonnull
  Mono<Application> save(@Nonnull Application application);

  @Nonnull
  Mono<Long> delete(long applicationId);

  @Nonnull
  Mono<Optional<Application>> findById(long applicationId);

  @Nonnull
  Mono<List<Application>> findAll();

  @Nonnull
  Mono<List<Application>> findAll(@Nonnull Collection<Long> applicationIdList);


  Mono<Res<List<Application>>> query(@Nullable QueryApplicationArgs args,
                                     @Nonnull Paging paging);
}
