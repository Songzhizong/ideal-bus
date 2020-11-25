package com.zzsong.bus.broker.constants;

/**
 * @author 宋志宗 on 2020/11/25
 */
public enum DeliverResult {

  /** 交付成功 */
  SUCCESS,

  /**
   * 交付失败, 应用离线
   * <p>应用不在线</p>
   */
  APP_OFFLINE,

  /**
   * 交付失败, 选取的通道被关闭
   * <p>只代表当前选取的通道被关闭了, 可能存在别的可用通道</p>
   */
  CHANNEL_CLOSED,

  /** 出现未知的异常 */
  UNKNOWN_EXCEPTION,
  ;
}
