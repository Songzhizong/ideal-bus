package com.zzsong.bus.base.mongo.document;

import com.zzsong.bus.common.constant.DBDefaults;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Data
@Document("ideal_bus_subscription")
public class SubscriptionMongoDo {
  @Id
  private Long subscriptionId;
  /**
   * 订阅者id
   */
  private long subscriberId;
  /**
   * 事件主题, 也是事件的唯一id
   */
  @NonNull
  private String topic;
  /**
   * 订阅条件表达式
   */
  @NonNull
  private String condition = DBDefaults.STRING_VALUE;
  /**
   * 是否广播
   */
  private boolean broadcast = false;
  /**
   * 失败重试次数
   */
  private int retryCount = 0;

}
