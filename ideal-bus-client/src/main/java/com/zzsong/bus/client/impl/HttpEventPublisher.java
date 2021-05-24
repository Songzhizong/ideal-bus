package com.zzsong.bus.client.impl;

import com.zzsong.bus.client.EventPublisher;
import com.zzsong.bus.common.message.EventMessage;
import com.zzsong.bus.common.message.ExchangeResult;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author 宋志宗 on 2021/5/24
 */
public class HttpEventPublisher implements EventPublisher {
  private final String baseUrl;
  private final WebClient webClient;

  public HttpEventPublisher(@Nonnull String baseUrl, @Nonnull WebClient webClient) {
    this.baseUrl = baseUrl;
    this.webClient = webClient;
  }

  @Nonnull
  @Override
  public Mono<ExchangeResult> publish(@Nonnull EventMessage<?> message) {
    List<EventMessage<?>> messages = Collections.singletonList(message);
    return publish(messages);
  }

  @Nonnull
  @Override
  public Mono<ExchangeResult> publish(@Nonnull Collection<EventMessage<?>> messages) {
    String url = baseUrl + "/publish/batch";
    return webClient.post().uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(messages))
        .retrieve().bodyToMono(ExchangeResult.class);
  }

  @Nonnull
  @Override
  public Mono<ExchangeResult> publish(@Nonnull Flux<EventMessage<?>> messages) {
    return messages.collectList().flatMap(this::publish);
  }
}
