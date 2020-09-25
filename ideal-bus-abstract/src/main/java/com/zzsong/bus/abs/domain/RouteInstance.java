package com.zzsong.bus.abs.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.index.Indexed;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * 推送实例
 *
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
public class RouteInstance {
  /**
   * 执行失败
   */
  public static final int FAILURE = 0;
  /**
   * 执行成功
   */
  public static final int SUCCESS = 1;
  /**
   * 等待执行状态
   */
  public static final int STATUS_WAITING = 0;
  /**
   * 非等待执行状态
   */
  public static final int STATUS_DISCARD = 1;
  /**
   * 实例id
   */
  @Nonnull
  private Long instanceId;
  /**
   * broker nodeId
   */
  private int nodeId;
  /**
   * 事件唯一id
   */
  @Nonnull
  private String eventId;
  /**
   * hash key
   */
  @Nullable
  private String key;
  /**
   * 订阅关系id
   */
  @Indexed
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
  private long nextPushTime = -1;
  /**
   * 是否执行成功
   */
  private int success = FAILURE;
  /**
   * 状态: 0 丢弃, 1 等待执行
   */
  private int status = STATUS_WAITING;
  /**
   * 已重试次数
   */
  private int retryCount = -1;
  /**
   * 没有ack的监听器列表
   */
  private List<String> unAckListeners = Collections.emptyList();
}
