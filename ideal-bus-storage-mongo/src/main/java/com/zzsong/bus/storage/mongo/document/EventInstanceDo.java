package com.zzsong.bus.storage.mongo.document;

import com.zzsong.bus.common.message.EventHeaders;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document("ideal_bus_event_inst")
public class EventInstanceDo {
  /**
   * 事件唯一id
   */
  @Id
  @Nonnull
  private String eventId;
  /**
   * 业务方唯一id
   */
  @Nonnull
  private String bizId;
  /**
   * 可通过该字段判断event归属哪个外部应用
   */
  @Nonnull
  private String externalApp;
  /**
   * 事件主题
   */
  @Nonnull
  @Indexed
  private String topic;
  /**
   * 消息头,可用于条件匹配
   */
  @Nonnull
  private EventHeaders headers;
  /**
   * 消息内容
   */
  @Nonnull
  private Object payload;
  /**
   * 事件产生时间戳
   */
  private long timestamp;

  /**
   * 状态
   */
  private int status = 0;
}
