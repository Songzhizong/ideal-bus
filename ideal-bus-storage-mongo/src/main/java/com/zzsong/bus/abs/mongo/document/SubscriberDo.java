package com.zzsong.bus.abs.mongo.document;

import com.zzsong.bus.abs.constants.DBDefaults;
import com.zzsong.bus.abs.constants.SubscriberTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("ideal_bus_subscriber")
public class SubscriberDo {
  /**
   * 订阅者id
   */
  @Id
  @NonNull
  private Long subscriberId;

  /**
   * 订阅者名称
   */
  @Indexed
  @NonNull
  private String title = DBDefaults.STRING_VALUE;

  /**
   * 订阅者描述
   */
  @NonNull
  private String desc = DBDefaults.STRING_VALUE;

  /**
   * 订阅者类型
   */
  @NonNull
  private SubscriberTypeEnum subscriberType = SubscriberTypeEnum.INTERNAL;

  /**
   * 应用名称, 内部用
   */
  @NonNull
  private String appName = DBDefaults.STRING_VALUE;

  /**
   * 应用编码, 外部应用拥有此属性
   */
  @Indexed
  @NonNull
  private String application = DBDefaults.STRING_VALUE;

  /**
   * 接收推送的地址, 外部应用拥有此属性
   */
  @NonNull
  private String receiveUrl = DBDefaults.STRING_VALUE;
}
