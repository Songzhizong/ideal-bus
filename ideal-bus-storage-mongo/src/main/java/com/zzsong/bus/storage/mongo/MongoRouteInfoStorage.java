package com.zzsong.bus.storage.mongo;

import com.zzsong.bus.abs.domain.RouteInfo;
import com.zzsong.bus.abs.generator.IDGenerator;
import com.zzsong.bus.abs.generator.IDGeneratorFactory;
import com.zzsong.bus.abs.storage.RouteInfoStorage;
import com.zzsong.bus.storage.mongo.converter.RouteInfoDoConverter;
import com.zzsong.bus.storage.mongo.document.RouteInfoDo;
import com.zzsong.bus.storage.mongo.repository.MongoRouteInfoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Component
public class MongoRouteInfoStorage implements RouteInfoStorage {
  @Nonnull
  private final IDGenerator idGenerator;
  @Nonnull
  private final MongoRouteInfoRepository routeInfoRepository;

  public MongoRouteInfoStorage(@Nonnull IDGeneratorFactory idGeneratorFactory,
                               @Nonnull MongoRouteInfoRepository routeInfoRepository) {
    this.idGenerator = idGeneratorFactory.getGenerator("routeInfo");
    this.routeInfoRepository = routeInfoRepository;
  }

  @Nonnull
  @Override
  public Mono<List<RouteInfo>> saveAll(@Nonnull Collection<RouteInfo> routeInfos) {
    if (routeInfos.isEmpty()) {
      return Mono.just(Collections.emptyList());
    }
    List<RouteInfoDo> collect = routeInfos.stream().map(routeInfo -> {
      //noinspection ConstantConditions
      if (routeInfo.getInstanceId() == null) {
        routeInfo.setInstanceId(idGenerator.generate());
      }
      return RouteInfoDoConverter.fromRouteInfo(routeInfo);
    }).collect(Collectors.toList());
    return routeInfoRepository.saveAll(collect)
        .map(RouteInfoDoConverter::toRouteInfo)
        .collectList()
        .defaultIfEmpty(Collections.emptyList());
  }
}
