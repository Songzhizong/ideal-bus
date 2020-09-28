package com.zzsong.bus.storage.mongo.document;

import com.zzsong.bus.abs.domain.RouteInstance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.HashIndexed;
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
public class RouteInstanceDo {
  /**
   * 实例id
   */
  @Id
  @Nonnull
  private Long instanceId;
  /**
   * broker nodeId
   */
  @HashIndexed
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
   * 下次推送时间
   */
  @Indexed
  private long nextPushTime = -1;
  /**
   * 状态: 0 丢弃, 1 等待执行
   */
  @HashIndexed
  private int status = RouteInstance.STATUS_WAITING;
  /**
   * 已重试次数
   */
  private int retryCount = -1;
  /**
   * 描述信息
   */
  private String message;
  /**
   * 没有ack的监听器列表
   */
  private List<String> unAckListeners = Collections.emptyList();
}
