package com.zzsong.bus.storage.mongo.document;

import com.zzsong.bus.common.message.EventHeaders;
import lombok.*;
import org.springframework.data.annotation.Id;
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
public class EventInstanceDo {
  /** 事件唯一id */
  @Id
  @Nonnull
  private Long eventId;

  /** 业务方唯一id */
  @Nonnull
  private String transactionId;

  /** 聚合id */
  @Nullable
  @Indexed(background = true)
  private String aggregate;

  /** 可通过该字段判断event归属哪个外部应用 */
  @Nullable
  private String externalApplication;

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
