package com.zzsong.bus.client;

import com.fasterxml.jackson.databind.JavaType;
import com.zzsong.bus.client.annotation.BusListenerBean;
import com.zzsong.bus.client.annotation.EventListener;
import com.zzsong.bus.client.deliver.EventContext;
import com.zzsong.bus.client.listener.ListenerFactory;
import com.zzsong.bus.client.listener.MethodEventListener;
import com.zzsong.bus.common.share.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * @author 宋志宗 on 2020/9/20 12:53 上午
 */
@Slf4j
public class SpringBusClient extends SimpleBusClient
    implements ApplicationContextAware, SmartInitializingSingleton {
  private ApplicationContext applicationContext;


  public SpringBusClient(int corePoolSize, int maximumPoolSize) {
    super(corePoolSize, maximumPoolSize);
  }

  @Override
  public void afterSingletonsInstantiated() {
    super.startClient();
  }

  @Override
  public void setApplicationContext(@Nonnull ApplicationContext applicationContext)
      throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  @SuppressWarnings("DuplicatedCode")
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
            ParameterizedType parameterizedType = (ParameterizedType) parameter
                .getParameterizedType();
            Class<?> typeClass = (Class<?>) parameterizedType.getRawType();
            if (typeClass != EventContext.class) {
              String className = bean.getClass().getName();
              String methodName = method.getName();
              //noinspection SpellCheckingInspection
              log.error("{}#{} 入参必须是 com.zzsong.bus.receiver.deliver.EventContext类型",
                  className, methodName);
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
            String listenerName = annotation.name();
            String condition = annotation.condition();
            String delayExp = annotation.delayExp();
            boolean autoAck = annotation.autoAck();

            MethodEventListener listener = new MethodEventListener(autoAck, delayExp,
                bean, method, condition, listenerName, javaType);
            ListenerFactory.register(topic, listener.getListenerName(), listener);
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
