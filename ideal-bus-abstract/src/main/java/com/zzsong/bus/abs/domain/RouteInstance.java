package com.zzsong.bus.abs.domain;

import com.zzsong.bus.abs.constants.DBDefaults;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 推送实例
 *
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
public class RouteInstance extends EventInstance {
  /** 丢弃 */
  public static final int STATUS_DISCARD = -1;
  /** 正在队列中等待消费 */
  public static final int STATUS_QUEUING = 0;
  /** 消费端正在处理 */
  public static final int STATUS_RUNNING = 1;
  /** 消费端处理成功 */
  public static final int STATUS_SUCCESS = 2;
  /** 消费端处理失败 */
  public static final int STATUS_FAILURE = 3;
  /** 存储库中暂存, 等待加入队列 */
  public static final int STATUS_TEMPING = 4;
  /** 延迟中, 待延迟结束转为暂存状态 */
  public static final int STATUS_DELAYING = 5;

  /** 实例id */
  @Nonnull
  private Long instanceId;

  /** broker nodeId */
  private long shard;

  /** 订阅关系id */
  @Nonnull
  private Long subscriptionId;

  /** 订阅者id */
  @Nonnull
  private Long applicationId;

  /** 是否广播 */
  private boolean broadcast = false;

  /** 下次推送时间 */
  private long nextPushTime = -1;

  /** 状态: -1 丢弃, 0 等待执行, 1 执行中, 2 完成, 3 失败 */
  private int status = STATUS_QUEUING;

  private long statusTime;

  /** 已重试次数 */
  private int retryCount = -1;

  /** 最大重试次数 */
  private int retryLimit = -1;

  /** 描述信息 */
  private String message = DBDefaults.STRING_VALUE;

  /** 消费该事件的监听器名称列表 */
  @Nullable
  private String listener = "";
}
