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
   * 监听条件
   */
  String condition() default "";
}
