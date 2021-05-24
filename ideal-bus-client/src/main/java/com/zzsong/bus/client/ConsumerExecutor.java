package com.zzsong.bus.client;

import com.zzsong.bus.common.message.DeliverEvent;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2021/5/24
 */
public interface ConsumerExecutor {

  /**
   * 将消息提交到消费者执行器, 如果返回false则代表线程已用完
   *
   * @param event   消息内容
   * @param channel 通道
   * @return 是否成功提交
   * @author 宋志宗 on 2021/5/25
   */
  boolean submit(@Nonnull DeliverEvent event, @Nonnull Channel channel);

  /**
   * 添加状态监听器
   *
   * @param listener 监听器
   * @author 宋志宗 on 2021/5/25
   */
  void addListener(@Nonnull ExecutorListener listener);
}
