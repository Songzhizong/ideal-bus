package com.zzsong.bus.storage.mongo.converter;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.generator.SnowFlake;
import com.zzsong.bus.storage.mongo.document.RouteInstanceDo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/19 7:28 下午
 */
@SuppressWarnings("DuplicatedCode")
public final class RouteInstanceDoConverter {
  private RouteInstanceDoConverter() {
  }

  @Nonnull
  public static RouteInstanceDo fromRouteInstance(@Nonnull RouteInstance routeInstance) {
    RouteInstanceDo routeInstanceDo = new RouteInstanceDo();
    routeInstanceDo.setInstanceId(routeInstance.getInstanceId());
    routeInstanceDo.setNodeId(routeInstance.getNodeId());
    routeInstanceDo.setEventId(routeInstance.getEventId());
    routeInstanceDo.setKey(routeInstance.getKey());
    routeInstanceDo.setSubscriptionId(routeInstance.getSubscriptionId());
    routeInstanceDo.setApplicationId(routeInstance.getApplicationId());
    routeInstanceDo.setTopic(routeInstance.getTopic());
    long nextPushTime = routeInstance.getNextPushTime();
    if (nextPushTime > SnowFlake.START_TIMESTAMP) {
      routeInstanceDo.setNextPushTime(nextPushTime);
    }
    routeInstanceDo.setStatus(routeInstance.getStatus());
    routeInstanceDo.setRetryCount(routeInstance.getRetryCount());
    routeInstanceDo.setMessage(routeInstance.getMessage());
    routeInstanceDo.setListeners(routeInstance.getListeners());
    routeInstanceDo.setUnAckListeners(routeInstance.getUnAckListeners());
    return routeInstanceDo;
  }

  @Nonnull
  public static RouteInstance toRouteInstance(@Nonnull RouteInstanceDo routeInstanceDo) {
    RouteInstance routeInstance = new RouteInstance();
    routeInstance.setInstanceId(routeInstanceDo.getInstanceId());
    routeInstance.setNodeId(routeInstanceDo.getNodeId());
    routeInstance.setEventId(routeInstanceDo.getEventId());
    routeInstance.setKey(routeInstanceDo.getKey());
    routeInstance.setSubscriptionId(routeInstanceDo.getSubscriptionId());
    routeInstance.setApplicationId(routeInstanceDo.getApplicationId());
    routeInstance.setTopic(routeInstanceDo.getTopic());
    routeInstance.setNextPushTime(routeInstanceDo.getNextPushTime());
    routeInstance.setStatus(routeInstanceDo.getStatus());
    routeInstance.setRetryCount(routeInstanceDo.getRetryCount());
    routeInstance.setMessage(routeInstanceDo.getMessage());
    routeInstance.setListeners(routeInstanceDo.getListeners());
    routeInstance.setUnAckListeners(routeInstanceDo.getUnAckListeners());
    return routeInstance;
  }
}
