package com.zzsong.bus.client;

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
   * handler 名称
   * <p>如果topic在订阅者服务内有多个监听器则需要通过此配置来进行区分</p>
   */
  String handler() default "default";

  /**
   * 监听条件
   */
  String condition() default "";
}
