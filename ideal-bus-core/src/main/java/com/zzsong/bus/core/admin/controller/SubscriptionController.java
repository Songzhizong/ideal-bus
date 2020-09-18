package com.zzsong.bus.core.admin.controller;

import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.abs.transfer.SubscribeArgs;
import com.zzsong.bus.abs.share.Res;
import com.zzsong.bus.common.transfer.AutoSubscribArgs;
import com.zzsong.bus.core.admin.service.SubscriptionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 订阅关系管理
 *
 * @author 宋志宗 on 2020/9/16
 */
@Validated
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
  public Mono<Res<Subscription>> subscribe(@Validated @RequestBody
                                           @Nonnull SubscribeArgs args) {
    return subscriptionService.subscribe(args).map(Res::data);
  }

  /**
   * 自动订阅
   *
   * @param args 订阅参数
   * @return 订阅结果
   */
  @Nonnull
  @PostMapping("/subscribe/auto")
  public Mono<Res<List<Subscription>>> autoSubscription(@Validated @RequestBody
                                                        @Nonnull AutoSubscribArgs args) {
    return subscriptionService.autoSubscription(args).map(Res::data);
  }

  /**
   * 解除指定的订阅关系
   *
   * @param subscriberId 订阅者id
   * @param topic        主题
   * @return 删除条数
   */
  @Nonnull
  @PostMapping("/unsubscribe")
  public Mono<Res<Long>> unsubscribe(@NotNull(message = "订阅者不能为空")
                                     @Nonnull Long subscriberId,
                                     @NotBlank(message = "主题不能为空")
                                     @Nonnull String topic) {
    return subscriptionService.unsubscribe(subscriberId, topic).map(Res::data);
  }

  /**
   * 解除指定主题的所有订阅关系
   *
   * @param topic 主题
   * @return 删除的条数
   */
  @Nonnull
  @PostMapping("/topic/unsubscribe")
  public Mono<Res<Long>> unsubscribe(@NotBlank(message = "主题不能为空")
                                     @Nonnull String topic) {
    return subscriptionService.unsubscribe(topic).map(Res::data);
  }

  /**
   * 解除指定订阅者所有的订阅关系
   *
   * @param subscriberId 订阅者id
   * @return 删除条数
   */
  @Nonnull
  @PostMapping("/subscriber/unsubscribe")
  public Mono<Res<Long>> unsubscribe(@NotNull(message = "订阅者不能为空")
                                     @Nonnull Long subscriberId) {
    return subscriptionService.unsubscribe(subscriberId).map(Res::data);
  }

  /**
   * 获取指定订阅者所有订阅关系
   *
   * @param subscriberId 订阅者ID
   * @return 订阅关系列表
   */
  @Nonnull
  @GetMapping("/subscriber")
  public Mono<Res<List<Subscription>>> getSubscription(@NotNull(message = "订阅者不能为空")
                                                       @Nonnull Long subscriberId) {
    return subscriptionService.getSubscription(subscriberId).map(Res::data);
  }

  /**
   * 获取指定主题的所有订阅关系
   *
   * @param topic 主题
   * @return 订阅关系列表
   */
  @Nonnull
  @GetMapping("/topic")
  public Mono<Res<List<Subscription>>> getSubscription(@NotBlank(message = "主题不能为空")
                                                       @Nonnull String topic) {
    return null;
  }
}
