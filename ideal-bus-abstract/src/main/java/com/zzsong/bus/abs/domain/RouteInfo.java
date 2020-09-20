package com.zzsong.bus.abs.domain;

import lombok.Getter;
import lombok.Setter;

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
public class RouteInfo {
  /**
   * 执行失败
   */
  public static final int STATUS_FAILURE = 0;
  /**
   * 执行成功
   */
  public static final int STATUS_SUCCESS = 1;
  /**
   * 非等待执行状态
   */
  public static final int UN_WAITING = 0;
  /**
   * 等待执行状态
   */
  public static final int WAITING = 1;

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

  @Nullable
  private String key;
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
  private long nextPushTime = -1;
  /**
   * 是否执行成功
   */
  private int success = STATUS_FAILURE;
  /**
   * 是否处于等待状态, 延迟消息和失败消息会等待一段时间之后执行推送
   */
  private int wait = UN_WAITING;
  /**
   * 已重试次数
   */
  private int retryCount = -1;
  /**
   * 没有ack的监听器列表
   */
  private List<String> unackListeners = Collections.emptyList();
}
