package com.zzsong.bus.core.processor;

import com.google.common.base.Joiner;
import com.zzsong.bus.abs.core.RouteTransfer;
import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.common.message.EventHeaders;
import com.zzsong.bus.common.message.PublishResult;
import com.zzsong.bus.abs.pojo.SubscriptionDetails;
import com.zzsong.bus.common.util.ConditionMatcher;
import com.zzsong.bus.core.admin.service.EventInstanceService;
import com.zzsong.bus.core.admin.service.RouteInstanceService;
import com.zzsong.bus.core.config.BusProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nfunk.jep.JEP;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Slf4j
@Component
public class EventExchange {

  @Nonnull
  private final LocalCache localCache;
  @Nonnull
  private final BusProperties properties;
  @Nonnull
  private final RouteTransfer routeTransfer;
  @Nonnull
  private final RouteInstanceService routeInstanceService;
  @Nonnull
  private final EventInstanceService eventInstanceService;

  public EventExchange(@Nonnull LocalCache localCache,
                       @Nonnull BusProperties properties,
                       @Nonnull RouteTransfer routeTransfer,
                       @Nonnull RouteInstanceService routeInstanceService,
                       @Nonnull EventInstanceService eventInstanceService) {
    this.localCache = localCache;
    this.properties = properties;
    this.routeTransfer = routeTransfer;
    this.routeInstanceService = routeInstanceService;
    this.eventInstanceService = eventInstanceService;
  }

  @Nonnull
  public Mono<PublishResult> publish(@Nonnull EventInstance event) {
    PublishResult.PublishResultBuilder builder = PublishResult.builder()
        .eventId(event.getEventId())
        .bizId(event.getBizId())
        .topic(event.getTopic())
        .success(true);
    // 路由, 获取满足订阅条件的订阅者列表
    Mono<List<RouteInstance>> route = route(event)
        .flatMap(list -> {
          if (list.isEmpty()) {
            event.setStatus(EventInstance.NOT_ROUTED);
          } else {
            event.setStatus(EventInstance.ROUTED);
          }
          return eventInstanceService.save(event).map(ins -> list);
        });
    return route.flatMap(instanceList -> {
      if (instanceList.isEmpty()) {
        PublishResult publishResult = builder.message("该事件没有订阅者").build();
        return Mono.just(publishResult);
      }
      List<RouteInstance> collect = instanceList.stream()
          .filter(instance -> instance.getNextPushTime() < 1L)
          .collect(Collectors.toList());
      return routeTransfer.submit(collect, false).map(b -> builder.message("success").build());
    });
  }

  @Nonnull
  private Mono<List<RouteInstance>> route(@Nonnull EventInstance event) {
    // 保存事件实例
    String topic = event.getTopic();
    List<SubscriptionDetails> subscription = localCache.getTopicSubscription(topic);
    if (subscription.isEmpty()) {
      return Mono.just(Collections.emptyList());
    }
    EventHeaders headers = event.getHeaders();
    List<RouteInstance> routeInstanceList = new ArrayList<>();
    for (SubscriptionDetails details : subscription) {
      List<Set<String>> group = details.getConditionGroup();
      if (!ConditionMatcher.match(group, headers)) {
        continue;
      }
      RouteInstance instance = createRouteInstance(event, details);
      routeInstanceList.add(instance);
    }
    if (routeInstanceList.isEmpty()) {
      return Mono.just(Collections.emptyList());
    }
    return routeInstanceService.saveAll(routeInstanceList);
  }

  @Nonnull
  private RouteInstance createRouteInstance(@Nonnull EventInstance event,
                                            @Nonnull SubscriptionDetails details) {
    RouteInstance instance = new RouteInstance();
    instance.setNodeId(properties.getNodeId());
    instance.setEventId(event.getEventId());
    instance.setKey(event.getKey());
    instance.setSubscriptionId(details.getSubscriptionId());
    instance.setApplicationId(details.getApplicationId());
    instance.setTopic(event.getTopic());
    instance.setStatus(RouteInstance.STATUS_WAITING);
    instance.setMessage("waiting");
    String delayExp = details.getDelayExp();
//    long delaySeconds = 0;
//    try {
//      delaySeconds = parseDelayExp(event.getHeaders(), delayExp);
//    } catch (Exception e) {
//      log.info("event: {} subscription: {} 接续延迟表达式失败: {}",
//          event.getEventId(), details.getSubscriptionId(), e.getMessage());
//    }
//    instance.setNextPushTime(System.currentTimeMillis() + (delaySeconds * 1000));
    return instance;
  }

  @Nonnull
  private static String parseDelayExp(@Nullable String delayExp) {
    if (StringUtils.isBlank(delayExp)) {
      return "";
    }
    // 存储变量列表
    List<String> variables = new ArrayList<>();
    char[] chars = delayExp.toCharArray();
    StringBuilder builder = new StringBuilder();
    boolean containsOperator = false;
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (isOperator(c)) {
        containsOperator = true;
        if (StringUtils.isNotBlank(builder)) {
          variables.add(builder.toString());
          builder = new StringBuilder();
        }
        continue;
      }
      if (StringUtils.isNotBlank(builder) || c < '0' || c > '9') {
        builder.append(c);
      }
      if (i == chars.length - 1) {
        String s = builder.toString();
        if (StringUtils.isNotBlank(s)) {
          variables.add(s);
        }
      }
    }
    System.out.println(Joiner.on(", ").join(variables));
    return "";
  }

  private static boolean isOperator(char c) {
    return c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '(' || c == ')';
  }


  public static void main(String[] args) {
//    JEP jep = new JEP();
//    jep.parseExpression("(100)");
//    System.out.println(jep.getValue());

    parseDelayExp("");
  }
}
