package com.zzsong.bus.client.listener;

import com.fasterxml.jackson.databind.JavaType;
import com.zzsong.bus.client.deliver.EventContext;

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

  @Nonnull
  String getCondition();

  /**
   * @return 监听条件
   */
  @Nonnull
  List<Set<String>> getConditionsGroup();

  boolean isAutoAck();

  /**
   * @return 延迟消费表达式
   */
  @Nullable
  String getDelayExp();

  @SuppressWarnings("UnusedReturnValue")
  @Nullable
  Object invoke(@Nonnull EventContext<Object> eventContext) throws Exception;
}
