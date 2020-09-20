package com.zzsong.bus.storage.mongo.repository;

import com.zzsong.bus.storage.mongo.document.EventInstanceDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author 宋志宗 on 2020/9/17
 */
public interface MongoEventInstanceRepository
    extends ReactiveMongoRepository<EventInstanceDo, String> {

}
