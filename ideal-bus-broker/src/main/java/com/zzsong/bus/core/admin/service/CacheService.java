package com.zzsong.bus.core.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author 宋志宗 on 2020/9/21
 */
@Slf4j
@Service
public class CacheService {
  /**
   * 通知各个节点更新缓存
   */
  public Mono<Boolean> notificationRefreshCache() {
    log.debug("通知各节点更新缓存");
    return Mono.just(true);
  }
}
