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
  public void testUnack(@Nonnull EventContext<List<String>> context) {
    log.info("testUnack 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  @EventListener(topic = "test", autoAck = true)
  public void testAutoAck(@Nonnull EventContext<List<String>> context) {
    log.info("testAutoAck 接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }
}
