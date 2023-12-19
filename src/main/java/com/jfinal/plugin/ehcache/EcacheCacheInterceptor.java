package com.jfinal.plugin.ehcache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.plugin.cache.CacheName;

/**
 * CacheInterceptor.
 */
public class EcacheCacheInterceptor implements Interceptor {

  private static ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<String, ReentrantLock>(512);

  private ReentrantLock getLock(String key) {
    ReentrantLock lock = lockMap.get(key);
    if (lock != null) {
      return lock;
    }

    lock = new ReentrantLock();
    ReentrantLock previousLock = lockMap.putIfAbsent(key, lock);
    return previousLock == null ? lock : previousLock;
  }

  final public void intercept(Invocation inv) {
    Object target = inv.getTarget();
    String cacheName = buildCacheName(inv, target);
    String cacheKey = buildCacheKey(inv);
    Object cacheData = CacheKit.get(cacheName, cacheKey);
    if (cacheData == null) {
      Lock lock = getLock(cacheName);
      lock.lock(); // prevent cache snowslide
      try {
        cacheData = CacheKit.get(cacheName, cacheKey);
        if (cacheData == null) {
          Object returnValue = inv.invoke();
          cacheMethodReturnValue(cacheName, cacheKey, returnValue);
          return;
        }
      } finally {
        lock.unlock();
      }
    }

    // useCacheDataAndReturn(cacheData, target);
    inv.setReturnValue(cacheData);
  }

  // TODO 考虑与 EvictInterceptor 一样强制使用 @CacheName
  protected String buildCacheName(Invocation inv, Object target) {
    CacheName cacheName = inv.getMethod().getAnnotation(CacheName.class);
    if (cacheName != null) {
      return cacheName.value();
    }

    cacheName = target.getClass().getAnnotation(CacheName.class);
    return (cacheName != null) ? cacheName.value() : inv.getMethodName();
  }

  /**
   * 返回方法名_参数的hashCode值
   * @param inv
   * @return
   */
  protected String buildCacheKey(Invocation inv) {
    StringBuilder sb = new StringBuilder(inv.getMethodName());
    Object[] args = inv.getArgs();
    for (Object object : args) {
      sb.append("_").append(object.hashCode());

    }
    return sb.toString();
  }

  protected void cacheMethodReturnValue(String cacheName, String cacheKey, Object returnValue) {
    CacheKit.put(cacheName, cacheKey, returnValue);
  }
}
