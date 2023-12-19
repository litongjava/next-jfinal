package com.jfinal.aop.process;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import com.jfinal.aop.Aop;
import com.jfinal.aop.annotation.BeforeStartConfiguration;
import com.jfinal.aop.annotation.Configuration;
import com.jfinal.model.DestroyableBean;
import com.jfinal.model.MultiReturn;

public class BeforeStartConfigurationProcess {

  @SuppressWarnings("unchecked")
  public List<Class<?>> process(List<Class<?>> scannedClasses) {
    if (scannedClasses == null) {
      return null;
    }

    // 保存Aop注解类,除了 @BeforeStartConfiguration
    List<Class<?>> aopClass = new LinkedList<>();

    // 保存 @BeforeStartConfiguration 注解类
    Queue<Class<?>> beforeStartConfigurationClass = new LinkedList<>();

    Map<Class<Object>, Class<? extends Object>> mapping = new ConcurrentHashMap<>();
    // 1. 分类为 Configuration类和其他类,先处理Configuration类
    for (Class<?> clazz : scannedClasses) {
      boolean annotationPresent = clazz.isAnnotationPresent(BeforeStartConfiguration.class);
      if (annotationPresent) {
        beforeStartConfigurationClass.add(clazz);
        continue;
      }
      annotationPresent = clazz.isAnnotationPresent(Configuration.class);
      // log.info("{},{}",clazz.toString(),annotationPresent);
      if (annotationPresent) {
        aopClass.add(clazz);
        continue;
      }
      if (Aop.isComponent(clazz)) {
        aopClass.add(clazz);
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
          mapping.put((Class<Object>) interfaces[0], (Class<? extends Object>) clazz);
        }
      }
    }

    MultiReturn<Queue<Object>, List<DestroyableBean>, Void> result = new ConfigurationAnnotaionProcess()
        .processConfiguration(beforeStartConfigurationClass, mapping);
    // 获取第二个返回结果
    Aop.addDestroyableBeans(result.getR2());

    return aopClass;
  }
}
