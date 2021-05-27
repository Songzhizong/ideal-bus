package com.zzsong.bus.sample.client;

import com.zzsong.bus.client.annotation.BusListenerBean;
import com.zzsong.bus.client.annotation.EventListener;
import com.zzsong.bus.client.deliver.EventContext;
import com.zzsong.bus.common.share.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 宋志宗 on 2020/9/18
 */
@Slf4j
@Component
@BusListenerBean
public class SampleBusListener {
  private final AtomicInteger counter = new AtomicInteger();

  @EventListener(name = "testUnAck", topic = "testUnAck", autoAck = false)
  public void testUnAck(@Nonnull EventContext<List<String>> context) throws InterruptedException {
    TimeUnit.SECONDS.sleep(5);
    log.info("testUnAck 接收到消息: {}, 序号: {}",
        JsonUtils.toJsonString(context.getPayload()), counter.incrementAndGet());
  }

  @EventListener(
      name = "testAutoAck",
      topic = "example_topic",
      condition = "age>10",
      delayExp = "-1",
      autoAck = true
  )
  @SuppressWarnings("DefaultAnnotationParam")
  public void testAutoAck(@Nonnull EventContext<List<String>> context) {
//    try {
//      TimeUnit.SECONDS.sleep(1);
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
    log.info("testAutoAck接收到消息: {}, 序号: {}",
        String.join(", ", context.getPayload()), counter.incrementAndGet());
  }

  @EventListener(name = "broadcast", topic = "broadcast")
  public void broadcast(@Nonnull EventContext<List<String>> context) {
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    log.info("broadcast 接收到消息: {}, 序号: {}",
        JsonUtils.toJsonString(context.getPayload()), counter.incrementAndGet());
  }
}
