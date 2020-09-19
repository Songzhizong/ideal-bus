package com.zzsong.bus.receiver.listener;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 宋志宗 on 2020/9/17
 */
public class ListenerFactory {
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
   * @return 同一个主题的监听器名称重复则返回false
   */
  public static boolean register(@Nonnull String topic,
                                 @Nonnull String listenerName,
                                 @Nonnull IEventListener listener) {
    Map<String, IEventListener> listenerMap
        = LISTENER_MAPPING.computeIfAbsent(topic, k -> new HashMap<>());
    if (listenerMap.containsKey(listenerName)) {
      return false;
    }
    listenerMap.put(listenerName, listener);
    return true;
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
