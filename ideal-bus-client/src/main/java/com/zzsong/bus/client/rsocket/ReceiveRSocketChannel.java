package com.zzsong.bus.client.rsocket;

import com.zzsong.bus.client.Channel;
import com.zzsong.bus.client.ExecutorListener;

/**
 * @author 宋志宗 on 2021/4/28
 */
public interface ReceiveRSocketChannel extends RSocketChannel, Channel, ExecutorListener {

}
