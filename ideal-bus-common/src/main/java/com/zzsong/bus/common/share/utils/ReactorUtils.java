package com.zzsong.bus.common.share.utils;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * @author 宋志宗 on 2020/8/21
 */
public final class ReactorUtils {
  private ReactorUtils() {
  }

  @Nonnull
  public static WebClient createWebClient(@Nonnull Duration responseTimeout) {
    return createWebClientBuilder(responseTimeout).build();
  }

  @Nonnull
  public static WebClient.Builder createWebClientBuilder(@Nonnull Duration responseTimeout) {
    HttpClient httpClient = createHttpClient(responseTimeout);
    return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
  }

  @Nonnull
  public static HttpClient createHttpClient(@Nonnull Duration responseTimeout) {
    return HttpClient.create().responseTimeout(responseTimeout).keepAlive(true);
  }
}
