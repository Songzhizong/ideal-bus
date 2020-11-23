package com.zzsong.bus.storage.mongo;

import com.zzsong.bus.abs.domain.Application;
import com.zzsong.bus.storage.mongo.converter.ApplicationDoConverter;
import com.zzsong.bus.storage.mongo.document.ApplicationDo;
import com.zzsong.bus.storage.mongo.repository.MongoApplicationRepository;
import com.zzsong.bus.abs.storage.ApplicationStorage;
import com.zzsong.bus.abs.transfer.QueryApplicationArgs;
import com.zzsong.bus.abs.constants.ApplicationTypeEnum;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Component
public class MongoApplicationStorage implements ApplicationStorage {
  @Nonnull
  private final IDGenerator idGenerator;
  @Nonnull
  private final ReactiveMongoTemplate template;
  @Nonnull
  private final MongoApplicationRepository repository;

  public MongoApplicationStorage(@Nonnull IDGeneratorFactory idGeneratorFactory,
                                 @Nonnull ReactiveMongoTemplate template,
                                 @Nonnull MongoApplicationRepository repository) {
    this.template = template;
    this.repository = repository;
    this.idGenerator = idGeneratorFactory.getGenerator("application");
  }

  @Nonnull
  @Override
  public Mono<Application> save(@Nonnull Application application) {
    //noinspection ConstantConditions
    if (application.getApplicationId() == null) {
      application.setApplicationId(idGenerator.generate());
    }
    ApplicationDo mongoDo = ApplicationDoConverter.fromApp(application);
    return repository.save(mongoDo).map(ApplicationDoConverter::toApp);
  }

  @Nonnull
  @Override
  public Mono<Long> delete(long applicationId) {
    return repository.deleteByApplicationId(applicationId);
  }

  @Nonnull
  @Override
  public Mono<Optional<Application>> findById(long applicationId) {
    return repository.findById(applicationId)
        .map(ApplicationDoConverter::toApp)
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty());
  }

  @Nonnull
  @Override
  public Mono<List<Application>> findAll() {
    return repository.findAll()
        .map(ApplicationDoConverter::toApp)
        .collectList()
        .defaultIfEmpty(Collections.emptyList());
  }

  @Nonnull
  @Override
  public Mono<List<Application>> findAll(@Nonnull Collection<Long> applicationIdList) {
    return repository.findAllById(applicationIdList)
        .map(ApplicationDoConverter::toApp)
        .collectList()
        .defaultIfEmpty(Collections.emptyList());
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public Mono<Res<List<Application>>> query(@Nullable QueryApplicationArgs args,
                                            @Nonnull Paging paging) {
    Criteria criteria = new Criteria();
    if (args != null) {
      String title = args.getTitle();
      String externalApplication = args.getExternalApplication();
      ApplicationTypeEnum applicationType = args.getApplicationType();
      if (StringUtils.isNotBlank(title)) {
        criteria.and("title").regex("^" + title);
      }
      if (StringUtils.isNotBlank(externalApplication)) {
        criteria.and("externalApplication").is(externalApplication);
      }
      if (applicationType != null) {
        criteria.and("applicationType").is(applicationType);
      }
    }
    Query query = Query.query(criteria);
    return template.count(query, ApplicationDo.class)
        .flatMap(count -> {
          if (count == 0) {
            return Mono.just(Res.ofPaging(paging, 0, Collections.emptyList()));
          }
          int offset = paging.getOffset();
          int size = paging.getSize();
          query.skip(offset).limit(size);
          Sort sort = SpringPages.getSort(paging);
          if (sort != null) {
            query.with(sort);
          }
          return template.find(query, ApplicationDo.class)
              .map(ApplicationDoConverter::toApp)
              .collectList()
              .map(list -> Res.ofPaging(paging, Math.toIntExact(count), list));
        });
  }
}
