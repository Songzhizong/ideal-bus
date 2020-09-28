package com.zzsong.bus.core.admin.service;

import com.zzsong.bus.abs.converter.ApplicationConverter;
import com.zzsong.bus.abs.domain.Application;
import com.zzsong.bus.abs.storage.ApplicationStorage;
import com.zzsong.bus.abs.transfer.CreateApplicationArgs;
import com.zzsong.bus.abs.transfer.QueryApplicationArgs;
import com.zzsong.bus.abs.transfer.UpdateApplicationArgs;
import com.zzsong.bus.abs.share.VisibleException;
import com.zzsong.bus.abs.share.CommonResMsg;
import com.zzsong.bus.abs.share.Paging;
import com.zzsong.bus.abs.share.Res;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * @author 宋志宗 on 2020/9/16
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
public class ApplicationService {
  @Autowired
  private SubscriptionService subscriptionService;
  @Nonnull
  private final ApplicationStorage applicationStorage;

  public ApplicationService(@Nonnull ApplicationStorage applicationStorage) {
    this.applicationStorage = applicationStorage;
  }

  @Nonnull
  public Mono<Application> create(@Nonnull CreateApplicationArgs args) {
    Application application = ApplicationConverter.fromCreateArgs(args);
    return applicationStorage.save(application);
  }

  @SuppressWarnings("DuplicatedCode")
  @Nonnull
  public Mono<Application> update(@Nonnull UpdateApplicationArgs args) {
    long applicationId = args.getApplicationId();
    return applicationStorage.findById(applicationId)
        .flatMap(opt -> {
          if (!opt.isPresent()) {
            return Mono.error(new VisibleException(CommonResMsg.NOT_FOUND));
          }
          Application application = opt.get();
          application.setTitle(args.getTitle());
          application.setDesc(args.getDesc());
          application.setAccessToken(args.getAccessToken());
          application.setApplicationType(args.getApplicationType());
          application.setAppName(args.getAppName());
          application.setExternalApp(args.getExternalApp());
          application.setReceiveUrl(args.getReceiveUrl());
          return applicationStorage.save(application);
        });
  }

  @Nonnull
  public Mono<Long> delete(long applicationId) {
    return subscriptionService.existByApplication(applicationId)
        .flatMap(exists -> {
          if (exists) {
            return Mono.error(new VisibleException("该订阅者存在订阅关系"));
          } else {
            return applicationStorage.delete(applicationId);
          }
        });
  }

  public Mono<Res<List<Application>>> query(@Nullable QueryApplicationArgs args,
                                            @Nonnull Paging paging) {
    return applicationStorage.query(args, paging);
  }

  @Nonnull
  public Mono<Optional<Application>> loadById(long applicationId) {
    return applicationStorage.findById(applicationId);
  }

  @Nonnull
  public Mono<List<Application>> findAll() {
    return applicationStorage.findAll();
  }
}
