package com.jfinal.aop;

import java.util.List;
import java.util.Map;

import com.jfinal.aop.annotation.Component;
import com.jfinal.aop.annotation.Configuration;
import com.jfinal.aop.annotation.Controller;
import com.jfinal.aop.annotation.HttpApi;
import com.jfinal.aop.annotation.Repository;
import com.jfinal.aop.annotation.Service;
import com.jfinal.aop.process.BeanProcess;
import com.jfinal.aop.process.BeforeStartConfigurationProcess;
import com.jfinal.aop.scaner.ComponentScanner;
import com.jfinal.model.DestroyableBean;

/**
 * Aop 支持在任意时空便捷使用 Aop
 * 
 * Aop 主要功能：
 * 1：Aop.get(Class) 根据 Class 去创建对象，然后对创建好的对象进行依赖注入
 * 
 * 2：Aop.inject(Object) 对传入的对象进行依赖注入
 * 
 * 3：Aop.inject(...) 与 Aop.get(...) 的区别是前者只针对传入的对象之中的属性进行注入。
 *    而后者先要使用 Class 去创建对象，创建完对象以后对该对象之中的属性进行注入。
 *    简单一句话：get(...) 比 inject(...) 多了一个目标对象的创建过程
 *    
 * 4：AopManager.me().setSingleton(...) 用于配置默认是否为单例
 * 
 * 5：在目标类上使用注解 Singleton 可以覆盖掉上面 setSingleton(...) 方法配置的默认值
 * 
 * 
 * 基本用法：
 * 1：先定义业务
 *    public class Service {
 *       @Inject
 *       OtherService otherSrv;
 *       
 *       @Before(Aaa.class)
 *       public void doIt() {
 *          ...
 *       }
 *    }
 *    
 *    public class OtherService {
 *       @Before(Bbb.class)
 *       public void doOther() {
 *          ...
 *       }
 *    }
 *    
 * 
 * 2：只进行注入，对象自己创建
 *    Service srv = Aop.inject(new Service());
 *    srv.doIt();
 *    由于 Service 对象是 new 出来的，不会被 AOP 代理，所以其 doIt() 方法上的 Aaa 拦截器并不会生效
 *    Aop.inject(...) 会对 OtherService otherSrv 进行注入，并且对 otherSrv 进行 AOP 代理，
 *    所以 OtherService.doOther() 方法上的 Bbb 拦截器会生效
 *    
 * 3：创建对象并注入
 *    Service srv = Aop.get(Service.class);
 *    srv.doIt();
 *    Aop.get(...) 用法对 OtherService otherSrv 的处理方式完全一样，在此基础之上 Service 自身也会被
 *    AOP 代理，所以 doIt() 上的 Aaa 拦截器会生效
 * 
 * 4：小结：对象的创建交给 Aop 而不是自己 new 出来，所创建的对象才能被 AOP 代理，其上的拦截器才能生效
 * 
 * 
 * 高级用法：
 * 1：@Inject 注解默认注入属性自身类型的对象，可以通过如下代码指定被注入的类型：
 *    @Inject(UserServiceImpl.class)			// 此处的 UserServiceImpl 为 UserService 的子类或实现类
 *    UserService userService;
 * 
 * 2：被注入对象默认是 singleton 单例，可以通过 AopManager.me().setSingleton(false) 配置默认不为单例
 * 
 * 3：可以在目标类中中直接配置注解 Singleton：
 *    @Singleton(false)
 *    public class MyService {...}
 *    
 *    注意：以上代码中的注解会覆盖掉 2 中 setSingleton() 方法配置的默认值
 * 
 * 4：如上 2、3 中的配置，建议的用法是：先用 setSingleton() 配置大多数情况，然后在个别
 *    违反上述配置的情况下使用 Singleton 注解来覆盖默认配置，这样可以节省大量代码
 */
public class Aop {

  static AopFactory aopFactory = new AopFactory();

  public static <T> T get(Class<T> targetClass) {
    return aopFactory.get(targetClass);
  }

  /**
   * 如果需要被注入的成员是一个接口类,从mapping中查找实现类,找到之后实例化实现类并将这个元素从mapping中移除
   * @param clazz
   * @param mapping
   * @return
   */
  public static <T> T get(Class<T> targetClass, Map<Class<Object>, Class<? extends Object>> mapping) {
    return aopFactory.getWithMapping(targetClass, mapping);
  }

  public static <T> T inject(T targetObject) {
    return aopFactory.inject(targetObject);
  }

  /*
   * 通过 AopManager.me().getAopFactory().inject(...) 可调用如下方法，不直接开放出来 public static <T> T inject(Class<T> targetClass, T targetObject) { return aopFactory.inject(targetClass, targetObject); }
   */

  /**
   * 添加到IOC容器中
   * @param type
   * @param value
   */
  public static void put(Class<?> type, Object value) {
    aopFactory.register(type, value);
  }

  public static String[] beans() {
    return aopFactory.beans();
  }

  public static void close() {
    aopFactory.clean();
  }

  public static void addDestroyableBeans(List<DestroyableBean> destroyableBeans) {
    aopFactory.addDestroyableBeans(destroyableBeans);
  }

  public static boolean isComponent(Class<?> clazz) {
    return clazz.isAnnotationPresent(Component.class)
        //
        || clazz.isAnnotationPresent(Controller.class)
        //
        || clazz.isAnnotationPresent(Service.class)
        //
        || clazz.isAnnotationPresent(Repository.class)
        //
        || clazz.isAnnotationPresent(HttpApi.class);
  }

  public static boolean isConfiguration(Class<?> clazz) {
    return clazz.isAnnotationPresent(Configuration.class);
  }

  public static List<Class<?>> scan(Class<?>... primarySources) throws Exception {
    return ComponentScanner.scan(primarySources);
  }

  public static void initAnnotation(List<Class<?>> scannedClasses) {
    new BeanProcess().initAnnotation(scannedClasses);
  }

  public static List<Class<?>> processBeforeStartConfiguration(List<Class<?>> scannedClasses) {
    return new BeforeStartConfigurationProcess().process(scannedClasses);
  }

}
