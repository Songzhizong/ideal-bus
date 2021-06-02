package com.zzsong.bus.storage.mongo.document;

import com.zzsong.bus.abs.domain.RouteInstance;
import com.zzsong.bus.common.message.EventHeaders;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document("ideal_bus_route_info")
@CompoundIndexes({
    @CompoundIndex(name = "ck_entity_aggregate",
        def = "{'entity' : 1, 'aggregate': 1}",
        background = true, sparse = true)
})
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

  private String uuid;

  /** 业务方唯一id, 通常为业务方的事务编号 */
  @Nullable
  private String transactionId;

  /** 实体类型 */
  @Nullable
  private String entity;

  /** 聚合id */
  @Nullable
  private String aggregate;

  /** 可通过该字段判断event归属哪个应用 */
  @Nullable
  private String externalApp;

  /** 主题 */
  @Nonnull
  private String topic;

  /** 事件标签, 一个事件应该只有一个标签 */
  @Nullable
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
  @Indexed(background = true)
  private long nextPushTime = -1;

  /** 状态 */
  @Indexed(background = true)
  private int status = RouteInstance.STATUS_QUEUING;

  @Indexed(background = true, sparse = true)
  private long statusTime;

  /** 已重试次数 */
  private int retryCount = -1;

  /** 最大重试次数 */
  private int retryLimit = -1;

  /** 描述信息 */
  @Nonnull
  private String message;

  /** 消费该事件的监听器 */
  @Nonnull
  private String listener;
}
