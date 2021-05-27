package com.zzsong.bus.client.rsocket;

import com.zzsong.bus.client.ConsumerExecutor;
import com.zzsong.bus.common.constants.RSocketRoute;
import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.message.DeliverResult;
import com.zzsong.bus.common.transfer.AckArgs;
import com.zzsong.bus.common.transfer.ChannelArgs;
import com.zzsong.bus.common.transfer.RejectArgs;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.messaging.handler.annotation.MessageMapping;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;

/**
 * @author 宋志宗 on 2021/4/28
 */
@CommonsLog
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

  @Override
  public void close() {
    super.close();
  }

  @Override
  public void ack(long routeInstanceId) {
    AckArgs args = new AckArgs();
    args.setRouteInstanceId(routeInstanceId);
    if (super.socketRequester == null) {
      super.restartSocket("super.socketRequester is null");
      return;
    }
    super.socketRequester.route(RSocketRoute.MESSAGE_ACK)
        .data(args)
        .retrieveMono(Boolean.class)
        .retryWhen(Retry.fixedDelay(5, Duration.ofMillis(500)))
        .doOnNext(b -> log.debug("ack message " + routeInstanceId))
        .subscribe();
  }

  @Override
  public void reject(long routeInstanceId, @Nullable String message) {
    RejectArgs args = new RejectArgs();
    args.setRouteInstanceId(routeInstanceId);
    args.setMessage(message);
    if (super.socketRequester == null) {
      super.restartSocket("super.socketRequester is null");
      return;
    }
    super.socketRequester.route(RSocketRoute.MESSAGE_REJECT)
        .data(args)
        .retrieveMono(Boolean.class)
        .retryWhen(Retry.fixedDelay(5, Duration.ofMillis(500)))
        .doOnNext(b -> log.debug("reject message " + routeInstanceId + " -> " + message))
        .subscribe();
  }

  @Override
  public void onBusy() {
    channelChangeStatus(ChannelArgs.STATUS_BUSY);
  }

  @Override
  public void onIdle() {
    channelChangeStatus(ChannelArgs.STATUS_IDLE);
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

  private void channelChangeStatus(int status) {
    ChannelArgs channelArgs = new ChannelArgs();
    channelArgs.setApplicationId(applicationId);
    channelArgs.setInstanceId(super.clientIpPort);
    channelArgs.setStatus(status);
    if (super.socketRequester == null) {
      super.restartSocket("super.socketRequester is null");
      return;
    }
    super.socketRequester.route(RSocketRoute.CHANNEL_CHANGE_STATUS)
        .data(channelArgs)
        .retrieveMono(Boolean.class)
        .retryWhen(Retry.fixedDelay(5, Duration.ofMillis(500)))
        .doOnNext(b -> log.info(super.brokerAddress + "change status to " + status))
        .subscribe();
  }
}
