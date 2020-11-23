package com.zzsong.bus.abs.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 失败历史
 * <p>记录推送失败的历史记录</p>
 *
 * @author 宋志宗 on 2020/11/23
 */
@Getter
@Setter
public class FailureHistory {

  /** 路由实例id */
  private long routeInstanceId;

  /**
   * 最终成功
   * <pre>
   *   可能多次推送失败后最终推送成功了, 该字段用于区分这种情况
   * </pre>
   */
  private boolean ultimateSuccess;
}
