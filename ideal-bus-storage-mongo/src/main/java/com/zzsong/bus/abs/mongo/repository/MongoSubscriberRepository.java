package com.zzsong.bus.abs.mongo.repository;

import com.zzsong.bus.abs.mongo.document.SubscriberDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface MongoSubscriberRepository
    extends ReactiveMongoRepository<SubscriberDo, Long> {

  Mono<Long> deleteBySubscriberId(long subscriberId);
}
