package com.zzsong.bus.common.generator;

import javax.annotation.Nonnull;

/**
 * Created by 宋志宗 on 2020/9/12
 */
public interface IDGeneratorFactory {
  
  IDGenerator getGenerator(@Nonnull String biz);
}
