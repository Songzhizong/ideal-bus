package com.zzsong.bus.client.spring.boot.starter;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
public class BusPublishProperties {
  /**
   * 是启用消息发布
   */
  private boolean enabled = true;


  private String httpBaseUrl;
}
