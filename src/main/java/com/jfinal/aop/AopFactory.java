package com.jfinal.aop;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jfinal.model.DestroyableBean;
import com.jfinal.proxy.Proxy;

/**
 * AopFactory 是工具类 Aop 功能的具体实现，详细用法见 Aop
 */
public class AopFactory {

  // 单例缓存
  protected Map<Class<?>, Object> singletonCache = new ConcurrentHashMap<Class<?>, Object>();

  // 支持循环注入
  // protected ThreadLocal<HashMap<Class<?>, Object>> singletonTl = ThreadLocal.withInitial(() -> new HashMap<>());
  protected ThreadLocal<HashMap<Class<?>, Object>> singletonTl = initThreadLocalHashMap();

//  protected ThreadLocal<HashMap<Class<?>, Object>> prototypeTl = ThreadLocal.withInitial(() -> new HashMap<>());
  protected ThreadLocal<HashMap<Class<?>, Object>> prototypeTl = initThreadLocalHashMap();

  // 父类到子类、接口到实现类之间的映射关系
  protected HashMap<Class<?>, Class<?>> mapping = null;

  protected boolean singleton = true; // 默认单例

  protected boolean injectSuperClass = false; // 默认不对超类进行注入

  protected List<DestroyableBean> destroyableBeans = new ArrayList<>();

  public ThreadLocal<HashMap<Class<?>, Object>> initThreadLocalHashMap() {
    return new ThreadLocal<HashMap<Class<?>, Object>>() {
      @Override
      protected HashMap<Class<?>, Object> initialValue() {
        return new HashMap<>();
      }
    };
  }

  public <T> T get(Class<T> targetClass) {
    try {
      return doGet(targetClass);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T getWithMapping(Class<T> targetClass, Map<Class<Object>, Class<? extends Object>> interfaceMapping) {
    try {
      return doGetgetWithMapping(targetClass, interfaceMapping);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  protected <T> T doGet(Class<T> targetClass, Class<?> intrefaceClass) throws ReflectiveOperationException {
    // Aop.get(obj.getClass()) 可以用 Aop.inject(obj)，所以注掉下一行代码
    // targetClass = (Class<T>)getUsefulClass(targetClass);

    targetClass = (Class<T>) getMappingClass(targetClass);

    Singleton si = targetClass.getAnnotation(Singleton.class);
    boolean singleton = (si != null ? si.value() : this.singleton);

    if (singleton) {
      return doGetSingleton(targetClass, intrefaceClass);
    } else {
      return doGetPrototype(targetClass, intrefaceClass);
    }
  }

  @SuppressWarnings("unchecked")
  protected <T> T doGetSingleton(Class<T> targetClass, Class<?> intrefaceClass) throws ReflectiveOperationException {
    Object ret = singletonCache.get(targetClass);
    if (ret != null) {
      return (T) ret;
    }

    HashMap<Class<?>, Object> map = singletonTl.get();
    int size = map.size();
    if (size > 0) {
      ret = map.get(targetClass);
      if (ret != null) { // 发现循环注入
        return (T) ret;
      }
    }

    synchronized (this) {
      try {
        ret = singletonCache.get(targetClass);
        if (ret == null) {
          if (intrefaceClass != null) {
            ret = createObject(targetClass, intrefaceClass);
          } else {
            ret = createObject(targetClass);
          }
        }
        map.put(targetClass, ret);
        doInject(targetClass, ret);
        singletonCache.put(targetClass, ret);
        return (T) ret;
      } finally {
        if (size == 0) { // 仅顶层才需要 remove()
          singletonTl.remove();
        }
      }
    }
  }

  protected <T> T doGet(Class<T> targetClass) throws ReflectiveOperationException {
    return doGet(targetClass, null);
  }

  @SuppressWarnings("unchecked")
  protected <T> T doGetgetWithMapping(Class<T> targetClass,
      Map<Class<Object>, Class<? extends Object>> interfaceMapping) throws ReflectiveOperationException {
    // Aop.get(obj.getClass()) 可以用 Aop.inject(obj)，所以注掉下一行代码
    // targetClass = (Class<T>)getUsefulClass(targetClass);

    targetClass = (Class<T>) getMappingClass(targetClass);

    Singleton si = targetClass.getAnnotation(Singleton.class);
    boolean singleton = (si != null ? si.value() : this.singleton);

    if (singleton) {
      return doGetSingletonWithMapping(targetClass, interfaceMapping);
    } else {
      return doGetPrototypeWithMapping(targetClass, interfaceMapping);
    }
  }

  @SuppressWarnings("unchecked")
  protected <T> T doGetgetWithMapping(Class<T> targetClass, Class<?> typeMaybeInterface,
      Map<Class<Object>, Class<? extends Object>> interfaceMapping) throws ReflectiveOperationException {
    targetClass = (Class<T>) getMappingClass(targetClass);

    Singleton si = targetClass.getAnnotation(Singleton.class);
    boolean singleton = (si != null ? si.value() : this.singleton);

    if (singleton) {
      return doGetSingletonWithMapping(targetClass, typeMaybeInterface, interfaceMapping);
    } else {
      return doGetPrototypeWithMapping(targetClass, typeMaybeInterface, interfaceMapping);
    }

  }

  protected <T> T doGetSingletonWithMapping(Class<T> targetClass,
      Map<Class<Object>, Class<? extends Object>> interfaceMapping) throws ReflectiveOperationException {
    return doGetSingletonWithMapping(targetClass, null, interfaceMapping);
  }

  /**
   * @param <T>
   * @param targetClass 目标类
   * @param typeMaybeInterface 目标类的接口或者抽象类 
   * @param interfaceMapping 目标类内成员变量的 接口和实现类映射 
   * @return
   * @throws ReflectiveOperationException
   */
  @SuppressWarnings("unchecked")
  protected <T> T doGetSingletonWithMapping(Class<T> targetClass, Class<?> typeMaybeInterface,
      Map<Class<Object>, Class<? extends Object>> interfaceMapping) throws ReflectiveOperationException {
    Object ret = singletonCache.get(targetClass);
    if (ret != null) {
      return (T) ret;
    }

    HashMap<Class<?>, Object> map = singletonTl.get();
    int size = map.size();
    if (size > 0) {
      ret = map.get(targetClass);
      if (ret != null) { // 发现循环注入
        return (T) ret;
      }
    }

    synchronized (this) {
      try {
        ret = singletonCache.get(targetClass);
        if (ret == null) {
          if (typeMaybeInterface == null) {
            ret = createObject(targetClass);
          } else {
            ret = createObject(targetClass, typeMaybeInterface);
          }

          map.put(targetClass, ret);
          doInjectWithMapping(targetClass, ret, interfaceMapping);
          singletonCache.put(targetClass, ret);
        }
        return (T) ret;
      } finally {
        if (size == 0) { // 仅顶层才需要 remove()
          singletonTl.remove();
        }
      }
    }
  }

  protected <T> T doGetSingleton(Class<T> targetClass) throws ReflectiveOperationException {
    return doGetSingletonWithMapping(targetClass, null);
  }

  protected <T> T doGetPrototypeWithMapping(Class<T> targetClass,
      Map<Class<Object>, Class<? extends Object>> interfaceMapping) throws ReflectiveOperationException {
    return doGetPrototypeWithMapping(targetClass, null, interfaceMapping);
  }

  /**
   * @param <T>
   * @param targetClass 目标类
   * @param typeMaybeInterface 目标类的接口或者抽象类 
   * @param interfaceMapping 目标类内成员变量的 接口和实现类映射 
   * @return
   * @throws ReflectiveOperationException
   */
  @SuppressWarnings("unchecked")
  protected <T> T doGetPrototypeWithMapping(Class<T> targetClass, Class<?> typeMaybeInterface,
      Map<Class<Object>, Class<? extends Object>> interfaceMapping) throws ReflectiveOperationException {
    Object ret;

    HashMap<Class<?>, Object> map = prototypeTl.get();
    int size = map.size();
    if (size > 0) {
      ret = map.get(targetClass);
      if (ret != null) { // 发现循环注入
        // return (T)ret;
        if (interfaceMapping != null) {
          return (T) createObject(targetClass, typeMaybeInterface);
        } else {
          return (T) createObject(targetClass);
        }

      }
    }

    try {
      if (interfaceMapping != null) {
        ret = (T) createObject(targetClass, typeMaybeInterface);
      } else {
        ret = (T) createObject(targetClass);
      }
      map.put(targetClass, ret);
      doInject(targetClass, ret);
      return (T) ret;
    } finally {
      if (size == 0) { // 仅顶层才需要 clear()
        map.clear();
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected <T> T doGetPrototype(Class<T> targetClass, Class<?> intrefaceClass) throws ReflectiveOperationException {
    Object ret;

    HashMap<Class<?>, Object> map = prototypeTl.get();
    int size = map.size();
    if (size > 0) {
      ret = map.get(targetClass);
      if (ret != null) { // 发现循环注入
        // return (T)ret;
        return (T) createObject(targetClass, intrefaceClass);
      }
    }

    try {
      ret = createObject(targetClass, intrefaceClass);
      map.put(targetClass, ret);
      doInject(targetClass, ret);
      return (T) ret;
    } finally {
      if (size == 0) { // 仅顶层才需要 clear()
        map.clear();
      }
    }
  }

  protected <T> T doGetPrototype(Class<T> targetClass) throws ReflectiveOperationException {
    return doGetPrototypeWithMapping(targetClass, null);
  }

  public <T> T inject(T targetObject) {
    try {
      doInject(targetObject.getClass(), targetObject);
      return targetObject;
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  // 方法原型的参数测试过可以是：Class<? super T> targetClass, T targetObject
  public <T> T inject(Class<T> targetClass, T targetObject) {
    try {
      doInject(targetClass, targetObject);
      return targetObject;
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  protected void doInjectWithMapping(Class<?> targetClass, Object targetObject,
      Map<Class<Object>, Class<? extends Object>> interfaceMapping) throws ReflectiveOperationException {

    targetClass = getUsefulClass(targetClass);
    Field[] fields = targetClass.getDeclaredFields();
    if (fields.length != 0) {
      for (Field field : fields) {
        if (field.isAnnotationPresent(Autowired.class)) {
          Class<?> typeMaybeInterface = field.getType();
          Object fieldInjectedObject = null;
          // 从interfaceMapping中查找实现类
          if (interfaceMapping != null) {
            Class<? extends Object> implClazz = interfaceMapping.get(typeMaybeInterface);
            if (implClazz != null) {
              fieldInjectedObject = doGetgetWithMapping(implClazz, typeMaybeInterface, interfaceMapping);
              interfaceMapping.remove(typeMaybeInterface);
            } else {
              fieldInjectedObject = doGetgetWithMapping(typeMaybeInterface, interfaceMapping);
            }

          } else {
            fieldInjectedObject = doGet(typeMaybeInterface);
          }
          field.setAccessible(true);
          field.set(targetObject, fieldInjectedObject);
        }

        Inject inject = field.getAnnotation(Inject.class);
        if (inject == null) {
          continue;
        }

        Class<?> fieldInjectedClass = inject.value();
        if (fieldInjectedClass == Void.class) {
          fieldInjectedClass = field.getType();
        }

        Object fieldInjectedObject = doGet(fieldInjectedClass);
        field.setAccessible(true);
        field.set(targetObject, fieldInjectedObject);
      }
    }
  }

  protected void doInject(Class<?> targetClass, Object targetObject) throws ReflectiveOperationException {
    targetClass = getUsefulClass(targetClass);
    Field[] fields = targetClass.getDeclaredFields();
    if (fields.length != 0) {
      for (Field field : fields) {
        if (field.isAnnotationPresent(Autowired.class)) {
          Class<?> typeMaybeInterface = field.getType();
          Object fieldInjectedObject = null;
          // 从interfaceMapping中查找实现类
          fieldInjectedObject = doGet(typeMaybeInterface);
          field.setAccessible(true);
          field.set(targetObject, fieldInjectedObject);
        }

        Inject inject = field.getAnnotation(Inject.class);
        if (inject == null) {
          continue;
        }

        Class<?> fieldInjectedClass = inject.value();
        if (fieldInjectedClass == Void.class) {
          fieldInjectedClass = field.getType();
        }

        Object fieldInjectedObject = doGet(fieldInjectedClass);
        field.setAccessible(true);
        field.set(targetObject, fieldInjectedObject);
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected Object createObject(Class<?> targetClass) throws ReflectiveOperationException {
    Class<?>[] interfaces = targetClass.getInterfaces();
    if (interfaces.length > 0) {
      addMapping((Class<Object>) interfaces[0], (Class<? extends Object>) targetClass);
    }
    return Proxy.get(targetClass);
  }

  protected Object createObject(Class<?> targetClass, Class<?> intrefaceClass) {
    if (intrefaceClass != null) {
      addMapping(intrefaceClass, targetClass);
    }
    return Proxy.get(targetClass);
  }

  /**
   * 字符串包含判断之 "_$$_" 支持 javassist，"$$Enhancer" 支持 cglib
   * 
   * 被 cglib、guice 增强过的类需要通过本方法获取到被增强之前的类型
   * 否则调用其 targetClass.getDeclaredFields() 方法时
   * 获取到的是一堆 cglib guice 生成类中的 Field 对象
   * 而被增强前的原类型中的 Field 反而获取不到
   */
  protected Class<?> getUsefulClass(Class<?> clazz) {
    // com.demo.blog.Blog$$EnhancerByCGLIB$$69a17158
    // return (Class<? extends Model>)((modelClass.getName().indexOf("EnhancerByCGLIB") == -1 ? modelClass : modelClass.getSuperclass()));
    // return (Class<?>)(clazz.getName().indexOf("$$EnhancerBy") == -1 ? clazz : clazz.getSuperclass());
    String n = clazz.getName();
    return (Class<?>) (n.indexOf("_$$_") > -1 || n.indexOf("$$Enhancer") > -1 ? clazz.getSuperclass() : clazz);
  }

  /**
   * 设置被注入的对象是否为单例，可使用 @Singleton(boolean) 覆盖此默认值 
   */
  public AopFactory setSingleton(boolean singleton) {
    this.singleton = singleton;
    return this;
  }

  public boolean isSingleton() {
    return singleton;
  }

  /**
   * 设置是否对超类进行注入
   */
  public AopFactory setInjectSuperClass(boolean injectSuperClass) {
    this.injectSuperClass = injectSuperClass;
    return this;
  }

  public boolean isInjectSuperClass() {
    return injectSuperClass;
  }

  public AopFactory addSingletonObject(Class<?> type, Object singletonObject) {
    if (type == null) {
      throw new IllegalArgumentException("type can not be null");
    }
    if (singletonObject == null) {
      throw new IllegalArgumentException("singletonObject can not be null");
    }
    if (singletonObject instanceof Class) {
      throw new IllegalArgumentException("singletonObject can not be Class type");
    }

    if (!(type.isAssignableFrom(singletonObject.getClass()))) {
      throw new IllegalArgumentException(singletonObject.getClass().getName() + " can not cast to " + type.getName());
    }

    // Class<?> type = getUsefulClass(singletonObject.getClass());
    if (singletonCache.putIfAbsent(type, singletonObject) != null) {
      throw new RuntimeException("Singleton object already exists for type : " + type.getName());
    }

    return this;
  }

  public AopFactory addSingletonObject(Object singletonObject) {
    Class<?> type = getUsefulClass(singletonObject.getClass());
    return addSingletonObject(type, singletonObject);
  }

  public synchronized <T> AopFactory addMapping(Class<? extends T> from, Class<? extends T> to) {
    if (from == null || to == null) {
      throw new IllegalArgumentException("The parameter from and to can not be null");
    }

    if (mapping == null) {
      mapping = new HashMap<Class<?>, Class<?>>(128, 0.25F);
    } else if (mapping.containsKey(from)) {
      throw new RuntimeException("Class already mapped : " + from.getName());
    }

    mapping.put(from, to);
    return this;
  }

  public <T> AopFactory addMapping(Class<T> from, String to) {
    try {
      @SuppressWarnings("unchecked")
      Class<T> toClass = (Class<T>) Class.forName(to.trim());
      if (from.isAssignableFrom(toClass)) {
        return addMapping(from, toClass);
      } else {
        throw new IllegalArgumentException(
            "The parameter \"to\" must be the subclass or implementation of the parameter \"from\"");
      }
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 获取父类到子类的映射值，或者接口到实现类的映射值
   * @param from 父类或者接口 
   * @return 如果映射存在则返回映射值，否则返回参数 from 的值
   */
  @SuppressWarnings("unchecked")
  public Class<?> getMappingClass(Class<?> from) {
    if (mapping != null) {
      Class<?> ret = mapping.get(from);
      return ret != null ? ret : from;
    } else {
      return from;
    }
  }

  /**
   * 注册的Bean容器
   * @param targetClass
   * @param value
   */
  public void register(Class<?> targetClass, Object value) {
    HashMap<Class<?>, Object> map = singletonTl.get();
    map.put(targetClass, value);
    try {
      doInject(targetClass, value);
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }
    singletonCache.put(targetClass, value);
  }

  /**
   * get from singletonCache
   * @param type
   * @return
   */
  @SuppressWarnings("unchecked")
  public <T> T getOnly(Class<T> type) {
    Class<?> usefulClass = getUsefulClass(type);
    Class<?> mappingClass = getMappingClass(usefulClass);
    Object object = singletonCache.get(mappingClass);
    if (object != null) {
      return (T) object;
    }
    return null;
  }

  public boolean contains(Class<?> type) {
    type = getUsefulClass(type);
    Class<?> mappingClass = getMappingClass(type);
    return singletonCache.containsKey(mappingClass);
  }

  public String[] beans() {
    return singletonCache.values().stream().map(Object::toString).toArray(String[]::new);
  }

  public void clean() {
    // 单例缓存
    singletonCache = new ConcurrentHashMap<Class<?>, Object>();

    // 支持循环注入
    singletonTl = initThreadLocalHashMap();
    prototypeTl = initThreadLocalHashMap();

    // 父类到子类、接口到实现类之间的映射关系
    mapping = null;
    // 关闭类
    for (DestroyableBean bean : destroyableBeans) {
      try {
        bean.getDestroyMethod().invoke(bean.getBean());
      } catch (IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }

  public void addDestroyableBeans(List<DestroyableBean> destroyableBeans) {
    this.destroyableBeans.addAll(destroyableBeans);
  }

}
