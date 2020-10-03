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
   * 监听器名称, 同一个服务内必须保证唯一
   */
  String name();

  /**
   * 监听的主题
   */
  String topic();

  /**
   * 条件表达式
   */
  String condition() default "";

  /**
   * 延迟表达式, 单位秒
   * <pre>
   * - 固定延迟时间: "120" (事件产生后延迟120秒消费)
   * - 基于事件头的固定值: "cycle" (cycle是EventHeader中的key, 通过该key对应的value确定一个固定时间)
   * </pre>
   */
  String delayExp() default "";

  /**
   * 在不抛出异常的情况下是否自动ack, 默认true
   * <p>如果方法抛出了异常, 将不会ack</p>
   *
   * @return 是否自动ack
   */
  boolean autoAck() default true;
}
