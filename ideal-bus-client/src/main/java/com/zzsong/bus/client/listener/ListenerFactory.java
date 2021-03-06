package com.zzsong.bus.client.listener;

import lombok.extern.apachecommons.CommonsLog;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 宋志宗 on 2020/9/17
 */
@CommonsLog
public class ListenerFactory {
  private ListenerFactory() {
  }

  /**
   * topic -> listenerName -> IEventListener
   */
  private static final Map<String, Map<String, IEventListener>> LISTENER_MAPPING = new HashMap<>();

  public static Map<String, Map<String, IEventListener>> getAll() {
    return LISTENER_MAPPING;
  }

  /**
   * 注册监听器
   *
   * @param topic        监听的主题
   * @param listenerName 监听器名称
   * @param listener     监听器对象
   */
  public static void register(@Nonnull String topic,
                              @Nonnull String listenerName,
                              @Nonnull IEventListener listener) {
    Map<String, IEventListener> listenerMap
        = LISTENER_MAPPING.computeIfAbsent(topic, k -> new HashMap<>(4));
    IEventListener previous = listenerMap.put(listenerName, listener);
    if (previous != null) {
      log.error("监听器名称: " + listenerName + " 重复");
      System.exit(0);
    }
  }

  /**
   * 获取某个主题所有的监听器
   *
   * @param topic 事件主题
   * @return 监听该主题的所有监听器, key为监听器名称
   */
  @Nonnull
  public static Map<String, IEventListener> get(@Nonnull String topic) {
    Map<String, IEventListener> map = LISTENER_MAPPING.get(topic);
    if (map == null) {
      return Collections.emptyMap();
    }
    return map;
  }
}
