package com.zzsong.bus.client.spring.boot.starter;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Configuration
@EnableConfigurationProperties({BusClientProperties.class})
public class BusClientAutoConfig {

}
