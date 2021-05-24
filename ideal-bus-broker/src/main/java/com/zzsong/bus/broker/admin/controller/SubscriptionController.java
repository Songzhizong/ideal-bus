package com.zzsong.bus.broker.admin.controller;

import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.abs.transfer.SubscribeArgs;
import com.zzsong.bus.abs.share.Res;
import com.zzsong.bus.common.transfer.ResubscribeArgs;
import com.zzsong.bus.broker.admin.service.SubscriptionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 订阅关系管理
 *
 * @author 宋志宗 on 2020/9/16
 */
@RestController
@RequestMapping("/subscription")
public class SubscriptionController {
  @Nonnull
  private final SubscriptionService subscriptionService;

  public SubscriptionController(@Nonnull SubscriptionService subscriptionService) {
    this.subscriptionService = subscriptionService;
  }

  /**
   * 新增订阅
   */
  @Nonnull
  @PostMapping("/subscribe")
  public Mono<Res<Subscription>> subscribe(@RequestBody @Nonnull SubscribeArgs args) {
    return Mono.just(args.checkAndGet()).flatMap(subscriptionService::subscribe).map(Res::data);
  }

  /**
   * 修改订阅信息
   */
  @Nonnull
  @PostMapping("/update")
  public Mono<Res<Subscription>> update(@RequestBody @Nonnull SubscribeArgs args,
                                        @Nonnull Long subscriptionId) {
    return Mono.just(args.checkAndGet())
        .doOnNext(a -> {
          //noinspection ConstantConditions
          if (subscriptionId == null) {
            throw new IllegalArgumentException("subscriptionId不能为空");
          }
        })
        .flatMap(checkedArgs ->
            subscriptionService.update(args, subscriptionId).map(Res::data)
        );
  }

  /**
   * 反转订阅状态
   */
  @Nonnull
  @PostMapping("/status/reversal")
  public Mono<Res<Integer>> reversalStatus(@Nonnull Long subscriptionId) {
    return Mono.just(1)
        .map(t -> {
          //noinspection ConstantConditions
          if (subscriptionId == null) {
            throw new IllegalArgumentException("subscriptionId不能为空");
          }
          return subscriptionId;
        })
        .flatMap(subscriptionService::reversalStatus)
        .map(Res::data);
  }

  /**
   * 自动订阅
   *
   * @param args 订阅参数
   * @return 订阅结果
   */
  @Nonnull
  @PostMapping("/resubscribe")
  public Mono<Res<List<Subscription>>> resubscribe(@RequestBody @Nonnull ResubscribeArgs args) {
    return Mono.just(args.checkAndGet())
        .flatMap(subscriptionService::resubscribe)
        .map(Res::data);
  }

  /**
   * 解除指定的订阅关系
   *
   * @param applicationId 订阅者id
   * @param topic         主题
   * @return 删除条数
   */
  @Nonnull
  @PostMapping("/unsubscribe")
  public Mono<Res<Long>> unsubscribe(@Nonnull Long applicationId,
                                     @Nonnull String topic) {
    return Mono.just(1)
        .doOnNext(t -> {
          //noinspection ConstantConditions
          if (applicationId == null) {
            throw new IllegalArgumentException("订阅者不能为空");
          }
          if (StringUtils.isBlank(topic)) {
            throw new IllegalArgumentException("主题不能为空");
          }
        })
        .flatMap(i -> subscriptionService.unsubscribe(applicationId, topic)).map(Res::data);
  }

  /**
   * 解除指定主题的所有订阅关系
   *
   * @param topic 主题
   * @return 删除的条数
   */
  @Nonnull
  @PostMapping("/topic/unsubscribe")
  public Mono<Res<Long>> unsubscribe(@Nonnull String topic) {
    return Mono.just(1)
        .map(t -> {
          if (StringUtils.isBlank(topic)) {
            throw new IllegalArgumentException("主题不能为空");
          }
          return topic;
        })
        .flatMap(subscriptionService::unsubscribe)
        .map(Res::data);
  }

  /**
   * 解除指定订阅者所有的订阅关系
   *
   * @param applicationId 订阅者id
   * @return 删除条数
   */
  @Nonnull
  @PostMapping("/application/unsubscribe")
  public Mono<Res<Long>> unsubscribe(@Nonnull Long applicationId) {
    return Mono.just(1)
        .map(t -> {
          //noinspection ConstantConditions
          if (applicationId == null) {
            throw new IllegalArgumentException("订阅者不能为空");
          }
          return applicationId;
        })
        .flatMap(subscriptionService::unsubscribe)
        .map(Res::data);
  }

  /**
   * 获取指定订阅者所有订阅关系
   *
   * @param applicationId 订阅者ID
   * @return 订阅关系列表
   */
  @Nonnull
  @GetMapping("/application")
  public Mono<Res<List<Subscription>>> getSubscription(@Nonnull Long applicationId) {
    return Mono.just(1)
        .map(t -> {
          //noinspection ConstantConditions
          if (applicationId == null) {
            throw new IllegalArgumentException("订阅者不能为空");
          }
          return applicationId;
        })
        .flatMap(subscriptionService::getSubscription)
        .map(Res::data);
  }

  /**
   * 获取指定主题的所有订阅关系
   *
   * @param topic 主题
   * @return 订阅关系列表
   */
  @Nonnull
  @GetMapping("/topic")
  public Mono<Res<List<Subscription>>> getSubscription(@Nonnull String topic) {
    return Mono.error(new UnsupportedOperationException("暂不支持"));
  }
}
