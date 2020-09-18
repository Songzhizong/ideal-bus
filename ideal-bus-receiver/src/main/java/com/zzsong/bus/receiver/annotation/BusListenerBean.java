package com.zzsong.bus.receiver.annotation;

import java.lang.annotation.*;

/**
 * @author 宋志宗 on 2020/6/4
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BusListenerBean {
}
