package com.jfinal.aop.process;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.aop.Aop;
import com.jfinal.aop.AopManager;
import com.jfinal.aop.annotation.Bean;
import com.jfinal.aop.annotation.Initialization;
import com.jfinal.model.DestroyableBean;
import com.jfinal.model.MultiReturn;
import com.jfinal.model.Pair;

public class ConfigurationAnnotaionProcess {
  private Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * 处理有和@Configuration注解类相似的逻辑
   * @param configurationClass
   * @param mapping 
   * @return
   */
  public MultiReturn<Queue<Object>, List<DestroyableBean>, Void> processConfiguration(
      Queue<Class<?>> configurationClass, Map<Class<Object>, Class<? extends Object>> mapping) {
    // 用于存储Bean方法及其类的信息
    List<Pair<Method, Class<?>>> beanMethods = new ArrayList<>();
    List<Pair<Method, Class<?>>> initializationMethods = new ArrayList<>();
    for (Class<?> clazz : configurationClass) {
      for (Method method : clazz.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Bean.class)) {
          beanMethods.add(new Pair<>(method, clazz));
        }
        if (method.isAnnotationPresent(Initialization.class)) {
          initializationMethods.add(new Pair<>(method, clazz));
        }
      }
    }

    // 2. 按照priority对beanMethods排序
    beanMethods.sort(Comparator.comparingInt(m -> m.getKey().getAnnotation(Bean.class).priority()));
    initializationMethods.sort(Comparator.comparingInt(m -> m.getKey().getAnnotation(Bean.class).priority()));
    Queue<Object> beans = new LinkedList<>();
    List<DestroyableBean> destroyableBeans = new ArrayList<>();
    // 3. 初始化beans
    for (Pair<Method, Class<?>> beanMethod : beanMethods) {
      Object beanInstance = this.processConfigBean(beanMethod.getValue(), beanMethod.getKey(), mapping);
      beans.add(beanInstance);

      Bean beanAnnotation = beanMethod.getKey().getAnnotation(Bean.class);
      if (!beanAnnotation.destroyMethod().isEmpty()) {

        try {
          // 尝试找到销毁方法
          Method destroyMethod = beanInstance.getClass().getMethod(beanAnnotation.destroyMethod());
          destroyableBeans.add(new DestroyableBean(beanInstance, destroyMethod));
        } catch (NoSuchMethodException e) {
          e.printStackTrace();
        } catch (SecurityException e) {
          e.printStackTrace();
        }

      }
    }
    // 4.初始化 initialization
    for (Pair<Method, Class<?>> beanMethod : initializationMethods) {
      this.processConfigInitialization(beanMethod.getValue(), beanMethod.getKey(), mapping);
    }
    // 返回初始化的Bean和可以销毁的bean
    return new MultiReturn<Queue<Object>, List<DestroyableBean>, Void>(true, beans, destroyableBeans);

  }
  
  /**
   * 处理有@Bean注解的方法
   * @param clazz
   * @param method
   * @param mapping 
   * @return
   */
  public Object processConfigBean(Class<?> clazz, Method method, Map<Class<Object>, Class<? extends Object>> mapping) {
    try {
      // 调用 @Bean 方法
      Object object = Aop.get(clazz, mapping);
      Object bean = method.invoke(object);

      // 如果 @Bean 注解中定义了 initMethod，调用该方法进行初始化
      Bean beanAnnotation = method.getAnnotation(Bean.class);
      if (!beanAnnotation.initMethod().isEmpty()) {
        Method initMethod = bean.getClass().getMethod(beanAnnotation.initMethod());
        initMethod.invoke(bean);
      }
      Class<? extends Object> realBeanClass = bean.getClass();
      String beanClassName = realBeanClass.getName();
      log.info("inited config bean:{}", beanClassName);

      Class<?> returnType = method.getReturnType();
      // 将bean添加到容器中，或进行其他操作,
      if (!returnType.getName().equals(realBeanClass.getName())) {
        AopManager.me().addMapping(returnType, realBeanClass);
        log.info("add bean mapping:{} from {}", returnType, beanClassName);
      }
      AopManager.me().addSingletonObject(bean);

      // 为单例注入依赖以后，再添加为单例供后续使用
      Aop.inject(bean);

      return bean;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  public void processConfigInitialization(Class<?> clazz, Method method,
      Map<Class<Object>, Class<? extends Object>> mapping) {
    // 调用 @Bean 方法
    try {
      // 添加到到bean容器
      method.invoke(Aop.get(clazz, mapping));
      // method.invoke(Aop.get(clazz));
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }

  }

}
