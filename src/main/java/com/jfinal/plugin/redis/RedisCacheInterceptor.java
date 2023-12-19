package com.jfinal.plugin.redis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.plugin.cache.CacheName;
import com.jfinal.plugin.redis.serializer.ISerializer;

import redis.clients.jedis.Jedis;

/**
 * CacheInterceptor.
 */
public class RedisCacheInterceptor implements Interceptor {

  private static ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<String, ReentrantLock>(512);

  protected Cache getCache() {
    return Redis.use();
  }

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
    Cache cache = getCache();
    Jedis jedis = cache.getThreadLocalJedis();

    if (jedis != null) {
      putIfNotExists(inv, cache, jedis);
    }

    try {
      jedis = cache.jedisPool.getResource();
      cache.setThreadLocalJedis(jedis);
      putIfNotExists(inv, cache, jedis);
    } finally {
      cache.removeThreadLocalJedis();
      jedis.close();
    }

  }

  private void putIfNotExists(Invocation inv, Cache cache, Jedis jedis) {
    Object target = inv.getTarget();
    String cacheName = buildCacheName(inv, target);
    String cacheKey = buildCacheKey(inv);
    String redisKey = cacheName + "_" + cacheKey;
    String cacheData = jedis.get(redisKey);

    
    if (cacheData == null) {
      IKeyNamingPolicy keyNamingPolicy = cache.getKeyNamingPolicy();
      ISerializer serializer = cache.getSerializer();
      Lock lock = getLock(cacheName);
      lock.lock(); // prevent cache snowslide
      try {
        cacheData = jedis.get(redisKey);
        if (cacheData == null) {
          Object returnValue = inv.invoke();
          cache.call(j -> {
            String keyStr = keyNamingPolicy.getKeyName(redisKey);
            byte[] keyToBytes = serializer.keyToBytes(keyStr);
            byte[] valueToBytes = serializer.valueToBytes(returnValue);
            return j.set(keyToBytes, valueToBytes);
          });
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

}
