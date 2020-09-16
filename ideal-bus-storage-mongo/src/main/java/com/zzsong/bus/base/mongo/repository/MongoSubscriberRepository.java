package com.zzsong.bus.base.mongo.repository;

import com.zzsong.bus.base.mongo.document.SubscriberMongoDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface MongoSubscriberRepository
    extends ReactiveMongoRepository<SubscriberMongoDo, Long> {

  Mono<Long> deleteBySubscriberId(long subscriberId);
}
