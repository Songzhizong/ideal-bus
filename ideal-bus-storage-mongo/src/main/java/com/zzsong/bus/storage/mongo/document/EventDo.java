package com.zzsong.bus.storage.mongo.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document("ideal_bus_event")
public class EventDo {

  @Id
  private long eventId;
  /**
   * 主题, 也是事件的唯一id
   */
  @NonNull
  @Indexed(unique = true)
  private String topic;

  /**
   * 事件名称
   */
  @NonNull
  private String eventName;

  /**
   * 描述
   */
  @NonNull
  private String desc;

  /**
   * 事件实例持久化存储的过期时间 单位 ms, 大于1天生效
   */
  private long expire;
}
