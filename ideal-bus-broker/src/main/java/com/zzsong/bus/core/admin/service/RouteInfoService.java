package com.zzsong.bus.core.admin.service;

import com.zzsong.bus.abs.domain.RouteInfo;
import com.zzsong.bus.abs.storage.RouteInfoStorage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Service
public class RouteInfoService {
  @Nonnull
  private final RouteInfoStorage storage;

  public RouteInfoService(@Nonnull RouteInfoStorage storage) {
    this.storage = storage;
  }

  @Nonnull
  public Mono<RouteInfo> save(@Nonnull RouteInfo routeInfo) {
    return storage.save(routeInfo);
  }

  @Nonnull
  public Mono<List<RouteInfo>> saveAll(@Nonnull Collection<RouteInfo> routeInfos) {
    return storage.saveAll(routeInfos);
  }

  @Nonnull
  public Mono<List<RouteInfo>> loadDelayed(long maxNextTime, int count, int nodeId) {
    return storage.loadDelayed(maxNextTime, count, nodeId);
  }
}
