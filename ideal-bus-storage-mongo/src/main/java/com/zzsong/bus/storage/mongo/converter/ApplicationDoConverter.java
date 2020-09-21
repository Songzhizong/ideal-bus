package com.zzsong.bus.storage.mongo.converter;

import com.zzsong.bus.abs.domain.Application;
import com.zzsong.bus.storage.mongo.document.ApplicationDo;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@SuppressWarnings("DuplicatedCode")
public final class ApplicationDoConverter {

  @Nonnull
  public static ApplicationDo fromApp(@Nonnull Application application) {
    ApplicationDo applicationDo = new ApplicationDo();
    applicationDo.setApplicationId(application.getApplicationId());
    applicationDo.setTitle(application.getTitle());
    applicationDo.setDesc(application.getDesc());
    applicationDo.setAccessToken(application.getAccessToken());
    applicationDo.setApplicationType(application.getApplicationType());
    applicationDo.setAppName(application.getAppName());
    applicationDo.setExternalId(application.getExternalId());
    applicationDo.setReceiveUrl(application.getReceiveUrl());
    return applicationDo;
  }

  public static Application toApp(@Nonnull ApplicationDo applicationDo) {
    Application application = new Application();
    application.setApplicationId(applicationDo.getApplicationId());
    application.setTitle(applicationDo.getTitle());
    application.setDesc(applicationDo.getDesc());
    application.setAccessToken(applicationDo.getAccessToken());
    application.setApplicationType(applicationDo.getApplicationType());
    application.setAppName(applicationDo.getAppName());
    application.setExternalId(applicationDo.getExternalId());
    application.setReceiveUrl(applicationDo.getReceiveUrl());
    return application;
  }
}