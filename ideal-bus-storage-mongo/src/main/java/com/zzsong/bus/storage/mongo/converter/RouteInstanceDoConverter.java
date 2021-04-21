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
    String transactionId = routeInstance.getTransactionId();
    if (StringUtils.isBlank(transactionId)) {
      routeInstanceDo.setTransactionId(null);
    } else {
      routeInstanceDo.setTransactionId(transactionId);
    }
    routeInstanceDo.setEntity(routeInstance.getEntity());
    routeInstanceDo.setAggregate(routeInstance.getAggregate());

    String externalApp = routeInstance.getExternalApp();
    if (StringUtils.isNotBlank(externalApp)) {
      routeInstanceDo.setExternalApp(externalApp);
    }
    routeInstanceDo.setTopic(routeInstance.getTopic());
    routeInstanceDo.setHeaders(routeInstance.getHeaders());
    routeInstanceDo.setPayload(routeInstance.getPayload());
    routeInstanceDo.setTimestamp(routeInstance.getTimestamp());
    routeInstanceDo.setShard(routeInstance.getShard());
    routeInstanceDo.setSubscriptionId(routeInstance.getSubscriptionId());
    routeInstanceDo.setApplicationId(routeInstance.getApplicationId());
    routeInstanceDo.setBroadcast(routeInstance.isBroadcast());
    long nextPushTime = routeInstance.getNextPushTime();
    if (nextPushTime > SnowFlake.START_TIMESTAMP) {
      routeInstanceDo.setNextPushTime(nextPushTime);
    }
    routeInstanceDo.setStatus(routeInstance.getStatus());
    routeInstanceDo.setRetryCount(routeInstance.getRetryCount());
    routeInstanceDo.setRetryLimit(routeInstance.getRetryLimit());
    routeInstanceDo.setMessage(routeInstance.getMessage());
    routeInstanceDo.setListener(routeInstance.getListener());
    return routeInstanceDo;
  }

  @Nonnull
  public static RouteInstance toRouteInstance(@Nonnull RouteInstanceDo routeInstanceDo) {
    RouteInstance routeInstance = new RouteInstance();
    routeInstance.setInstanceId(routeInstanceDo.getInstanceId());
    routeInstance.setShard(routeInstanceDo.getShard());
    routeInstance.setSubscriptionId(routeInstanceDo.getSubscriptionId());
    routeInstance.setApplicationId(routeInstanceDo.getApplicationId());
    routeInstance.setBroadcast(routeInstanceDo.isBroadcast());
    long nextPushTime = routeInstanceDo.getNextPushTime();
    routeInstance.setNextPushTime(nextPushTime);
    routeInstance.setStatus(routeInstanceDo.getStatus());
    routeInstance.setRetryCount(routeInstanceDo.getRetryCount());
    routeInstance.setRetryLimit(routeInstanceDo.getRetryLimit());
    routeInstance.setMessage(routeInstanceDo.getMessage());
    routeInstance.setListener(routeInstanceDo.getListener());
    routeInstance.setEventId(routeInstanceDo.getEventId());
    routeInstance.setTransactionId(routeInstanceDo.getTransactionId());
    routeInstance.setEntity(routeInstanceDo.getEntity());
    routeInstance.setAggregate(routeInstanceDo.getAggregate());
    routeInstance.setExternalApp(routeInstanceDo.getExternalApp());
    routeInstance.setTopic(routeInstanceDo.getTopic());
    routeInstance.setHeaders(routeInstanceDo.getHeaders());
    routeInstance.setPayload(routeInstanceDo.getPayload());
    routeInstance.setTimestamp(routeInstanceDo.getTimestamp());
    return routeInstance;
  }
}
