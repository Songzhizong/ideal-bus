package com.zzsong.bus.storage.mongo.document;

import com.zzsong.bus.abs.constants.DBDefaults;
import com.zzsong.bus.abs.constants.EventTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.HashIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author 宋志宗 on 2020/9/16
 */
@Data
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
   * 归属模块
   */
  @HashIndexed
  private long moduleId = DBDefaults.LONG_VALUE;

  /**
   * 事件类型
   */
  @NonNull
  private EventTypeEnum eventType = EventTypeEnum.UNKNOWN;

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
}
