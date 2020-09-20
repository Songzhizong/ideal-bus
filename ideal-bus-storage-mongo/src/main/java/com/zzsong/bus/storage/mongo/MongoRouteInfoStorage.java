package com.zzsong.bus.storage.mongo;

import com.zzsong.bus.abs.domain.RouteInfo;
import com.zzsong.bus.abs.generator.IDGenerator;
import com.zzsong.bus.abs.generator.IDGeneratorFactory;
import com.zzsong.bus.abs.storage.RouteInfoStorage;
import com.zzsong.bus.storage.mongo.converter.RouteInfoDoConverter;
import com.zzsong.bus.storage.mongo.document.RouteInfoDo;
import com.zzsong.bus.storage.mongo.repository.MongoRouteInfoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
  private final ReactiveMongoTemplate template;
  @Nonnull
  private final MongoRouteInfoRepository routeInfoRepository;

  public MongoRouteInfoStorage(@Nonnull IDGeneratorFactory idGeneratorFactory,
                               @Nonnull ReactiveMongoTemplate template,
                               @Nonnull MongoRouteInfoRepository routeInfoRepository) {
    this.idGenerator = idGeneratorFactory.getGenerator("routeInfo");
    this.template = template;
    this.routeInfoRepository = routeInfoRepository;
  }

  @Nonnull
  @Override
  public Mono<RouteInfo> save(@Nonnull RouteInfo routeInfo) {
    RouteInfoDo routeInfoDo = RouteInfoDoConverter.fromRouteInfo(routeInfo);
    return routeInfoRepository.save(routeInfoDo)
        .map(RouteInfoDoConverter::toRouteInfo);
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

  @Nonnull
  @Override
  public Mono<List<RouteInfo>> loadDelayed(long maxNextTime, int count, int nodeId) {
    Criteria criteria = Criteria
        .where("wait").is(RouteInfo.WAITING)
        .and("nodeId").is(nodeId)
        .and("nextPushTime").lte(maxNextTime);
    Query query = Query.query(criteria).limit(count).with(Sort.by("instanceId"));
    return template.find(query, RouteInfoDo.class)
        .map(RouteInfoDoConverter::toRouteInfo)
        .collectList()
        .flatMap(routeInfoList -> {
          if (routeInfoList.isEmpty()) {
            return Mono.just(routeInfoList);
          }
          List<Long> instanceIds = routeInfoList.stream()
              .map(RouteInfo::getInstanceId)
              .collect(Collectors.toList());
          Query updateQuery = Query.query(Criteria.where("instanceId").in(instanceIds));
          Update update = new Update();
          update.set("wait", RouteInfo.UN_WAITING);
          update.set("nextPushTime", -1L);
          return template.updateMulti(updateQuery, update, RouteInfoDo.class)
              .map(r -> routeInfoList);
        });
  }
}
