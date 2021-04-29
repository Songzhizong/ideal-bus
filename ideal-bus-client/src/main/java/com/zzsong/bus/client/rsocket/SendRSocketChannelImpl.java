package com.zzsong.bus.client.rsocket;

import com.zzsong.bus.common.constants.RSocketRoute;
import com.zzsong.bus.common.message.EventMessage;
import com.zzsong.bus.common.message.PublishResult;
import com.zzsong.bus.common.share.utils.JsonUtils;
import com.zzsong.bus.common.transfer.ResubscribeArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author 宋志宗 on 2021/4/29
 */
@Slf4j
public class SendRSocketChannelImpl extends AbstractRSocketChannel implements SendRSocketChannel {
  private static final ParameterizedTypeReference<PublishResult> PUBLISH_RESULT_RES
      = new ParameterizedTypeReference<PublishResult>() {
  };

  protected SendRSocketChannelImpl(int socketType,
                                   @Nonnull String brokerIp,
                                   int brokerPort,
                                   long applicationId,
                                   @Nonnull String clientIpPort,
                                   @Nullable String accessToken) {
    super(socketType, brokerIp, brokerPort, applicationId, clientIpPort, accessToken);
  }

  @Override
  public Mono<PublishResult> publishEvent(@Nonnull EventMessage<?> message) {
    if (socketRequester == null) {
      return Mono.error(new IllegalArgumentException("socketRequester is null"));
    }
    return socketRequester.route(RSocketRoute.PUBLISH)
        .data(message)
        .retrieveMono(PUBLISH_RESULT_RES)
        .doOnNext(res -> {
          if (log.isDebugEnabled()) {
            log.debug("\nPublish result: \n{}",
                JsonUtils.toJsonString(res, true, true));
          }
        }).subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<Boolean> resubscribe(@Nonnull ResubscribeArgs resubscribeArgs) {
    if (socketRequester == null) {
      return Mono.error(new IllegalArgumentException("socketRequester is null"));
    }
    return socketRequester.route(RSocketRoute.AUTO_SUBSCRIBE)
        .data(resubscribeArgs)
        .retrieveMono(String.class)
        .doOnNext(res -> {
          if (log.isDebugEnabled()) {
            log.debug("autoSubscribe result: {}", res);
          }
        })
        .map(s -> true);
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    return brokerAddress;
  }

  @Override
  public boolean heartbeat() {
    return running
        && !destroyed
        && socketRequester != null
        && socketRequester.rsocket() != null
        && !Objects.requireNonNull(socketRequester.rsocket()).isDisposed();
  }

  @Override
  public void dispose() {
    super.close();
  }
}
