package com.zzsong.bus.client.spring.boot.starter;

import com.zzsong.bus.client.rsocket.ReceiveRSocketChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.List;

/**
 * @author 宋志宗 on 2021/5/25
 */
@RequiredArgsConstructor
public class BusClientRunner implements ApplicationRunner {
  private final List<ReceiveRSocketChannel> receiveRSocketChannels;
  @Override
  public void run(ApplicationArguments args) {
    for (ReceiveRSocketChannel channel : receiveRSocketChannels) {
      channel.connect();
    }
  }
}
