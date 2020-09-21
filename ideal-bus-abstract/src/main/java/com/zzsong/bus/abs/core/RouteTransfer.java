package com.zzsong.bus.abs.core;

import com.zzsong.bus.abs.domain.RouteInstance;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/19 8:04 下午
 */
public interface RouteTransfer {

  Mono<Boolean> submit(@Nonnull List<RouteInstance> routeInstanceList);
}
