package com.zzsong.bus.abs.mongo.repository;

import com.zzsong.bus.abs.mongo.document.RouteInstanceDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author 宋志宗 on 2020/9/17
 */
public interface MongoRouteInstanceStorage extends ReactiveMongoRepository<RouteInstanceDo, Long> {

}
