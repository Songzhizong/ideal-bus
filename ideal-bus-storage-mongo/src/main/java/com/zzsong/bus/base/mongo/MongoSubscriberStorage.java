package com.zzsong.bus.base.mongo;

import com.google.common.collect.ImmutableList;
import com.zzsong.bus.base.domain.Subscriber;
import com.zzsong.bus.base.mongo.converter.SubscriberMongoDoConverter;
import com.zzsong.bus.base.mongo.document.SubscriberMongoDo;
import com.zzsong.bus.base.mongo.repository.MongoSubscriberRepository;
import com.zzsong.bus.base.storage.SubscriberStorage;
import com.zzsong.bus.base.transfer.QuerySubscriberArgs;
import com.zzsong.bus.common.constant.SubscriberTypeEnum;
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
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Component
public class MongoSubscriberStorage implements SubscriberStorage {
  @Nonnull
  private final IDGenerator idGenerator;
  @Nonnull
  private final ReactiveMongoTemplate template;
  @Nonnull
  private final MongoSubscriberRepository repository;

  public MongoSubscriberStorage(@Nonnull IDGeneratorFactory idGeneratorFactory,
                                @Nonnull ReactiveMongoTemplate template,
                                @Nonnull MongoSubscriberRepository repository) {
    this.template = template;
    this.repository = repository;
    this.idGenerator = idGeneratorFactory.getGenerator("subscriber");
  }

  @Nonnull
  @Override
  public Mono<Subscriber> save(@Nonnull Subscriber subscriber) {
    //noinspection ConstantConditions
    if (subscriber.getSubscriberId() == null) {
      subscriber.setSubscriberId(idGenerator.generate());
    }
    SubscriberMongoDo mongoDo = SubscriberMongoDoConverter.fromSubscriber(subscriber);
    return repository.save(mongoDo).map(SubscriberMongoDoConverter::toSubscriber);
  }

  @Nonnull
  @Override
  public Mono<Long> delete(long subscriberId) {
    return repository.deleteBySubscriberId(subscriberId);
  }

  @Nonnull
  @Override
  public Mono<Optional<Subscriber>> findById(long subscriberId) {
    return repository.findById(subscriberId)
        .map(SubscriberMongoDoConverter::toSubscriber)
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty());
  }

  @Nonnull
  @Override
  public Mono<List<Subscriber>> findAll() {
    return repository.findAll()
        .map(SubscriberMongoDoConverter::toSubscriber)
        .collectList();
  }

  @Override
  public Mono<Res<List<Subscriber>>> query(@Nullable QuerySubscriberArgs args,
                                           @Nonnull Paging paging) {
    Criteria criteria = new Criteria();
    if (args != null) {
      String title = args.getTitle();
      String application = args.getApplication();
      SubscriberTypeEnum subscriberType = args.getSubscriberType();
      if (StringUtils.isNotBlank(title)) {
        criteria.and("title").regex("^" + title);
      }
      if (StringUtils.isNotBlank(application)) {
        criteria.and("application").is(application);
      }
      if (subscriberType != null) {
        criteria.and("subscriberType").is(subscriberType);
      }
    }
    Query query = Query.query(criteria);
    return template.count(query, SubscriberMongoDo.class)
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
          return template.find(query, SubscriberMongoDo.class)
              .collectList()
              .map(list -> {
                List<Subscriber> collect = list.stream()
                    .map(SubscriberMongoDoConverter::toSubscriber)
                    .collect(Collectors.toList());
                return Res.ofPaging(paging, Math.toIntExact(count), collect);
              });
        });
  }
}
