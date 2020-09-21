package com.zzsong.bus.core.admin.service;

import com.zzsong.bus.core.processor.LocalCache;
import com.zzsong.common.utils.ReactorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2020/9/21
 */
@Slf4j
@Service
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class CacheService {
  private final WebClient webClient = ReactorUtils
      .createWebClient(200, 200, 400);

  @Value("${spring.application.name}")
  private String applicationName;

  @Nonnull
  @Autowired
  private LocalCache localCache;
  @Nonnull
  private final DiscoveryClient discoveryClient;

  public CacheService(@Nonnull DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  /**
   * 通知各个节点更新缓存
   */
  public Mono<Boolean> notificationRefreshCache() {
    log.debug("通知各节点更新缓存");
    List<ServiceInstance> instances = discoveryClient.getInstances(applicationName);
    final List<String> ipPorts = instances.stream()
        .map(instance -> "http://" + instance.getHost() + ":" + instance.getPort() + "/cache/refresh")
        .collect(Collectors.toList());
    return Flux.fromIterable(ipPorts)
        .flatMap(url -> webClient.get().uri(url)
            .retrieve().bodyToMono(String.class)
        ).collectList()
        .map(list -> true);
  }

  public Mono<Boolean> refreshLocalCache() {
    localCache.refreshCache();
    return Mono.just(true);
  }
}
