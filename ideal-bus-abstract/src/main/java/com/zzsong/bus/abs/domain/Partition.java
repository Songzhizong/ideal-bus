package com.zzsong.bus.abs.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 分区信息
 *
 * @author 宋志宗 on 2020/11/20
 */
@Getter
@Setter
public class Partition {

  /** broker节点id */
  private int nodeId;

  /** 分区编号, 0 ~ n */
  private int partition;

  /** 分区状态 */
  private Status status;

  public enum Status {
    /**
     * 可用
     * <pre>
     *   新的消息可投入此分区
     *   此分区的消息等待投递给消费端
     * </pre>
     */
    AVAILABLE,

    /**
     * 弃用中
     * <pre>
     *   分区已被标记为启用
     *   可能分区内还有待处理的消息需要继续投递
     *   新的消息不会投入到备标记为弃用的分区
     * </pre>
     */
    DEPRECATING,

    /**
     * 已弃用, 非活动分区
     */
    DEPRECATED,
    ;
  }
}
