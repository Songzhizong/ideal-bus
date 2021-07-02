package com.zzsong.bus.ideal.boot.starter.event;

import cn.idealframework.core.json.jackson.JacksonUtils;
import cn.idealframework.core.trace.TraceConstants;
import cn.idealframework.core.trace.TraceContext;
import cn.idealframework.core.trace.TraceContextHolder;
import cn.idealframework.event.message.DomainEvent;
import cn.idealframework.event.message.EventHeaders;
import cn.idealframework.event.message.EventMessage;
import cn.idealframework.event.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 宋志宗 on 2021/6/2
 */
@RequiredArgsConstructor
public class BusEventPublisherImpl implements EventPublisher {
  private final com.zzsong.bus.client.EventPublisher eventPublisher;

  @Override
  public void publish(@Nonnull Collection<EventMessage<? extends DomainEvent>> messages) {
    Optional<TraceContext> contextOptional = TraceContextHolder.current();
    List<com.zzsong.bus.common.message.EventMessage<?>> messageList
        = messages.stream().map(msg -> {
      com.zzsong.bus.common.message.EventMessage<?> eventMessage = this.convert(msg);
      contextOptional.ifPresent(traceContext -> {
        com.zzsong.bus.common.message.EventHeaders headers = eventMessage.getHeaders();
        headers.set(TraceConstants.HTTP_HEADER_TRACE_ID, traceContext.getTraceId());
        headers.set(TraceConstants.HTTP_HEADER_SPAN_ID, traceContext.generateNextSpanId());
      });
      return eventMessage;
    }).collect(Collectors.toList());
    eventPublisher.publish(messageList).block();
  }

  @Nonnull
  private com.zzsong.bus.common.message.EventMessage<?> convert(EventMessage<? extends DomainEvent> message) {
    com.zzsong.bus.common.message.EventMessage<Object> eventMessage
        = new com.zzsong.bus.common.message.EventMessage<>();
    eventMessage.setUuid(message.uuid());
    eventMessage.setTransactionId(null);
    eventMessage.setEntity(message.getAggregateType());
    eventMessage.setAggregate(message.getAggregateId());
    eventMessage.setExternalApp(null);
    eventMessage.setTopic(message.getTopic());
    eventMessage.setTag(null);
    EventHeaders headers = message.getHeaders();
    if (headers != null) {
      String jsonString = JacksonUtils.toJsonString(headers);
      com.zzsong.bus.common.message.EventHeaders eventHeaders
          = JacksonUtils.parse(jsonString, com.zzsong.bus.common.message.EventHeaders.class);
      eventMessage.setHeaders(eventHeaders);
    }
    eventMessage.setPayload(message.getPayload());
    return eventMessage;
  }
}
