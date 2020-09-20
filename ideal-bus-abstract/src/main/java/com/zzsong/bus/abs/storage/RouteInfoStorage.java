package com.zzsong.bus.abs.storage;

import com.zzsong.bus.abs.domain.RouteInfo;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/17
 */
public interface RouteInfoStorage {

  @Nonnull
  Mono<RouteInfo> save(@Nonnull RouteInfo routeInfo);

  @Nonnull
  Mono<List<RouteInfo>> saveAll(@Nonnull Collection<RouteInfo> routeInfos);
}
