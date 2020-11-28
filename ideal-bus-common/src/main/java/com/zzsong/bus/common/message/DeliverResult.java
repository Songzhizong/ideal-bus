package com.zzsong.bus.common.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliverResult {

  private long eventId;

  private Status status;

  @Nonnull
  private String message = "";

  public enum Status {
    /**
     * 交付成功
     * <p>客户端成功接收消息并完成处理</p>
     */
    ACK,

    /**
     * 执行失败
     * <p>客户端成功接收但处理失败</p>
     */
    UN_ACK,

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
}
