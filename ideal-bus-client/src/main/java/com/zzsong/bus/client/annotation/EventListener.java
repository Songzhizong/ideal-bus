package com.zzsong.bus.client.annotation;

import java.lang.annotation.*;

/**
 * @author 宋志宗 on 2020/6/4
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventListener {
  /**
   * 监听的主题
   */
  String topic();

  /**
   * 监听器名称, 同一个topic不能出现名称相同的监听器
   * <p>如果topic在订阅者服务内有多个监听器则需要通过此配置来进行区分</p>
   */
  String name() default "default";

  /**
   * 监听条件
   */
  String condition() default "";

  /**
   * 在不抛出异常的情况下是否自动ack, 默认true
   * <p>如果方法抛出了异常, 将不会ack</p>
   *
   * @return 是否自动ack
   */
  boolean autoAck() default true;
}
