package com.zzsong.bus.core.socket.http;

import com.zzsong.bus.abs.core.MessageRouter;
import com.zzsong.bus.abs.domain.EventInstance;
import com.zzsong.bus.abs.pojo.PublishResult;
import com.zzsong.bus.common.message.EventMessage;
import com.zzsong.bus.common.transfer.Res;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/17
 */
@RestController
@RequestMapping("/publish")
public class PublishController {
  @Nonnull
  private final MessageRouter messageRouter;

  public PublishController(@Nonnull MessageRouter messageRouter) {
    this.messageRouter = messageRouter;
  }

  /**
   * 单条发布
   */
  @Nonnull
  @PostMapping("/single")
  public Mono<PublishResult> publish(@RequestBody @Nonnull
                                         EventInstance message) {
    return messageRouter.route(message);
  }

  /**
   * 批量发布
   */
  @Nonnull
  @PostMapping("/batch")
  public Flux<PublishResult> batchPublish(@RequestBody @Nonnull
                                              List<EventInstance> messages) {
    return Flux.fromIterable(messages).flatMap(messageRouter::route);
  }
}
