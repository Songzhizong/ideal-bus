package com.zzsong.bus.storage.mongo.document;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.common.message.EventHeaders;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document("ideal_bus_route_info")
public class RouteInstanceDo {
  /**
   * 实例id
   */
  @Id
  @Nonnull
  private Long instanceId;

  //  --------------------------- 事件内容相关信息 ~ ~ ~
  /** 事件唯一id */
  private long eventId;

  /** 业务方唯一id, 通常为业务方的事务编号 */
  @Nonnull
  private String transactionId;

  /** 聚合id */
  @Nonnull
  private String aggregate;

  /** 可通过该字段判断event归属哪个应用 */
  @Nullable
  private String externalApp;

  /** 主题 */
  @Nonnull
  private String topic;

  /** 事件标签, 一个事件应该只有一个标签 */
  @Nonnull
  private String tag;

  /** 消息头,可用于条件匹配 */
  @Nonnull
  private EventHeaders headers;

  /** 消息内容 */
  @Nonnull
  private Object payload;

  /** 事件产生时间戳 */
  private long timestamp;


  //  --------------------------- 路由相关信息 ~ ~ ~
  /** broker nodeId */
  @Indexed(background = true)
  private long shard;

  /** 订阅关系id */
  @Nonnull
  @Indexed(background = true)
  private Long subscriptionId;

  /** 订阅者id */
  @Nonnull
  private Long applicationId;

  /** 是否广播 */
  private boolean broadcast = false;

  /** 下次推送时间 */
  @Nullable
  @Indexed(direction = IndexDirection.ASCENDING, background = true, sparse = true)
  private Long nextPushTime;

  /** 状态 */
  @Indexed(background = true)
  private int status = RouteInstance.STATUS_WAITING;

  /** 已重试次数 */
  private int retryCount = -1;

  /** 最大重试次数 */
  private int retryLimit = -1;

  /** 描述信息 */
  @Nonnull
  private String message;

  /** 消费该事件的监听器列表 */
  @Nonnull
  private List<String> listeners = Collections.emptyList();

  /** 没有ack的监听器列表 */
  @Nonnull
  private List<String> unAckListeners = Collections.emptyList();
}
