package com.zzsong.bus.abs.generator;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/10/10 4:21 下午
 */
public interface IDGeneratorFactory {
  
  IDGenerator getGenerator(@Nonnull String biz);
}
