package com.zzsong.bus.broker.core.consumer;

import com.zzsong.bus.abs.storage.RouteInstanceStorage;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 宋志宗 on 2021/5/19
 */
@Component
public class SimpleConsumerManager implements ConsumerManager {
  private final Map<Long, Consumer> consumerMap = new ConcurrentHashMap<>();

  @Nonnull
  @Override
  public Consumer loadConsumer(long applicationId, @Nonnull RouteInstanceStorage routeInstanceStorage) {
    return consumerMap.computeIfAbsent(applicationId, k -> new SimpleConsumer(applicationId, routeInstanceStorage));
  }
}
