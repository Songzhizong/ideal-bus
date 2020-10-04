package com.zzsong.bus.storage.mongo.document;

import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.abs.constants.DBDefaults;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document("ideal_bus_subscription")
@CompoundIndexes({
    @CompoundIndex(name = "sub_unique",
        def = "{'applicationId' : 1, 'topic': 1, 'listenerName': 1}", unique = true)
})
public class SubscriptionDo {
  @Id
  private Long subscriptionId;
  /**
   * 订阅者id
   */
  private long applicationId;
  /**
   * 事件主题, 也是事件的唯一id
   */
  @NonNull
  private String topic;
  /**
   * 监听器名称
   */
  @Nonnull
  private String listenerName;
  /**
   * 延迟表达式
   */
  @Nonnull
  private String delayExp = DBDefaults.STRING_VALUE;
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
  /**
   * 消费模式
   */
  private int consumeType;
  /**
   * 订阅状态
   * see {@link Subscription#STATUS_DISABLED} & {@link Subscription#STATUS_ENABLED}
   */
  private int status;
}
