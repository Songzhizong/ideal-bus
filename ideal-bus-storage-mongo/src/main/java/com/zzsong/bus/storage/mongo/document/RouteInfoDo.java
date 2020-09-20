package com.zzsong.bus.storage.mongo.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("ideal_bus_route_info")
public class RouteInfoDo {
  /**
   * 实例id
   */
  @Id
  @Nonnull
  private Long instanceId;
  /**
   * broker nodeId
   */
  @Indexed
  private int nodeId;
  /**
   * 事件唯一id
   */
  @Nonnull
  private String eventId;
  /**
   * 订阅关系id
   */
  @Nonnull
  private Long subscriptionId;
  /**
   * 订阅者id
   */
  @Nonnull
  private Long applicationId;
  /**
   * 主题
   */
  @Nonnull
  private String topic;
  /**
   * 是否待执行
   */
  private int wait;
  /**
   * 下次推送时间
   */
  @Indexed
  private long nextPushTime;
  /**
   * 是否执行成功
   */
  private int success;
  /**
   * 已重试次数
   */
  private int retryCount = -1;
  /**
   * 没有ack的监听器列表
   */
  private List<String> unackListeners = Collections.emptyList();
}
