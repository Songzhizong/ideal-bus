package com.zzsong.bus.client.rsocket;

import com.zzsong.bus.client.BusyListener;
import com.zzsong.bus.client.EventConsumer;
import com.zzsong.bus.common.constants.RSocketRoute;
import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.message.DeliverResult;
import org.springframework.messaging.handler.annotation.MessageMapping;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * @author 宋志宗 on 2021/4/28
 */
public class ReceiveRSocketChannelImpl extends AbstractRSocketChannel implements ReceiveRSocketChannel, BusyListener {
  private final String listenerId = UUID.randomUUID().toString();
  private final EventConsumer eventConsumer;

  protected ReceiveRSocketChannelImpl(int socketType,
                                      @Nonnull String brokerIp,
                                      int brokerPort,
                                      long applicationId,
                                      @Nonnull String clientIpPort,
                                      @Nullable String accessToken,
                                      EventConsumer eventConsumer) {
    super(socketType, brokerIp, brokerPort, applicationId, clientIpPort, accessToken);
    this.eventConsumer = eventConsumer;
    this.eventConsumer.registerBusyListener(this);
  }

  /**
   * 接收broker交付的事件
   *
   * @param event 事件信息
   * @return 交付结果
   * @author 宋志宗 on 2021/4/29
   */
  @Nonnull
  @MessageMapping(RSocketRoute.CLIENT_RECEIVE)
  public Mono<DeliverResult> receive(@Nonnull DeliverEvent event) {
    return eventConsumer.receive(event);
  }

  @Override
  public String getListenerId() {
    return listenerId;
  }

  @Override
  public void busyNotice() {

  }

  @Override
  public void close() {
    this.eventConsumer.removeBusyListener(this);
    super.close();
  }
}
