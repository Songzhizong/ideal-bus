package com.zzsong.bus.storage.mongo;

import com.mongodb.client.result.UpdateResult;
import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.generator.IDGenerator;
import com.zzsong.bus.abs.generator.IDGeneratorFactory;
import com.zzsong.bus.abs.generator.SnowFlake;
import com.zzsong.bus.abs.storage.RouteInstanceStorage;
import com.zzsong.bus.storage.mongo.converter.RouteInstanceDoConverter;
import com.zzsong.bus.storage.mongo.document.RouteInstanceDo;
import com.zzsong.bus.storage.mongo.repository.MongoRouteInstanceRepository;
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
public class MongoRouteInstanceStorage implements RouteInstanceStorage {
  @Nonnull
  private final IDGenerator idGenerator;
  @Nonnull
  private final ReactiveMongoTemplate template;
  @Nonnull
  private final MongoRouteInstanceRepository repository;

  public MongoRouteInstanceStorage(@Nonnull IDGeneratorFactory idGeneratorFactory,
                                   @Nonnull ReactiveMongoTemplate template,
                                   @Nonnull MongoRouteInstanceRepository repository) {
    this.idGenerator = idGeneratorFactory.getGenerator("routeInstance");
    this.template = template;
    this.repository = repository;
  }

  @Nonnull
  @Override
  public Mono<RouteInstance> save(@Nonnull RouteInstance routeInstance) {
    //noinspection ConstantConditions
    if (routeInstance.getInstanceId() == null) {
      routeInstance.setInstanceId(idGenerator.generate());
    }
    RouteInstanceDo routeInstanceDo = RouteInstanceDoConverter.fromRouteInstance(routeInstance);
    return repository.save(routeInstanceDo)
        .map(RouteInstanceDoConverter::toRouteInstance);
  }

  @Nonnull
  @Override
  public Mono<List<RouteInstance>> saveAll(@Nonnull Collection<RouteInstance> routeInstances) {
    if (routeInstances.isEmpty()) {
      return Mono.just(Collections.emptyList());
    }
    List<RouteInstanceDo> collect = routeInstances.stream().map(instance -> {
      //noinspection ConstantConditions
      if (instance.getInstanceId() == null) {
        instance.setInstanceId(idGenerator.generate());
      }
      return RouteInstanceDoConverter.fromRouteInstance(instance);
    }).collect(Collectors.toList());
    return repository.saveAll(collect)
        .map(RouteInstanceDoConverter::toRouteInstance)
        .collectList()
        .defaultIfEmpty(Collections.emptyList());
  }

  @Nonnull
  @Override
  public Mono<List<RouteInstance>> loadDelayed(long maxNextTime, int count, int nodeId) {
    String nextPushTime = "nextPushTime";
    String instanceId = "instanceId";
    Criteria criteria = Criteria
        .where("nodeId").is(nodeId)
        .andOperator(
            Criteria.where(nextPushTime).gt(SnowFlake.START_TIMESTAMP),
            Criteria.where(nextPushTime).lte(maxNextTime)
        );
    Query query = Query.query(criteria).limit(count)
        .with(Sort.by(Sort.Direction.ASC, instanceId));
    return template.find(query, RouteInstanceDo.class)
        .map(RouteInstanceDoConverter::toRouteInstance)
        .collectList()
        .flatMap(instanceList -> {
          if (instanceList.isEmpty()) {
            return Mono.just(instanceList);
          }
          List<Long> instanceIds = instanceList.stream()
              .map(RouteInstance::getInstanceId)
              .collect(Collectors.toList());
          Query updateQuery = Query.query(Criteria.where(instanceId).in(instanceIds));
          Update update = new Update();
          update.set(nextPushTime, -1);
          return template.updateMulti(updateQuery, update, RouteInstanceDo.class)
              .map(r -> instanceList);
        });
  }

  @Nonnull
  @Override
  public Mono<List<RouteInstance>> loadWaiting(int count, int nodeId, long subscriptionId) {
    Criteria criteria = Criteria
        .where("nodeId").is(nodeId)
        .and("subscriptionId").is(subscriptionId)
        .and("nextPushTime").lte(SnowFlake.START_TIMESTAMP)
        .and("status").is(RouteInstance.STATUS_WAITING);
    Query query = Query.query(criteria).limit(count)
        .with(Sort.by(Sort.Direction.ASC, "instanceId"));
    return template.find(query, RouteInstanceDo.class)
        .map(RouteInstanceDoConverter::toRouteInstance)
        .collectList()
        .defaultIfEmpty(Collections.emptyList());
  }

  @Nonnull
  @Override
  public Mono<Long> updateStatus(long instanceId, int status, @Nonnull String message) {
    Query updateQuery = Query.query(Criteria.where("instanceId").is(instanceId));
    Update update = new Update();
    update.set("status", status);
    update.set("message", message);
    return template.updateFirst(updateQuery, update, RouteInstanceDo.class)
        .map(UpdateResult::getModifiedCount);
  }

  /**
   * 删除创建时间小于或等于指定时间戳的成功推送数据
   * <pre>
   *   因为主键是通过SnowFlake生成的, 因此可用通过时间戳来计算出对应的最小id, 所以这里通过主键来删除过期的数据.
   *   如果未来主键的生成策略发生了变更, 那这里应该重写.
   * </pre>
   *
   * @param time 最小时间
   * @return 删除数量
   * @author 宋志宗 on 2020/11/23
   */
  @Nonnull
  @Override
  public Mono<Long> deleteAllSucceedByCreateTimeLessThan(long time) {
    int status = RouteInstance.STATUS_SUCCESS;
    long minId = SnowFlake.calculateMinId(time);
    return repository.deleteAllByStatusAndInstanceIdLessThan(status, minId);
  }
}
