package com.zzsong.bus.broker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@Component
@ConfigurationProperties("ideal.bus")
public class BusProperties {

  /** 当前节点的nodeId, 集群部署每个节点都必须有自己的id */
  private int nodeId = -1;

  private boolean enableExpireScheduler = false;

  /** 执行成功的路由实例过期时间, 至少一天 */
  private Duration routeInstanceExpire = Duration.ofDays(15);

  /** 事件实例的过期配置 */
  private List<EventInstanceExpire> eventInstanceExpires = new ArrayList<>();

  /** 本地缓存的刷新间隔 */
  private Duration refreshLocalCacheInterval = Duration.ofMinutes(10);

  @Getter
  @Setter
  public static class EventInstanceExpire {
    @Nonnull
    private Duration expire = Duration.ofDays(15);
    private Set<String> topics;
  }
}
