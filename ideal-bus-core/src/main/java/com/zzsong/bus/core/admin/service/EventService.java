package com.zzsong.bus.core.admin.service;

import com.zzsong.bus.abs.converter.EventConverter;
import com.zzsong.bus.abs.domain.Event;
import com.zzsong.bus.abs.storage.EventStorage;
import com.zzsong.bus.abs.transfer.QueryEventArgs;
import com.zzsong.bus.abs.transfer.SaveEventArgs;
import com.zzsong.bus.common.exception.VisibleException;
import com.zzsong.bus.common.transfer.Paging;
import com.zzsong.bus.common.transfer.Res;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/16
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
public class EventService {
  private static final Logger log = LoggerFactory.getLogger(EventService.class);
  @Autowired
  private SubscriptionService subscriptionService;
  @Nonnull
  private final EventStorage eventStorage;

  public EventService(@Nonnull EventStorage eventStorage) {
    this.eventStorage = eventStorage;
  }

  @Nonnull
  public Mono<Event> create(@Nonnull SaveEventArgs args) {
    return eventStorage.findByTopic(args.getTopic())
        .flatMap(opt -> {
          if (opt.isPresent()) {
            return Mono.error(new VisibleException("topic已存在"));
          }
          Event event = EventConverter.fromCreateArgs(args);
          return eventStorage.save(event);
        });
  }

  @Nonnull
  public Mono<Event> update(@Nonnull SaveEventArgs args) {
    return eventStorage.findByTopic(args.getTopic())
        .flatMap(opt -> {
          if (opt.isPresent()) {
            Event event = EventConverter.fromCreateArgs(args);
            return eventStorage.save(event);
          }
          return Mono.error(new VisibleException("topic不存在"));
        });
  }

  @Nonnull
  public Mono<Long> delete(@Nonnull String topic) {
    return subscriptionService.existByTopic(topic)
        .flatMap(exists -> {
          if (exists) {
            return Mono.error(new VisibleException("该订主题存在订阅关系"));
          }
          return eventStorage.delete(topic);
        });
  }

  @Nonnull
  public Mono<Res<List<Event>>> query(@Nullable QueryEventArgs args,
                                      @Nonnull Paging paging) {
    return eventStorage.query(args, paging);
  }

  @Nonnull
  public Mono<List<Event>> findAll() {
    return eventStorage.findAll();
  }
}
