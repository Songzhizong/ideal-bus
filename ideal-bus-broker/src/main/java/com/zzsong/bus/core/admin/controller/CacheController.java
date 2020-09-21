package com.zzsong.bus.core.admin.controller;

import com.zzsong.bus.abs.share.Res;
import com.zzsong.bus.core.admin.service.CacheService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/21
 */
@RestController
@RequestMapping("/cache")
public class CacheController {
  @Nonnull
  private final CacheService cacheService;

  public CacheController(@Nonnull CacheService cacheService) {
    this.cacheService = cacheService;
  }

  @Nonnull
  @GetMapping("/refresh/notice")
  Mono<Res<Boolean>> refreshCacheNotice() {
    return cacheService.notificationRefreshCache().map(Res::data);
  }

  @Nonnull
  @GetMapping("/refresh")
  Mono<Res<Boolean>> refreshCache() {
    return cacheService.refreshLocalCache().map(Res::data);
  }
}
