package com.zzsong.bus.storage.mongo.converter;

import com.zzsong.bus.abs.domain.RouteInfo;
import com.zzsong.bus.storage.mongo.document.RouteInfoDo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/19 7:28 下午
 */
public final class RouteInfoDoConverter {

  @Nonnull
  public static RouteInfoDo fromRouteInfo(@Nonnull RouteInfo routeInfo) {
    RouteInfoDo routeInfoDo = new RouteInfoDo();
    Long instanceId = routeInfo.getInstanceId();
    routeInfoDo.setInstanceId(instanceId);
    routeInfoDo.setEventId(routeInfo.getEventId());
    routeInfoDo.setSubscriptionId(routeInfo.getSubscriptionId());
    routeInfoDo.setApplicationId(routeInfo.getApplicationId());
    routeInfoDo.setTopic(routeInfo.getTopic());
    routeInfoDo.setWait(routeInfo.getWait());
    routeInfoDo.setNextPushTime(routeInfo.getNextPushTime());
    routeInfoDo.setSuccess(routeInfo.getSuccess());
    routeInfoDo.setRetryCount(routeInfo.getRetryCount());
    return routeInfoDo;
  }

  @Nonnull
  public static RouteInfo toRouteInfo(@Nonnull RouteInfoDo routeInfoDo) {
    RouteInfo routeInfo = new RouteInfo();
    routeInfo.setInstanceId(routeInfoDo.getInstanceId());
    routeInfo.setEventId(routeInfoDo.getEventId());
    routeInfo.setSubscriptionId(routeInfoDo.getSubscriptionId());
    routeInfo.setApplicationId(routeInfoDo.getApplicationId());
    routeInfo.setTopic(routeInfoDo.getTopic());
    routeInfo.setNextPushTime(routeInfoDo.getNextPushTime());
    routeInfo.setSuccess(routeInfoDo.getSuccess());
    routeInfo.setWait(routeInfoDo.getWait());
    routeInfo.setRetryCount(routeInfoDo.getRetryCount());
    return routeInfo;
  }
}
