package com.zzsong.bus.client.impl;

import com.zzsong.bus.client.BusAdmin;
import com.zzsong.bus.common.transfer.ResubscribeArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2021/5/25
 */
@CommonsLog
@RequiredArgsConstructor
public class BusAdminImpl implements BusAdmin {
  private final String baseUrl;
  private final WebClient webClient;

  @Override
  public void resubscribe(@Nonnull ResubscribeArgs args) {
    String url = baseUrl + "/subscription/resubscribe";
    webClient.post().uri(url).contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(args))
        .retrieve().bodyToMono(String.class)
        .doOnNext(res -> log.info("resubscribe res: " + res))
        .block();
  }
}
