package com.zzsong.bus.client;

import com.zzsong.bus.common.message.EventMessage;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * event批量生成
 * <p>提供链式创建event的方法</p>
 *
 * @author 宋志宗 on 2020/9/17
 */
public final class EventGenerator {
  private transient int cursor = -1;
  private final List<EventMessage<?>> messages = new ArrayList<>();

  @Nonnull
  public static EventGenerator create() {
    return new EventGenerator();
  }

  @Nonnull
  public static EventGenerator of(@Nonnull String topic, @Nonnull Object payload) {
    final EventGenerator eventGenerator = new EventGenerator();
    final EventMessage<Object> eventMessage = new EventMessage<>();
    eventMessage.setTopic(topic);
    eventMessage.setPayload(payload);
    eventGenerator.messages.add(eventMessage);
    eventGenerator.cursor = 0;
    return eventGenerator;
  }

  private EventGenerator() {
  }

  /**
   * 追加事件消息
   */
  @Nonnull
  public EventGenerator then(@Nonnull String topic, @Nonnull Object payload) {
    final EventMessage<Object> eventMessage = new EventMessage<>();
    eventMessage.setTopic(topic);
    eventMessage.setPayload(payload);
    return then(eventMessage);
  }

  /**
   * 追加事件消息
   */
  @Nonnull
  public EventGenerator then(@Nonnull EventMessage<?> eventMessage) {
    messages.add(eventMessage);
    cursor++;
    return this;
  }

  /**
   * 为指定的headerName添加一个value
   *
   * @param name  头名称
   * @param value 值
   * @return EventGenerator
   */
  @Nonnull
  public EventGenerator addHeader(@Nonnull String name, @Nonnull String value) {
    EventMessage<?> message = messages.get(cursor);
    message.addHeader(name, value);
    return this;
  }

  /**
   * 为指定的headerName添加一组value
   *
   * @param name   头名称
   * @param values 值列表
   * @return EventGenerator
   */
  @Nonnull
  public EventGenerator addHeader(@Nonnull String name, @Nonnull Collection<String> values) {
    EventMessage<?> message = messages.get(cursor);
    message.addHeader(name, values);
    return this;
  }

  /**
   * 为指定的headerName添加一组value
   *
   * @param name   头名称
   * @param values 值列表
   * @return EventGenerator
   */
  @Nonnull
  public EventGenerator addHeader(@Nonnull String name, @Nonnull String... values) {
    EventMessage<?> message = messages.get(cursor);
    message.addHeader(name, values);
    return this;
  }

  /**
   * 在现有的延迟时间基础上增加一定的秒数
   *
   * @param delaySeconds 增加秒数
   * @return EventGenerator
   */
  @Nonnull
  public EventGenerator delaySeconds(int delaySeconds) {
    EventMessage<?> message = messages.get(cursor);
    message.delaySeconds(delaySeconds);
    return this;
  }

  /**
   * 在现有的延迟时间基础上增加一定的分钟数
   *
   * @param delayMinutes 增加分钟数
   * @return EventGenerator
   */
  @Nonnull
  public EventGenerator delayMinutes(int delayMinutes) {
    EventMessage<?> message = messages.get(cursor);
    message.delayMinutes(delayMinutes);
    return this;
  }

  /**
   * 在现有的延迟时间基础上增加一定的小时数
   *
   * @param delayHours 增加小时数
   * @return EventGenerator
   */
  @Nonnull
  public EventGenerator delayHours(int delayHours) {
    EventMessage<?> message = messages.get(cursor);
    message.delayHours(delayHours);
    return this;
  }

  /**
   * 在现有的延迟时间基础上增加一定的天数
   *
   * @param delayDays 增加天数
   * @return EventGenerator
   */
  @Nonnull
  public EventGenerator delayDays(int delayDays) {
    EventMessage<?> message = messages.get(cursor);
    message.delayDays(delayDays);
    return this;
  }

  @Nonnull
  public EventGenerator bizId(@Nonnull String bizId) {
    EventMessage<?> message = messages.get(cursor);
    message.bizId(bizId);
    return this;
  }

  /**
   * 可通过该字段判断event归属哪个外部应用
   */
  @Nonnull
  public EventGenerator externalApp(@Nonnull String externalApp) {
    EventMessage<?> message = messages.get(cursor);
    message.externalApp(externalApp);
    return this;
  }

  @Nonnull
  public List<EventMessage<?>> get() {
    return messages;
  }
}
