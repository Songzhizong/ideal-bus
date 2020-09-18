package com.zzsong.bus.receiver.annotation;

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
   * 监听条件
   */
  String conditions() default "";

  /**
   * 在不抛出异常的情况下是否自动ack, 默认true
   * <p>如果方法抛出了异常, 将不会ack</p>
   *
   * @return 是否自动ack
   */
  boolean autoAck() default true;
}
