package com.zzsong.bus.client.rsocket;

import com.zzsong.bus.client.ConsumerExecutor;
import com.zzsong.bus.common.constants.RSocketRoute;
import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.message.DeliverResult;
import org.springframework.messaging.handler.annotation.MessageMapping;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2021/4/28
 */
public class ReceiveRSocketChannelImpl extends AbstractRSocketChannel implements ReceiveRSocketChannel {
  private final ConsumerExecutor consumerExecutor;

  public ReceiveRSocketChannelImpl(@Nonnull String brokerIp,
                                   int brokerPort,
                                   long applicationId,
                                   @Nonnull String clientIpPort,
                                   @Nullable String accessToken,
                                   @Nonnull ConsumerExecutor consumerExecutor) {
    super(brokerIp, brokerPort, applicationId, clientIpPort, accessToken);
    this.consumerExecutor = consumerExecutor;
    consumerExecutor.addListener(this);
  }

  /**
   * 接收broker交付的事件
   *
   * @param event 事件信息
   * @return 交付结果
   * @author 宋志宗 on 2021/4/29
   */
  @Nonnull
  @MessageMapping(RSocketRoute.MESSAGE_DELIVER)
  public Mono<DeliverResult> deliver(@Nonnull DeliverEvent event) {
    boolean submit = consumerExecutor.submit(event, this);
    DeliverResult result = new DeliverResult();
    result.setEventId(event.getEventId());
    if (submit) {
      result.setStatus(DeliverResult.Status.SUCCESS);
      result.setMessage("success");
    } else {
      result.setStatus(DeliverResult.Status.BUSY);
      result.setMessage("busy");
    }
    return Mono.just(result);
  }

  @Override
  public void close() {
    super.close();
  }

  @Override
  public void ack(long routeInstanceId) {

  }

  @Override
  public void reject(long routeInstanceId) {

  }

  @Override
  public void onBusy() {

  }

  @Override
  public void onIdle() {

  }
}
