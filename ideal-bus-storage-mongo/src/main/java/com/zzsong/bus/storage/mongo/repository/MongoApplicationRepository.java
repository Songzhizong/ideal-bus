package com.zzsong.bus.storage.mongo.repository;

import com.zzsong.bus.storage.mongo.document.ApplicationDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface MongoApplicationRepository
    extends ReactiveMongoRepository<ApplicationDo, Long> {

  Mono<Long> deleteByApplicationId(long applicationId);
}
