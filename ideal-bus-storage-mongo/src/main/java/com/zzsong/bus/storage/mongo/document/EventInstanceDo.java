package com.zzsong.bus.storage.mongo.document;

import com.zzsong.bus.common.message.EventHeaders;
import lombok.*;
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
@Document("ideal_bus_event_inst")
@CompoundIndexes({
    @CompoundIndex(name = "ck_entity_aggregate",
        def = "{'entity' : 1, 'aggregate': 1}",
        background = true, sparse = true)
})
public class EventInstanceDo {
  /** 事件唯一id */
  @Id
  @Nonnull
  private Long eventId;

  /** 业务方唯一id */
  @Nullable
  @Indexed(background = true, sparse = true)
  private String transactionId;

  /** 实体类型 */
  @Nullable
  private String entity;

  /** 聚合id */
  @Nullable
  private String aggregate;

  /** 可通过该字段判断event归属哪个外部应用 */
  @Nullable
  private String externalApp;

  /** 事件主题 */
  @Nonnull
  @Indexed(background = true)
  private String topic;

  /** 事件标签, 一个事件应该只有一个标签 */
  @Nonnull
  @Indexed(background = true)
  private String tag;

  /** 消息头,可用于条件匹配 */
  @Nonnull
  private EventHeaders headers;

  /** 消息内容 */
  @Nonnull
  private Object payload;

  /** 事件产生时间戳 */
  private long timestamp;
}
