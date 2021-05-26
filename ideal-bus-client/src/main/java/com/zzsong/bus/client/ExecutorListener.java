package com.zzsong.bus.client;

/**
 * @author 宋志宗 on 2021/5/24
 */
public interface ExecutorListener {
  /** 忙碌通知 */
  void onBusy();

  /** 空闲通知 */
  void onIdle();
}
