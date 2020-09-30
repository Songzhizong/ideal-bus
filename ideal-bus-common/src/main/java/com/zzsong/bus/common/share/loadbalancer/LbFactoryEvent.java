package com.zzsong.bus.common.share.loadbalancer;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author 宋志宗 on 2020/9/9
 */
@Getter
@Setter
public class LbFactoryEvent {
  /**
   * 服务名称
   */
  private String serverName;
  /**
   * 所有服务列表
   */
  private List<? extends LbServer> allServers;
  /**
   * 可达服务列表
   */
  private List<? extends LbServer> reachableServers;
}
