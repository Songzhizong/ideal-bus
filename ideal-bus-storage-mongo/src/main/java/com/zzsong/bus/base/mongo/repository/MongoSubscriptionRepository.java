package com.zzsong.bus.base.mongo.repository;

import com.zzsong.bus.base.mongo.document.SubscriptionMongoDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface MongoSubscriptionRepository extends ReactiveMongoRepository<SubscriptionMongoDo, Long> {
}
