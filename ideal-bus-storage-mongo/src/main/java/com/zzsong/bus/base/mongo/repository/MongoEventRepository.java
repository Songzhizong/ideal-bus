package com.zzsong.bus.base.mongo.repository;

import com.zzsong.bus.base.mongo.document.EventMongoDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface MongoEventRepository extends ReactiveMongoRepository<EventMongoDo, String> {
}
