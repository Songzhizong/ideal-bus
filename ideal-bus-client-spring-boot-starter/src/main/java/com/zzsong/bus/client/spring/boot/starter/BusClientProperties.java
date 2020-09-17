package com.zzsong.bus.client.spring.boot.starter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "ideal.cmpt.api.personnel")
public class BusClientProperties {
  
}