package com.zzsong.bus.broker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.Duration;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@Component
@ConfigurationProperties("ideal.bus")
public class BusProperties {
  private static final Duration MIN_ROUTE_EXPIRE = Duration.ofDays(1);
  private static final long MIN_ROUTE_EXPIRE_SECONDS = MIN_ROUTE_EXPIRE.getSeconds();

  /** 当前节点的nodeId, 集群部署每个节点都必须有自己的id */
  private int nodeId = -1;

  /** 执行成功的路由实例过期时间, 至少一天 */
  private Duration routeInstanceExpire = Duration.ofDays(15);

  /** 本地缓存的刷新间隔 */
  private Duration refreshLocalCacheInterval = Duration.ofMinutes(10);

  public Duration getRouteInstanceExpire() {
    if (routeInstanceExpire.getSeconds() < MIN_ROUTE_EXPIRE_SECONDS) {
      return MIN_ROUTE_EXPIRE;
    }
    return routeInstanceExpire;
  }

  public void setRouteInstanceExpire(@Nullable Duration routeInstanceExpire) {
    if (routeInstanceExpire == null) {
      return;
    }
    if (routeInstanceExpire.getSeconds() < MIN_ROUTE_EXPIRE_SECONDS) {
      routeInstanceExpire = MIN_ROUTE_EXPIRE;
    }
    this.routeInstanceExpire = routeInstanceExpire;
  }
}
