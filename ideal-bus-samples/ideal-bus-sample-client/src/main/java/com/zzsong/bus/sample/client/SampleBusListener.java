package com.zzsong.bus.sample.client;

import com.zzsong.bus.receiver.annotation.BusListenerBean;
import com.zzsong.bus.receiver.annotation.EventListener;
import com.zzsong.bus.receiver.deliver.EventContext;
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

  @EventListener(name = "testAutoAck", topic = "testAutoAck", condition = "age>10", delayExp = "120")
  public void testAutoAck(@Nonnull EventContext<List<String>> context) throws InterruptedException {
    int incrementAndGet = counter.incrementAndGet();
    TimeUnit.SECONDS.sleep(10);
    log.info("testAutoAck 接收到消息: {}, 接收到消息序号: {}",
        JsonUtils.toJsonString(context.getPayload()), incrementAndGet);
  }

  @EventListener(name = "broadcast", topic = "broadcast")
  public void broadcast(@Nonnull EventContext<List<String>> context) {
    log.info("broadcast 接收到消息: {}, 序号: {}",
        JsonUtils.toJsonString(context.getPayload()), counter.incrementAndGet());
  }
}
