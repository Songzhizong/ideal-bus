package com.zzsong.bus.receiver.listener;

import com.fasterxml.jackson.databind.JavaType;
import com.zzsong.bus.receiver.deliver.EventContext;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * @author 宋志宗 on 2020/9/17
 */
public class MethodEventListener implements IEventListener {

  @Getter
  private final boolean autoAck;
  @Nonnull
  private final Object target;
  @Nonnull
  private final Method method;
  @Getter
  @Nonnull
  private final String listenerName;
  @Getter
  @Nonnull
  private final JavaType payloadType;
  @Getter
  @Nonnull
  private final List<Set<String>> conditionsGroup;

  public MethodEventListener(boolean autoAck,
                             @Nonnull Object target,
                             @Nonnull Method method,
                             @Nonnull JavaType payloadType,
                             @Nonnull List<Set<String>> conditionsGroup) {
    this.autoAck = autoAck;
    this.target = target;
    this.method = method;
    this.payloadType = payloadType;
    this.conditionsGroup = conditionsGroup;
    String className = target.getClass().getName();
    String methodName = method.getName();
    this.listenerName = className + "#" + methodName;
  }

  @Nullable
  @Override
  public Object invoke(@Nonnull EventContext<Object> eventContext) throws Exception {
    return this.method.invoke(this.target, eventContext);
  }
}
