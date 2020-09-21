package com.zzsong.bus.sample.client;

import com.zzsong.bus.receiver.annotation.BusListenerBean;
import com.zzsong.bus.receiver.annotation.EventListener;
import com.zzsong.bus.receiver.deliver.EventContext;
import com.zzsong.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/18
 */
@Slf4j
@Component
@BusListenerBean
public class SampleBusListener {

  @EventListener(topic = "test", autoAck = false)
  public void testUnAck(@Nonnull EventContext<List<String>> context) {
    log.info("testUnAck 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "test", autoAck = true)
  public void testAutoAck(@Nonnull EventContext<List<String>> context) {
    log.info("testAutoAck 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "broadcast")
  public void broadcast(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "a")
  public void a(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "b")
  public void b(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "c")
  public void c(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "d")
  public void d(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "e")
  public void e(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "f")
  public void f(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "g")
  public void g(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "h")
  public void h(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "i")
  public void i(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "j")
  public void j(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "k")
  public void k(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "l")
  public void l(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "m")
  public void m(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "n")
  public void n(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }
}
