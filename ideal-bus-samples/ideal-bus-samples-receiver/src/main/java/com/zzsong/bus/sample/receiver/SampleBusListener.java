package com.zzsong.bus.sample.receiver;

import com.zzsong.bus.common.transfer.Res;
import com.zzsong.bus.common.util.JsonUtils;
import com.zzsong.bus.receiver.annotation.BusListenerBean;
import com.zzsong.bus.receiver.annotation.EventListener;
import com.zzsong.bus.receiver.deliver.EventContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/18
 */
@Component
@BusListenerBean
public class SampleBusListener {
  private static final Logger log = LoggerFactory.getLogger(SampleBusListener.class);

  @EventListener(topic = "test", autoAck = false)
  public void test(@Nonnull EventContext<Res<List<String>>> context) {
    log.info("接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }
}
