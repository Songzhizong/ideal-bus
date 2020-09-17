package com.zzsong.bus.core.admin.controller;

import com.zzsong.bus.abs.domain.Subscriber;
import com.zzsong.bus.abs.transfer.CreateSubscriberArgs;
import com.zzsong.bus.abs.transfer.QuerySubscriberArgs;
import com.zzsong.bus.abs.transfer.UpdateSubscriberArgs;
import com.zzsong.bus.common.transfer.Paging;
import com.zzsong.bus.common.transfer.Res;
import com.zzsong.bus.core.admin.service.SubscriberService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 订阅者管理
 *
 * @author 宋志宗 on 2020/9/16
 */
@RestController
@RequestMapping("/subscriber")
public class SubscriberController {

  @Nonnull
  private final SubscriberService subscriberService;

  public SubscriberController(@Nonnull SubscriberService subscriberService) {
    this.subscriberService = subscriberService;
  }

  @Nonnull
  @PostMapping("/create")
  public Mono<Res<Subscriber>> create(@Validated @RequestBody
                                      @Nonnull CreateSubscriberArgs args) {
    return subscriberService.create(args).map(Res::data);
  }

  @Nonnull
  @PostMapping("/update")
  public Mono<Res<Subscriber>> update(@Validated @RequestBody
                                      @Nonnull UpdateSubscriberArgs args) {
    return subscriberService.update(args).map(Res::data);
  }

  @Nonnull
  @DeleteMapping("/delete/{subscriberId}")
  public Mono<Res<Long>> delete(@PathVariable("subscriberId") long subscriberId) {
    return subscriberService.delete(subscriberId).map(Res::data);
  }

  @Nonnull
  @GetMapping("/query")
  public Mono<Res<List<Subscriber>>> query(@Nullable QuerySubscriberArgs args,
                                           @Nullable Paging paging) {
    if (paging == null) {
      paging = Paging.of(1, 10);
    }
    paging.descBy("subscriberId");
    return subscriberService.query(args, paging);
  }
}
