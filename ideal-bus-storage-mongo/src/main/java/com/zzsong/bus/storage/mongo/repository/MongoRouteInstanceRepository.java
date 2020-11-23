package com.zzsong.bus.storage.mongo.repository;

import com.zzsong.bus.storage.mongo.document.RouteInstanceDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * @author 宋志宗 on 2020/9/17
 */
public interface MongoRouteInstanceRepository
    extends ReactiveMongoRepository<RouteInstanceDo, Long> {

  Mono<Long> deleteAllByStatusAndInstanceIdLessThan(int status, long instanceId);
}
