package com.zzsong.bus.broker.connect.http;

import com.zzsong.bus.abs.constants.ApplicationType;
import com.zzsong.bus.broker.connect.DelivererChannel;
import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
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
import java.util.List;

/**
 * @author 宋志宗 on 2020/11/25
 */
@Slf4j
@RequiredArgsConstructor
public class HttpDelivererChannel implements DelivererChannel {
  /** 2分钟超时时间 */
  private final WebClient webClient = ReactorUtils.createWebClient(Duration.ofMinutes(2));
  @Nonnull
  private final String url;
  @Nonnull
  private final ApplicationType applicationType;
  @Nullable
  private String instanceId = null;

  @Nonnull
  @Override
  public Mono<DeliveredResult> deliver(@Nonnull DeliveredEvent event) {
    return webClient.post().uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(event))
        .retrieve()
        .bodyToMono(String.class)
        .map(res ->
            switch (applicationType) {
              case INTERNAL -> JsonUtils.parseJson(res, DeliveredResult.class);
              case EXTERNAL -> {
                DeliveredResult deliveredResult = new DeliveredResult();
                deliveredResult.setEventId(event.getEventId());
                deliveredResult.setSuccess(true);
                List<String> listeners = event.getListeners();
                if (listeners != null) {
                  for (String listener : listeners) {
                    deliveredResult.markAck(listener, true);
                  }
                }
                yield deliveredResult;
              }
            }
        )
        .onErrorResume(throwable -> {
          DeliveredResult deliveredResult = new DeliveredResult();
          deliveredResult.setEventId(event.getEventId());
          deliveredResult.setSuccess(false);
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
