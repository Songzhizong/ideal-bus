package com.zzsong.bus.receiver;

import com.fasterxml.jackson.databind.JavaType;
import com.zzsong.bus.common.util.ConditionMatcher;
import com.zzsong.bus.common.util.JsonUtils;
import com.zzsong.bus.receiver.annotation.BusListenerBean;
import com.zzsong.bus.receiver.annotation.EventListener;
import com.zzsong.bus.receiver.deliver.EventContext;
import com.zzsong.bus.receiver.listener.ListenerFactory;
import com.zzsong.bus.receiver.listener.MethodEventListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @author 宋志宗 on 2020/9/18
 */
public class SpringBusReceiver extends SimpleBusReceiver
    implements ApplicationContextAware, SmartInitializingSingleton {
  private static final Logger log = LoggerFactory.getLogger(SpringBusReceiver.class);
  private ApplicationContext applicationContext;

  public SpringBusReceiver(ExecutorService executorService) {
    super(executorService);
  }

  @Override
  public void afterSingletonsInstantiated() {
    super.startReceiver();
  }

  @Override
  public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  protected void initEventListeners() {
    Map<String, Object> beanMapping = applicationContext
        .getBeansWithAnnotation(BusListenerBean.class);
    Collection<Object> beans = beanMapping.values();
    for (Object bean : beans) {
      Class<?> aClass = bean.getClass();
      Method[] methods = aClass.getMethods();
      for (Method method : methods) {
        EventListener annotation = method.getAnnotation(EventListener.class);
        if (annotation != null) {
          String topic = annotation.topic();
          if (StringUtils.isBlank(topic)) {
            String className = bean.getClass().getName();
            String methodName = method.getName();
            log.error("{}#{} 未指定 topic", className, methodName);
            continue;
          }
          Parameter[] parameters = method.getParameters();
          if (parameters.length == 1) {
            Parameter parameter = parameters[0];
            ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
            Class<?> typeClass = (Class<?>) parameterizedType.getRawType();
            if (typeClass != EventContext.class) {
              String className = bean.getClass().getName();
              String methodName = method.getName();
              log.error("{}#{} 入参必须是 com.zzsong.bus.receiver.deliver.EventContext类型", className, methodName);
              continue;
            }
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length == 0) {
              String className = bean.getClass().getName();
              String methodName = method.getName();
              log.error("{}#{} 入参缺少泛型", className, methodName);
              continue;
            }
            JavaType javaType = JsonUtils.getJavaType(typeArguments[0]);
            boolean autoAck = annotation.autoAck();
            String conditions = annotation.conditions();
            List<Set<String>> sets = ConditionMatcher.parseConditionString(conditions);
            MethodEventListener listener = new MethodEventListener(autoAck, bean, method, javaType, sets);
            String listenerName = listener.getListenerName();
            ListenerFactory.register(topic, listenerName, listener);
          } else {
            String className = bean.getClass().getName();
            String methodName = method.getName();
            log.error("{}#{} 参数列表长度不合法", className, methodName);
          }
        }
      }
    }
  }
}