package com.zzsong.bus.storage.mongo;

import com.google.common.collect.ImmutableList;
import com.zzsong.bus.abs.domain.Event;
import com.zzsong.bus.storage.mongo.converter.EventDoConverter;
import com.zzsong.bus.storage.mongo.document.EventDo;
import com.zzsong.bus.storage.mongo.repository.MongoEventRepository;
import com.zzsong.bus.abs.storage.EventStorage;
import com.zzsong.bus.abs.transfer.QueryEventArgs;
import com.zzsong.bus.abs.constants.EventTypeEnum;
import com.zzsong.bus.abs.generator.IDGenerator;
import com.zzsong.bus.abs.generator.IDGeneratorFactory;
import com.zzsong.bus.abs.share.Paging;
import com.zzsong.bus.abs.share.Res;
import com.zzsong.bus.abs.share.SpringPages;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
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
    EventDo eventDo = EventDoConverter.fromEvent(event);
    return repository.findByTopic(event.getTopic())
        .map(EventDo::getEventId).defaultIfEmpty(-1L)
        .flatMap(id -> {
          if (id > 0) {
            eventDo.setEventId(id);
          } else {
            eventDo.setEventId(idGenerator.generate());
          }
          return repository.save(eventDo).map(EventDoConverter::toEvent);
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
        .map(EventDoConverter::toEvent)
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty());
  }

  @Nonnull
  @Override
  public Mono<List<Event>> findAll() {
    return repository.findAll()
        .map(EventDoConverter::toEvent)
        .collectList()
        .defaultIfEmpty(ImmutableList.of());
  }

  @Nonnull
  @Override
  public Mono<List<Event>> findAll(@Nonnull Collection<String> topicList) {
    return repository.findAllByTopicIn(topicList)
        .map(EventDoConverter::toEvent)
        .collectList()
        .defaultIfEmpty(ImmutableList.of());
  }

  @SuppressWarnings("DuplicatedCode")
  @Nonnull
  @Override
  public Mono<Res<List<Event>>> query(@Nullable QueryEventArgs args,
                                      @Nonnull Paging paging) {
    paging.descBy("eventId");
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
    return template.count(query, EventDo.class)
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
          return template.find(query, EventDo.class)
              .map(EventDoConverter::toEvent)
              .collectList()
              .map(list -> Res.ofPaging(paging, Math.toIntExact(count), list));
        });
  }
}
