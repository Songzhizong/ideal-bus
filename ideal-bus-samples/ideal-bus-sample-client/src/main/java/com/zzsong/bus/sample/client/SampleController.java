package com.zzsong.bus.sample.client;

import com.google.common.collect.ImmutableList;
import com.zzsong.bus.client.EventPublisher;
import com.zzsong.bus.common.message.EventMessage;
import com.zzsong.bus.common.share.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/20 2:31 下午
 */
@Slf4j
@RestController
@RequestMapping("/client")
public class SampleController {
  @Nonnull
  private final EventPublisher eventPublisher;

  public SampleController(@Nonnull EventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  /**
   * 这是一个同步发布事件的示例
   */
  @GetMapping("/testBlock/{topic}")
  public void testBlock(@PathVariable("topic") String topic) {
    long start = System.currentTimeMillis();
    List<String> payload = ImmutableList.of("1", "2", "3");
    eventPublisher.publish(EventMessage.of(topic, payload))
        .map(JsonUtils::toJsonString)
        .doOnNext(log::info)
        .block(); // block终结符会以阻塞当前线程的方式执行事件发布
    log.info("完成发布, 当前线程耗时: {}", System.currentTimeMillis() - start);
  }

  /**
   * 这是一个异步发布事件的示例
   */
  @GetMapping("/testAsync/{topic}")
  public void testAsync(@PathVariable("topic") String topic) {
    long start = System.currentTimeMillis();
    List<String> payload = ImmutableList.of("1", "2", "3");
    EventMessage<List<String>> message = EventMessage.of(topic, payload)
        .transactionId("123456")
        .aggregate("key")
        .externalApplication("externalApp")
        .addHeader("age", "20");
    eventPublisher.publish(message)
        .map(JsonUtils::toJsonString)
        .doOnNext(log::info)
        .doFinally(s -> log.info("发布耗时: {}", System.currentTimeMillis() - start))
        .subscribe(); // .subscribe() 终结符默认会在reactor线程中执行事件发布, 正常情况下这是一个非IO阻塞的操作.
    log.info("完成发布, 当前线程耗时: {}", System.currentTimeMillis() - start);
  }
}
