package com.zzsong.bus.broker.connect.http;

import com.zzsong.bus.abs.constants.ApplicationType;
import com.zzsong.bus.broker.connect.DelivererChannel;
import com.zzsong.bus.common.message.DeliverEvent;
import com.zzsong.bus.common.message.DeliverResult;
import com.zzsong.bus.common.share.utils.JsonUtils;
import com.zzsong.bus.common.share.utils.ReactorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;

/**
 * @author 宋志宗 on 2020/11/25
 */
@Slf4j
@RequiredArgsConstructor
public class HttpDelivererChannel implements DelivererChannel {
  private static final WebClient WEB_CLIENT
      = ReactorUtils.createWebClient(Duration.ofSeconds(10));
  @Nonnull
  private final String url;
  @Nonnull
  private final ApplicationType applicationType;
  @Nullable
  private String instanceId = null;

  @Nonnull
  @Override
  public Mono<DeliverResult> deliver(@Nonnull DeliverEvent event) {
    return WEB_CLIENT.post().uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(event))
        .retrieve()
        .bodyToMono(String.class)
        .map(res -> {
              if (applicationType == ApplicationType.INTERNAL) {
                return JsonUtils.parseJson(res, DeliverResult.class);
              } else {
                DeliverResult deliveredResult = new DeliverResult();
                deliveredResult.setEventId(event.getEventId());
                deliveredResult.setStatus(DeliverResult.Status.ACK);
                return deliveredResult;
              }
            }
        )
        .onErrorResume(throwable -> {
          DeliverResult deliveredResult = new DeliverResult();
          deliveredResult.setEventId(event.getEventId());
          deliveredResult.setStatus(DeliverResult.Status.UN_ACK);
          return Mono.just(deliveredResult);
        });
  }

  @Nonnull
  @Override
  public String getInstanceId() {
    if (instanceId == null) {
      synchronized (url) {
        if (instanceId == null) {
          String[] split = StringUtils.split(url, "//", 2);
          String s;
          if (split.length == 1) {
            s = split[0];
          } else {
            s = split[1];
          }
          instanceId = StringUtils.split(s, "/", 2)[0];
        }
      }
    }
    return instanceId;
  }

  @Override
  public boolean heartbeat() {
    return true;
  }
}
