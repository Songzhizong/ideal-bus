package com.zzsong.bus.storage.mongo.converter;

import com.zzsong.bus.abs.constants.DBDefaults;
import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.abs.generator.SnowFlake;
import com.zzsong.bus.storage.mongo.document.RouteInstanceDo;
import org.apache.commons.lang3.StringUtils;

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
    routeInstanceDo.setEventId(routeInstance.getEventId());
    routeInstanceDo.setTransactionId(routeInstance.getTransactionId());
    String aggregate = routeInstance.getAggregate();
    if (StringUtils.isBlank(aggregate)) {
      aggregate = DBDefaults.STRING_VALUE;
    }
    routeInstanceDo.setAggregate(aggregate);
    String externalApplication = routeInstance.getExternalApplication();
    if (StringUtils.isNotBlank(externalApplication)) {
      routeInstanceDo.setExternalApplication(externalApplication);
    }
    routeInstanceDo.setTopic(routeInstance.getTopic());
    routeInstanceDo.setHeaders(routeInstance.getHeaders());
    routeInstanceDo.setPayload(routeInstance.getPayload());
    routeInstanceDo.setTimestamp(routeInstance.getTimestamp());
    routeInstanceDo.setShard(routeInstance.getShard());
    routeInstanceDo.setSubscriptionId(routeInstance.getSubscriptionId());
    routeInstanceDo.setApplicationId(routeInstance.getApplicationId());
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
    routeInstance.setShard(routeInstanceDo.getShard());
    routeInstance.setSubscriptionId(routeInstanceDo.getSubscriptionId());
    routeInstance.setApplicationId(routeInstanceDo.getApplicationId());
    Long nextPushTime = routeInstanceDo.getNextPushTime();
    if (nextPushTime == null) {
      nextPushTime = -1L;
    }
    routeInstance.setNextPushTime(nextPushTime);
    routeInstance.setStatus(routeInstanceDo.getStatus());
    routeInstance.setRetryCount(routeInstanceDo.getRetryCount());
    routeInstance.setMessage(routeInstanceDo.getMessage());
    routeInstance.setListeners(routeInstanceDo.getListeners());
    routeInstance.setUnAckListeners(routeInstanceDo.getUnAckListeners());
    routeInstance.setEventId(routeInstanceDo.getEventId());
    routeInstance.setTransactionId(routeInstanceDo.getTransactionId());
    routeInstance.setAggregate(routeInstanceDo.getAggregate());
    routeInstance.setExternalApplication(routeInstanceDo.getExternalApplication());
    routeInstance.setTopic(routeInstanceDo.getTopic());
    routeInstance.setHeaders(routeInstanceDo.getHeaders());
    routeInstance.setPayload(routeInstanceDo.getPayload());
    routeInstance.setTimestamp(routeInstanceDo.getTimestamp());
    return routeInstance;
  }
}
