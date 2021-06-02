package com.zzsong.bus.sample.client;

import com.zzsong.bus.client.EventPublisher;
import com.zzsong.bus.common.message.EventMessage;
import com.zzsong.bus.common.message.ExchangeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;

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

  @PostMapping("/publish")
  public ExchangeResult publish(@RequestBody EventMessage<Object> eventMessage) {
    return eventPublisher.publish(eventMessage).block();
  }
}
