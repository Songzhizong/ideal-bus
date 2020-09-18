package com.zzsong.bus.storage.mongo;

import com.google.common.collect.ImmutableList;
import com.zzsong.bus.abs.domain.Subscriber;
import com.zzsong.bus.storage.mongo.converter.SubscriberDoConverter;
import com.zzsong.bus.storage.mongo.document.SubscriberDo;
import com.zzsong.bus.storage.mongo.repository.MongoSubscriberRepository;
import com.zzsong.bus.abs.storage.SubscriberStorage;
import com.zzsong.bus.abs.transfer.QuerySubscriberArgs;
import com.zzsong.bus.abs.constants.SubscriberTypeEnum;
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
    SubscriberDo mongoDo = SubscriberDoConverter.fromSubscriber(subscriber);
    return repository.save(mongoDo).map(SubscriberDoConverter::toSubscriber);
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
        .map(SubscriberDoConverter::toSubscriber)
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty());
  }

  @Nonnull
  @Override
  public Mono<List<Subscriber>> findAll() {
    return repository.findAll()
        .map(SubscriberDoConverter::toSubscriber)
        .collectList()
        .defaultIfEmpty(ImmutableList.of());
  }

  @Nonnull
  @Override
  public Mono<List<Subscriber>> findAll(@Nonnull Collection<Long> subscriberIdList) {
    return repository.findAllById(subscriberIdList)
        .map(SubscriberDoConverter::toSubscriber)
        .collectList()
        .defaultIfEmpty(ImmutableList.of());
  }

  @SuppressWarnings("DuplicatedCode")
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
    return template.count(query, SubscriberDo.class)
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
          return template.find(query, SubscriberDo.class)
              .map(SubscriberDoConverter::toSubscriber)
              .collectList()
              .map(list -> Res.ofPaging(paging, Math.toIntExact(count), list));
        });
  }
}
