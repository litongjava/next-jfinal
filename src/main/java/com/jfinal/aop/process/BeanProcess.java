package com.jfinal.aop.process;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import com.jfinal.aop.Aop;
import com.jfinal.aop.Autowired;
import com.jfinal.aop.annotation.Configuration;
import com.jfinal.model.DestroyableBean;
import com.jfinal.model.MultiReturn;

public class BeanProcess {
  // 创建一个队列来存储 process 方法的返回值
  private Queue<Class<?>> componentClass = new LinkedList<>();
  private Queue<Class<?>> configurationClass = new LinkedList<>();

  @SuppressWarnings("unchecked")
  public void initAnnotation(List<Class<?>> scannedClasses) {
    if (scannedClasses == null) {
      return;
    }
    ConfigurationAnnotaionProcess configurationAnnotaionProcess = new ConfigurationAnnotaionProcess();
    // for(int i=0;i<scannedClasses.size();i++) {
    // log.info("{}",scannedClasses.get(i).toString());
    // }
    // interface,impl
    Map<Class<Object>, Class<? extends Object>> mapping = new ConcurrentHashMap<>();
    // 1. 分类为 Configuration类和其他类,先处理Configuration类
    for (Class<?> clazz : scannedClasses) {
      boolean annotationPresent = clazz.isAnnotationPresent(Configuration.class);
      // log.info("{},{}",clazz.toString(),annotationPresent);
      if (annotationPresent) {
        configurationClass.add(clazz);
        continue;
      }
      if (Aop.isComponent(clazz)) {
        componentClass.add(clazz);
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
          mapping.put((Class<Object>) interfaces[0], (Class<? extends Object>) clazz);
        }
      }
    }

    MultiReturn<Queue<Object>, List<DestroyableBean>, Void> processConfiguration = configurationAnnotaionProcess
        .processConfiguration(configurationClass, mapping);
    // Queue<Object> beans = processConfiguration.getR1();
    List<DestroyableBean> destroyableBeans = processConfiguration.getR2();
    Aop.addDestroyableBeans(destroyableBeans);

    // 处理autoWird注解,Aop框架已经内置改支持
    // this.processAutowired(beans);

    // 处理componment注解
    // Queue<Object> componentBeans = this.processComponent(componentClass);
    this.processComponent(componentClass, mapping);
    //
    // this.processAutowired(componentBeans);

  }

  public Queue<Object> processComponent(Queue<Class<?>> componentClass,
      Map<Class<Object>, Class<? extends Object>> mapping) {
    Queue<Object> componentBeans = new LinkedList<>();
    for (Class<?> clazz : componentClass) {
      Object object = Aop.get(clazz, mapping);
      componentBeans.add(object);
    }
    return componentBeans;
  }

  @SuppressWarnings("unused")
  private void processAutowired(Queue<Object> beans) {
    for (Object bean : beans) {
      Class<?> clazz = bean.getClass();
      for (Field field : clazz.getDeclaredFields()) {
        if (field.isAnnotationPresent(Autowired.class)) {
          Object value = Aop.get(field.getType());
          try {
            field.setAccessible(true);
            field.set(bean, value);
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
