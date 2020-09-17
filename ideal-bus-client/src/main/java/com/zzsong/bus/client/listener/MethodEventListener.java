package com.zzsong.bus.client.listener;

import com.fasterxml.jackson.databind.JavaType;
import com.zzsong.bus.client.EventContext;
import lombok.Builder;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Builder
public class MethodEventListener implements IEventListener {

  @Nonnull
  private final Object target;
  @Nonnull
  private final Method method;
  private final boolean autoAck;
  @Nonnull
  private final String listenerName;
  @Nonnull
  private final JavaType payloadType;
  @Nonnull
  private final List<Set<String>> conditionsGroup;

  public MethodEventListener(@Nonnull Object target,
                             @Nonnull Method method,
                             boolean aotoAck,
                             @Nonnull String listenerName,
                             @Nonnull JavaType payloadType,
                             @Nonnull List<Set<String>> conditionsGroup) {
    this.target = target;
    this.method = method;
    this.autoAck = aotoAck;
    this.listenerName = listenerName;
    this.payloadType = payloadType;
    this.conditionsGroup = conditionsGroup;
  }

  @Nullable
  @Override
  public Object invoke(@Nonnull EventContext<Object> eventContext) throws Exception {
    return this.method.invoke(this.method, eventContext);
  }
}
