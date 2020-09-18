package com.zzsong.bus.storage.mongo.repository;

import com.zzsong.bus.storage.mongo.document.EventDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author 宋志宗 on 2020/9/16
 */
public interface MongoEventRepository extends ReactiveMongoRepository<EventDo, String> {

  @Nonnull
  Flux<EventDo> findAllByTopicIn(@Nonnull Collection<String> topicList);

  @Nonnull
  Mono<EventDo> findByTopic(@Nonnull String topic);

  @Nonnull
  Mono<Long> deleteByTopic(@Nonnull String topic);

}
