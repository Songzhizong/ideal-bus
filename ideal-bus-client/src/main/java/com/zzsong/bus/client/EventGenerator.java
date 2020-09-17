package com.zzsong.bus.client;

import com.zzsong.bus.common.message.EventMessage;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * event批量生成
 * <p>提供链式创建event的方法</p>
 *
 * @author 宋志宗 on 2020/9/17
 */
public class EventGenerator {
  private int cursor = -1;
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
   * 设置消息的延迟发布事件,单位: 毫秒
   */
  @Nonnull
  public EventGenerator delay(@Nonnull Duration delay) {
    EventMessage<?> message = messages.get(cursor);
    message.setDelay(delay);
    return this;
  }

  @Nonnull
  public EventGenerator bizId(@Nonnull String bizId) {
    EventMessage<?> message = messages.get(cursor);
    message.bizId(bizId);
    return this;
  }

  @Nonnull
  public EventGenerator application(@Nonnull String application) {
    EventMessage<?> message = messages.get(cursor);
    message.application(application);
    return this;
  }

  @Nonnull
  public List<EventMessage<?>> get() {
    return messages;
  }

}
