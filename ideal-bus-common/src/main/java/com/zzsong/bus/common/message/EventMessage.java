package com.zzsong.bus.common.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Collection;

/**
 * 事件消息体
 *
 * @author 宋志宗 on 2019-06-03
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage<T> {
  /**
   * 业务方唯一id
   */
  @Nullable
  private String bizId;
  /**
   * 可通过该字段判断event归属哪个应用
   */
  @Nonnull
  private String application = "";
  /**
   * 事件主题
   */
  @Nonnull
  private String topic;
  /**
   * 消息头,可用于条件匹配
   */
  @Nonnull
  private EventHeaders headers = EventHeaders.create();
  /**
   * 延迟时间,默认不延迟
   */
  @Nullable
  private Duration delay;
  /**
   * 消息内容
   */
  @Nonnull
  private T payload;
  /**
   * 事件产生时间戳
   */
  private long timestamp = System.currentTimeMillis();


  private EventMessage(@Nonnull String topic,
                       @Nonnull T payload) {
    this.topic = topic;
    this.payload = payload;
  }

  private EventMessage(@Nonnull String topic,
                       @Nonnull T payload,
                       @Nonnull EventHeaders headers) {
    this.topic = topic;
    this.payload = payload;
    this.headers = headers;
  }


  @Nonnull
  public static <T> EventMessage<T> create(@Nonnull String topic,
                                           @Nonnull T payload) {
    return new EventMessage<>(topic, payload);
  }

  @Nonnull
  public static <T> EventMessage<T> create(@Nonnull String topic,
                                           @Nonnull T payload,
                                           @Nonnull EventHeaders headers) {
    return new EventMessage<>(topic, payload, headers);
  }

  public EventMessage<T> bizId(@Nonnull String bizId) {
    this.bizId = bizId;
    return this;
  }

  public EventMessage<T> application(@Nonnull String application) {
    this.application = application;
    return this;
  }

  public EventMessage<T> delay(@Nonnull Duration delay) {
    this.delay = delay;
    return this;
  }

  public EventMessage<T> addHeader(@Nonnull String key, @Nonnull String value) {
    this.headers.add(key, value);
    return this;
  }

  public EventMessage<T> addHeader(@Nonnull String key, @Nonnull Collection<String> values) {
    this.headers.addAll(key, values);
    return this;
  }

  public EventMessage<T> setHeader(@Nonnull String key, @Nonnull String value) {
    this.headers.set(key, value);
    return this;
  }
}