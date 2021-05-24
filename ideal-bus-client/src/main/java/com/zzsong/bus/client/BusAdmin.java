package com.zzsong.bus.client;

import com.zzsong.bus.common.transfer.ResubscribeArgs;

import javax.annotation.Nonnull;

/**
 * 管理接口
 *
 * @author 宋志宗 on 2021/5/24
 */
public interface BusAdmin {

  /**
   * 刷新订阅关系
   *
   * @param args 输入参数
   * @author 宋志宗 on 2021/5/24
   */
  void resubscribe(@Nonnull ResubscribeArgs args);
}
