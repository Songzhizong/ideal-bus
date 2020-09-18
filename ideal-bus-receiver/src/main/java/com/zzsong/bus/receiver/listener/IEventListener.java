package com.zzsong.bus.receiver.listener;

import com.fasterxml.jackson.databind.JavaType;
import com.zzsong.bus.receiver.deliver.EventContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * @author 宋志宗 on 2020/9/17
 */
public interface IEventListener {
  /**
   * @return 监听器名称
   */
  @Nonnull
  String getListenerName();

  @Nonnull
  JavaType getPayloadType();

  /**
   * @return 监听条件
   */
  @Nonnull
  List<Set<String>> getConditionsGroup();

  boolean isAutoAck();

  @SuppressWarnings("UnusedReturnValue")
  @Nullable
  Object invoke(@Nonnull EventContext<Object> eventContext) throws Exception;
}
