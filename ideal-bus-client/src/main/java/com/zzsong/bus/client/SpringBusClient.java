package com.zzsong.bus.client;

import com.zzsong.bus.receiver.BusReceiver;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * @author 宋志宗 on 2020/9/20 12:53 上午
 */
public class SpringBusClient extends SimpleBusClient implements SmartInitializingSingleton {

  public SpringBusClient(BusReceiver busReceiver) {
    super(busReceiver);
  }

  @Override
  public void afterSingletonsInstantiated() {
    super.startClient();
  }
}
