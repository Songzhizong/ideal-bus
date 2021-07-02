package com.zzsong.bus.ideal.boot.starter.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 宋志宗 on 2021/4/24
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "ideal.event.bus")
public class IdealEventBusProperties {
  private boolean autoSubscribe = false;
}
