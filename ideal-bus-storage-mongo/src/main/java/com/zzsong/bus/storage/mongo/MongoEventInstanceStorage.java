package com.zzsong.bus.storage.mongo;

import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.storage.EventInstanceStorage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Component
public class MongoEventInstanceStorage implements EventInstanceStorage {

  @Nonnull
  @Override
  public Mono<EventInstance> save(@Nonnull EventInstance eventInstance) {
    return Mono.empty();
  }
}
