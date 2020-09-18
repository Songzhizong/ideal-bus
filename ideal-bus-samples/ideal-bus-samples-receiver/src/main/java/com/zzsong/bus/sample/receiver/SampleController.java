package com.zzsong.bus.sample.receiver;

import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import com.zzsong.bus.common.message.EventHeaders;
import com.zzsong.bus.common.transfer.Res;
import com.zzsong.bus.common.util.JsonUtils;
import com.zzsong.bus.receiver.BusReceiver;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/18
 */
@RestController
public class SampleController {
  @Nonnull
  private final BusReceiver receiver;

  public SampleController(@Nonnull BusReceiver receiver) {
    this.receiver = receiver;
  }

  @PostMapping("/bus/receive")
  public DeliveredResult receive(@RequestBody @Nonnull DeliveredEvent event) {
    return receiver.receive(event).block();
  }

  public static void main(String[] args) {
    List<String> list = new ArrayList<>();
    list.add("1");
    list.add("2");
    list.add("3");
    Res<List<String>> payload = Res.data(list);

    DeliveredEvent event = new DeliveredEvent();
    event.setInstanceId(1232412345L);
    event.setEventId("event_id");
    event.setBizId("biz_id");
    event.setTopic("test");
    event.setHeaders(new EventHeaders().add("name", "张三").add("name", "李四").add("age", "16"));
    event.setPayload(payload);
    event.setTimestamp(System.currentTimeMillis());
    System.out.println(JsonUtils.toJsonString(event, true, true));
  }
}
