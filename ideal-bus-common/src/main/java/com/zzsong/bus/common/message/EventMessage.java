package com.zzsong.bus.common.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * 事件信息
 *
 * @author 宋志宗 on 2019-06-03
 */
@SuppressWarnings("UnusedReturnValue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage<T> {

  /** 业务方唯一id, 通常为业务方的事务编号 */
  @Nonnull
  private String transactionId = "";

  /** 聚合id */
  @Nullable
  private String aggregate;

  /** 可通过该字段判断event归属哪个应用 */
  @Nullable
  private String externalApp;

  /** 事件主题, 一个事件应该只有一个主题 */
  @Nonnull
  private String topic;

  /** 事件标签, 一个事件应该只有一个标签 */
  @Nullable
  private String tag;

  /** 消息头,可用于条件匹配 */
  @Nonnull
  private EventHeaders headers = EventHeaders.create();

  /** 消息内容 */
  @Nonnull
  private T payload;

  /** 事件产生时间戳 */
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
  public static <T> EventMessage<T> of(@Nonnull String topic,
                                       @Nonnull T payload) {
    return new EventMessage<>(topic, payload);
  }

  @Nonnull
  public static <T> EventMessage<T> of(@Nonnull String topic,
                                       @Nonnull T payload,
                                       @Nonnull String aggregate) {
    return new EventMessage<T>(topic, payload).aggregate(aggregate);
  }

  @Nonnull
  public static <T> EventMessage<T> of(@Nonnull String topic,
                                       @Nonnull T payload,
                                       @Nonnull EventHeaders headers) {
    return new EventMessage<>(topic, payload, headers);
  }

  @Nonnull
  public EventMessage<T> transactionId(@Nonnull String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  @Nonnull
  public EventMessage<T> aggregate(@Nonnull String aggregate) {
    this.aggregate = aggregate;
    return this;
  }

  /**
   * 可通过该字段判断event归属哪个应用
   */
  @Nonnull
  public EventMessage<T> externalApp(@Nonnull String externalApp) {
    this.externalApp = externalApp;
    return this;
  }

  @Nonnull
  public EventMessage<T> addHeader(@Nonnull String name, @Nonnull String value) {
    this.headers.add(name, value);
    return this;
  }

  @Nonnull
  public EventMessage<T> addHeader(@Nonnull String name, @Nonnull Collection<String> values) {
    this.headers.addAll(name, values);
    return this;
  }

  @Nonnull
  public EventMessage<T> addHeader(@Nonnull String name, @Nonnull String... values) {
    this.headers.addAll(name, Arrays.asList(values));
    return this;
  }
}
