package com.zzsong.bus.abs.converter;

import com.zzsong.bus.abs.domain.Application;
import com.zzsong.bus.abs.transfer.CreateApplicationArgs;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@SuppressWarnings("DuplicatedCode")
public final class ApplicationConverter {

  @Nonnull
  public static Application fromCreateArgs(@Nonnull CreateApplicationArgs args) {
    Application application = new Application();
//      application.setApplicationId();
    application.setTitle(args.getTitle());
    application.setDesc(args.getDesc());
    application.setAccessToken(args.getAccessToken());
    application.setApplicationType(args.getApplicationType());
    application.setAppName(args.getAppName());
    application.setExternalApp(args.getExternalApp());
    application.setReceiveUrl(args.getReceiveUrl());
    return application;
  }
}
