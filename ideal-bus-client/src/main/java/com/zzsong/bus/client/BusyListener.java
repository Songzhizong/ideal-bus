package com.zzsong.bus.client;

/**
 * @author 宋志宗 on 2021/4/29
 */
public interface BusyListener {
  String getListenerId();

  void busyNotice();

  void idleNotice();
}
