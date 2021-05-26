package com.zzsong.bus.common.share.lb;


import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/8/19
 */
public interface LbServer {

  /**
   * @return 唯一ID
   */
  @Nonnull
  String getInstanceId();

  /**
   * 获取权重
   * <p>如果需要使用加权策略, 请实现该方法</p>
   *
   * @return 权重, 用于加权算法, 至少为1
   */
  default int getWeight() {
    final String className = this.getClass().getName();
    throw new NotImplementedException(className + " not implemented getWeight");
  }
}
