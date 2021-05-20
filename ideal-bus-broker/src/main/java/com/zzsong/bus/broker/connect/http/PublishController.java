package com.zzsong.bus.broker.connect.http;

import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.broker.core.exchanger.ExchangeResult;
import com.zzsong.bus.broker.core.exchanger.Exchanger;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/17
 */
@RestController
@RequestMapping("/publish")
@RequiredArgsConstructor
public class PublishController {
  private final Exchanger exchanger;

  /**
   * 单条发布
   */
  @Nonnull
  @PostMapping("/single")
  public Mono<ExchangeResult> publish(@RequestBody @Nonnull EventInstance message) {
    return exchanger.exchange(Collections.singletonList(message));
  }

  /**
   * 批量发布
   */
  @Nonnull
  @PostMapping("/batch")
  public Mono<ExchangeResult> publish(@RequestBody @Nonnull List<EventInstance> messages) {
    return exchanger.exchange(messages);
  }
}
