package com.zzsong.bus.sample.client;

import com.google.common.collect.ImmutableList;
import com.zzsong.bus.client.Publisher;
import com.zzsong.bus.common.message.EventMessage;
import com.zzsong.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/20 2:31 下午
 */
@Slf4j
@RestController
@RequestMapping("/client")
public class SampleController {
  @Nonnull
  private final Publisher publisher;

  public SampleController(@Nonnull Publisher publisher) {
    this.publisher = publisher;
  }

  /**
   * 这是一个同步发布事件的示例
   */
  @GetMapping("/testBlock")
  public void testBlock() {
    final long start = System.currentTimeMillis();
    final List<String> payload = ImmutableList.of("1", "2", "3");
    publisher.publish(EventMessage.of("test", payload))
        .map(JsonUtils::toJsonString)
        .doOnNext(log::info)
        .block(); // block终结符会以阻塞当前线程的方式执行事件发布
    log.info("完成发布, 当前线程耗时: {}", System.currentTimeMillis() - start);
  }

  /**
   * 这是一个异步发布事件的示例
   * <pre>
   *   .subscribe() 终结符默认会在reactor线程中执行事件发布, 正常情况下这是一个非IO阻塞的操作.
   *   注意: 在reactor管道中执行IO阻塞操作将会快速消耗reactor线程, 并发量大的情况下可能会导致服务不可用
   * </pre>
   */
  @GetMapping("/testAsync")
  public void testAsync() {
    final long start = System.currentTimeMillis();
    final List<String> payload = ImmutableList.of("1", "2", "3");
    publisher.publish(EventMessage.of("test", payload))
        .map(JsonUtils::toJsonString)
        .doOnNext(log::info)
        .doFinally(s -> log.info("发布耗时: {}", System.currentTimeMillis() - start))
        .subscribe();
    log.info("完成发布, 当前线程耗时: {}", System.currentTimeMillis() - start);
  }

  @GetMapping("/testDelay")
  public void testDelay() {
    final long start = System.currentTimeMillis();
    final List<String> payload = ImmutableList.of("1", "2", "3");
    publisher.publish(EventMessage.of("test", payload).delay(Duration.ofSeconds(10)))
        .map(JsonUtils::toJsonString)
        .doOnNext(log::info)
        .block();
    log.info("完成发布, 当前线程耗时: {}", System.currentTimeMillis() - start);
  }
}