package com.zzsong.bus.base.mongo.repository;

import com.zzsong.bus.base.mongo.document.EventMongoDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface MongoEventRepository extends ReactiveMongoRepository<EventMongoDo, String> {

  @Nonnull
  Mono<EventMongoDo> findByTopic(@Nonnull String topic);

  @Nonnull
  Mono<Long> deleteByTopic(@Nonnull String topic);

}
