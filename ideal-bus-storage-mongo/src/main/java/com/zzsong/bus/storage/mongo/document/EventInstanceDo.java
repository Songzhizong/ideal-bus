package com.zzsong.bus.storage.mongo.document;

import com.zzsong.bus.common.message.EventHeaders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Data
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
  private String externalId;
  /**
   * 事件主题
   */
  @Nonnull
  @Indexed(name = "eventInst_topic")
  private String topic;
  /**
   * 消息头,可用于条件匹配
   */
  @Nonnull
  private EventHeaders headers;

  /**
   * 延迟时间,默认不延迟
   */
  @Nullable
  private Duration delay;
  /**
   * 消息内容
   */
  @Nonnull
  private Object payload;
  /**
   * 事件产生时间戳
   */
  private long timestamp;
}
