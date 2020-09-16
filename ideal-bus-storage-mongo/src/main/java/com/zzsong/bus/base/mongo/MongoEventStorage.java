package com.zzsong.bus.base.mongo;

import com.google.common.collect.ImmutableList;
import com.zzsong.bus.base.domain.Event;
import com.zzsong.bus.base.mongo.converter.EventMongoDoConverter;
import com.zzsong.bus.base.mongo.document.EventMongoDo;
import com.zzsong.bus.base.mongo.repository.MongoEventRepository;
import com.zzsong.bus.base.storage.EventStorage;
import com.zzsong.bus.base.transfer.QueryEventArgs;
import com.zzsong.bus.common.constant.EventTypeEnum;
import com.zzsong.bus.common.generator.IDGenerator;
import com.zzsong.bus.common.generator.IDGeneratorFactory;
import com.zzsong.bus.common.transfer.Paging;
import com.zzsong.bus.common.transfer.Res;
import com.zzsong.bus.common.transfer.SpringPages;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Component
public class MongoEventStorage implements EventStorage {
  @Nonnull
  private final IDGenerator idGenerator;
  @Nonnull
  private final ReactiveMongoTemplate template;
  @Nonnull
  private final MongoEventRepository repository;

  public MongoEventStorage(@Nonnull ReactiveMongoTemplate template,
                           @Nonnull MongoEventRepository repository,
                           @Nonnull IDGeneratorFactory idGeneratorFactory) {
    this.template = template;
    this.repository = repository;
    this.idGenerator = idGeneratorFactory.getGenerator("event");
  }

  @Nonnull
  @Override
  public Mono<Event> save(@Nonnull Event event) {
    EventMongoDo eventMongoDo = EventMongoDoConverter.fromEvent(event);
    return repository.findByTopic(event.getTopic())
        .map(EventMongoDo::getId).defaultIfEmpty(-1L)
        .flatMap(id -> {
          if (id > 0) {
            eventMongoDo.setId(id);
          } else {
            eventMongoDo.setId(idGenerator.generate());
          }
          return repository.save(eventMongoDo).map(EventMongoDoConverter::toEvent);
        });
  }

  @Nonnull
  @Override
  public Mono<Long> delete(@Nonnull String topic) {
    return repository.deleteByTopic(topic);
  }

  @Nonnull
  @Override
  public Mono<Optional<Event>> findByTopic(@Nonnull String topic) {
    return repository.findByTopic(topic)
        .map(EventMongoDoConverter::toEvent)
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty());
  }

  @Nonnull
  @Override
  public Mono<List<Event>> findAll() {
    return repository.findAll()
        .map(EventMongoDoConverter::toEvent)
        .collectList()
        .defaultIfEmpty(ImmutableList.of());
  }

  @SuppressWarnings("DuplicatedCode")
  @Nonnull
  @Override
  public Mono<Res<List<Event>>> query(@Nullable QueryEventArgs args,
                                      @Nonnull Paging paging) {
    Criteria criteria = new Criteria();
    if (args != null) {
      String topic = args.getTopic();
      Long moduleId = args.getModuleId();
      String eventName = args.getEventName();
      EventTypeEnum eventType = args.getEventType();
      if (StringUtils.isNotBlank(topic)) {
        criteria.and("topic").is(topic);
      }
      if (moduleId != null) {
        criteria.and("moduleId").is(moduleId);
      }
      if (StringUtils.isNotBlank(eventName)) {
        criteria.and("eventName").regex("^" + eventName);
      }
      if (eventType != null) {
        criteria.and("eventType").is(eventType);
      }
    }
    Query query = Query.query(criteria);
    return template.count(query, EventMongoDo.class)
        .flatMap(count -> {
          if (count == 0) {
            return Mono.just(Res.ofPaging(paging, 0, ImmutableList.of()));
          }
          int offset = paging.getOffset();
          int size = paging.getSize();
          query.skip(offset).limit(size);
          Sort sort = SpringPages.getSort(paging);
          if (sort != null) {
            query.with(sort);
          }
          return template.find(query, EventMongoDo.class)
              .map(EventMongoDoConverter::toEvent)
              .collectList()
              .map(list -> Res.ofPaging(paging, Math.toIntExact(count), list));
        });
  }
}
