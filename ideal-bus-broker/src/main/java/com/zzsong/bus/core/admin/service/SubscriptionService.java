package com.zzsong.bus.core.admin.service;

import com.zzsong.bus.abs.converter.SubscriptionConverter;
import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.abs.share.VisibleException;
import com.zzsong.bus.abs.storage.SubscriptionStorage;
import com.zzsong.bus.abs.transfer.SubscribeArgs;
import com.zzsong.bus.common.transfer.AutoSubscribeArgs;
import com.zzsong.bus.common.transfer.SubscriptionArgs;
import com.zzsong.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订阅关系管理
 * <p>相同的主题和订阅者只能存在一个订阅关系</p>
 *
 * @author 宋志宗 on 2020/9/16
 */
@Slf4j
@Service
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class SubscriptionService {
  @Autowired
  private ApplicationService applicationService;
  @Nonnull
  private final CacheService cacheService;
  @Nonnull
  private final SubscriptionStorage storage;

  public SubscriptionService(@Nonnull CacheService cacheService,
                             @Nonnull SubscriptionStorage storage) {
    this.cacheService = cacheService;
    this.storage = storage;
  }


  @Nonnull
  Mono<Boolean> existByTopic(@Nonnull String topic) {
    return storage.existByTopic(topic);
  }

  @Nonnull
  Mono<Boolean> existByApplication(long applicationId) {
    return storage.existByApplication(applicationId);
  }

  @Nonnull
  public Mono<Subscription> subscribe(@Nonnull SubscribeArgs args) {
    long applicationId = args.getApplicationId();
    String topic = args.getTopic();
    return storage.existByApplicationAndTopic(applicationId, topic)
        .flatMap(b -> {
          if (b) {
            return Mono.error(new VisibleException("订阅关系已存在"));
          } else {
            Subscription subscription = SubscriptionConverter.fromSubscribeArgs(args);
            return storage.save(subscription);
          }
        });
  }

  @Nonnull
  public Mono<Subscription> update(@Nonnull SubscribeArgs args, long subscriptionId) {
    return storage.findById(subscriptionId)
        .flatMap(opt -> {
          if (!opt.isPresent()) {
            return Mono.error(new VisibleException("订阅关系不存在"));
          }
          Subscription subscription = opt.get();
          subscription.setCondition(args.getCondition());
          subscription.setBroadcast(args.isBroadcast());
          subscription.setRetryCount(args.getRetryCount());
          return storage.save(subscription);
        });
  }

  public Mono<Integer> reversalStatus(long subscriptionId) {
    return storage.findById(subscriptionId)
        .flatMap(opt -> {
          if (!opt.isPresent()) {
            return Mono.error(new VisibleException("订阅关系不存在"));
          }
          Subscription subscription = opt.get();
          int status = subscription.getStatus();
          subscription.setStatus(status ^ 1);
          return storage.save(subscription).map(Subscription::getStatus);
        });
  }

  @Nonnull
  public Mono<List<Subscription>> autoSubscribe(@Nonnull AutoSubscribeArgs autoSubscribeArgs) {
    long applicationId = autoSubscribeArgs.getApplicationId();
    return applicationService.loadById(applicationId)
        .flatMap(opt -> {
          if (!opt.isPresent()) {
            return Mono.error(new VisibleException("订阅者不存在"));
          }
          return getSubscription(applicationId).flatMap(subscriptions -> {
            // 存储新的订阅, topic -> SubscriptionArgs
            Set<String> newTopics = new HashSet<>();
            // 存储现有的订阅关系, topic -> Subscription
            Map<String, Subscription> currentSubMap = subscriptions.stream()
                .collect(Collectors.toMap(Subscription::getTopic, s -> s));
            List<SubscriptionArgs> argsList = autoSubscribeArgs.getSubscriptionArgsList();
            // 计算出变更列表
            List<Subscription> changeList = new ArrayList<>();
            for (SubscriptionArgs args : argsList) {
              String topic = args.getTopic();
              if (newTopics.contains(topic)) {
                return Mono.error(new VisibleException("topic: " + topic + " 重复订阅"));
              }
              newTopics.add(topic);
              Subscription subscription = currentSubMap.get(topic);
              if (subscription == null) {
                // 之前没有这个主题的订阅关系 -> 新增
                Subscription newSubs = SubscriptionConverter.fromSubscriptionArgs(args);
                newSubs.setApplicationId(applicationId);
                changeList.add(newSubs);
              } else {
                // 之前有这个主题的订阅关系并且发生了变更 -> 更新
                Subscription calculateChange = calculateChange(subscription, args);
                if (calculateChange != null) {
                  changeList.add(calculateChange);
                }
              }
            }
            // 计算出解除订阅列表
            List<Long> unsubscribeList = new ArrayList<>();
            for (Subscription subscription : subscriptions) {
              // 新的订阅关系列表中没有这个主题 -> 解除该订阅关系
              if (!newTopics.contains(subscription.getTopic())) {
                unsubscribeList.add(subscription.getSubscriptionId());
              }
            }
            log.debug("变更订阅关系: {} 条, 解除订阅关系: {} 条",
                changeList.size(), unsubscribeList.size());
            Mono<List<Subscription>> saveAll = storage.saveAll(changeList);
            Mono<Long> unsubscribeAll = storage.unsubscribeAll(unsubscribeList);
            Mono<List<Subscription>> result = Mono.zip(saveAll, unsubscribeAll)
                .flatMap(t -> storage.findAllByApplication(applicationId));

            if (changeList.size() > 0 || unsubscribeList.size() > 0) {
              return result.doOnNext(res -> cacheService.notificationRefreshCache().subscribe());
            } else {
              return result;
            }
          });
        })
        .doOnNext(res -> {
          if (log.isDebugEnabled()) {
            log.debug("自动订阅结果: {}", JsonUtils.toJsonString(res));
          }
        });
  }

  /**
   * 计算新的订阅关系是否发生变更
   * <pre>
   *   满足以下条件则说明订阅关系发生了变更
   *    - condition 不为null, 且发生了变动
   *    - broadcast 不为null, 且发生了变动
   *    - retryCount 不为null, 且发生了变动
   * </pre>
   *
   * @param subscription     原订阅关系
   * @param subscriptionArgs 新订阅关系
   * @return 合并后的订阅关系, 如果没有变更则返回<code>null</code>
   */
  @Nullable
  private Subscription calculateChange(@Nonnull Subscription subscription,
                                       @Nonnull SubscriptionArgs subscriptionArgs) {
    boolean changed = false;
    String condition = subscriptionArgs.getCondition();
    if (condition != null && !condition.equals(subscription.getCondition())) {
      changed = true;
      subscription.setCondition(condition);
    }
    Boolean broadcast = subscriptionArgs.getBroadcast();
    if (broadcast != null && broadcast != subscription.isBroadcast()) {
      changed = true;
      subscription.setBroadcast(broadcast);
    }
    Integer retryCount = subscriptionArgs.getRetryCount();
    if (retryCount != null && retryCount != subscription.getRetryCount()) {
      changed = true;
      subscription.setRetryCount(retryCount);
    }
    if (changed) {
      return subscription;
    } else {
      return null;
    }
  }

  @Nonnull
  public Mono<Long> unsubscribe(long applicationId, @Nonnull String topic) {
    return storage.unsubscribe(applicationId, topic);
  }

  @Nonnull
  public Mono<Long> unsubscribe(@Nonnull String topic) {
    return storage.unsubscribeAll(topic);
  }

  @Nonnull
  public Mono<Long> unsubscribe(long applicationId) {
    return storage.unsubscribeAll(applicationId);
  }

  @Nonnull
  public Mono<List<Subscription>> getSubscription(long applicationId) {
    return storage.findAllByApplication(applicationId);
  }

  @Nonnull
  public Mono<List<Subscription>> findAllEnabled() {
    return storage.findAllEnabled();
  }

}
