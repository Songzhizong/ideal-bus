package com.zzsong.bus.client.rsocket;

import com.zzsong.bus.common.share.loadbalancer.LbServer;

/**
 * @author 宋志宗 on 2021/4/28
 */
public interface SendRSocketChannel extends RSocketChannel, LbServer {

}
