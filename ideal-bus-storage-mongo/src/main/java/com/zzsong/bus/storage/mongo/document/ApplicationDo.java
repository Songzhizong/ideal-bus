package com.zzsong.bus.storage.mongo.document;

import com.zzsong.bus.abs.constants.DBDefaults;
import com.zzsong.bus.abs.constants.ApplicationTypeEnum;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.HashIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document("ideal_bus_application")
public class ApplicationDo {
  /**
   * 订阅者id
   */
  @Id
  @NonNull
  private Long applicationId;

  /**
   * 订阅者名称
   */
  @NonNull
  @Indexed
  private String title = DBDefaults.STRING_VALUE;

  /**
   * 订阅者描述
   */
  @NonNull
  private String desc = DBDefaults.STRING_VALUE;

  @Nonnull
  private String accessToken = DBDefaults.STRING_VALUE;

  /**
   * 订阅者类型
   */
  @NonNull
  private ApplicationTypeEnum applicationType = ApplicationTypeEnum.INTERNAL;

  /**
   * 应用名称, 内部用
   */
  @NonNull
  private String appName = DBDefaults.STRING_VALUE;

  /**
   * 应用编码, 外部应用拥有此属性
   */
  @NonNull
  @HashIndexed
  private String externalApp = DBDefaults.STRING_VALUE;

  /**
   * 接收推送的地址, 外部应用拥有此属性
   */
  @NonNull
  private String receiveUrl = DBDefaults.STRING_VALUE;
}
