package com.zzsong.bus.sample.client;

import com.zzsong.bus.receiver.annotation.BusListenerBean;
import com.zzsong.bus.receiver.annotation.EventListener;
import com.zzsong.bus.receiver.deliver.EventContext;
import com.zzsong.common.utils.DateTimes;
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
  public void test(@Nonnull EventContext<List<String>> context) {
    log.info("接收到消息: {}", JsonUtils.toJsonString(context.getPayload()));
  }

  public static void main(String[] args) {
    final String format = DateTimes.format(DateTimes.parse(1600586339754L), DateTimes.yyyy_MM_dd_HH_mm_ss);
    System.out.println(format);
  }
}
