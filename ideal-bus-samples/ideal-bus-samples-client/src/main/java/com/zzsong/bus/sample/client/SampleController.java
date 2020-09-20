package com.zzsong.bus.sample.client;

import com.google.common.collect.ImmutableList;
import com.zzsong.bus.client.Publisher;
import com.zzsong.bus.common.message.EventMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/20 2:31 下午
 */
@RestController
@RequestMapping("/client")
public class SampleController {
  @Nonnull
  private final Publisher publisher;

  public SampleController(@Nonnull Publisher publisher) {
    this.publisher = publisher;
  }

  @GetMapping("/test1")
  public void test() {
    publisher.publish(EventMessage.of("test", ImmutableList.of("1", "2", "3")))
        .doOnNext(System.out::println).block();
  }
}
