package com.zzsong.bus.client.listener;

import com.fasterxml.jackson.databind.JavaType;
import com.zzsong.bus.common.util.ConditionMatcher;
import com.zzsong.bus.client.deliver.EventContext;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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
  @Getter
  @Nonnull
  private final String delayExp;
  @Nonnull
  private final Object target;
  @Nonnull
  private final Method method;
  @Getter
  @Nonnull
  private final String condition;
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
                             @Nonnull String delayExp,
                             @Nonnull Object target,
                             @Nonnull Method method,
                             @Nonnull String condition,
                             @Nonnull String listenerName,
                             @Nonnull JavaType payloadType) {
    this.autoAck = autoAck;
    this.delayExp = delayExp;
    this.target = target;
    this.method = method;
    this.condition = condition;
    if (StringUtils.isNotBlank(listenerName)) {
      this.listenerName = listenerName;
    } else {
      String className = target.getClass().getName();
      String methodName = method.getName();
      this.listenerName = className + "#" + methodName;
    }
    this.payloadType = payloadType;
    this.conditionsGroup = ConditionMatcher.parseConditionString(condition);
  }

  @Nullable
  @Override
  public Object invoke(@Nonnull EventContext<Object> eventContext) throws Exception {
    return this.method.invoke(this.target, eventContext);
  }
}
