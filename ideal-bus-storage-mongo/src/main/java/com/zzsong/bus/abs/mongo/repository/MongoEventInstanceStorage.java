package com.zzsong.bus.abs.mongo.repository;

import com.zzsong.bus.abs.mongo.document.EventInstanceDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author 宋志宗 on 2020/9/17
 */
public interface MongoEventInstanceStorage extends ReactiveMongoRepository<EventInstanceDo, String> {
  
}
