package com.zzsong.bus.abs.storage;

import com.zzsong.bus.abs.domain.RouteInstance;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/17
 */
public interface RouteInstanceStorage {

  @Nonnull
  Mono<RouteInstance> save(@Nonnull RouteInstance routeInstance);

  @Nonnull
  Mono<List<RouteInstance>> saveAll(@Nonnull Collection<RouteInstance> routeInstances);

  @Nonnull
  Mono<List<RouteInstance>> loadDelayed(long maxNextTime, int count, int nodeId);

  @Nonnull
  Mono<List<RouteInstance>> loadWaiting(int count, int nodeId);
}
