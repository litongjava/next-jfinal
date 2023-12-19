package com.jfinal.plugin.ehcache;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * CacheKit. Useful tool box for EhCache.
 */
public class CacheKit {

  private static Logger log = LoggerFactory.getLogger(CacheKit.class);
  private static CacheManager cacheManager;

  static void init(CacheManager cacheManager) {
    CacheKit.cacheManager = cacheManager;
  }

  public static CacheManager getCacheManager() {
    return cacheManager;
  }

  static Cache getOrAddCache(String cacheName) {
    Cache cache = cacheManager.getCache(cacheName);
    if (cache == null) {
      synchronized (CacheKit.class) {
        cache = cacheManager.getCache(cacheName);
        if (cache == null) {
          log.warn("Could not find cache config [" + cacheName + "], using default.");
          cacheManager.addCacheIfAbsent(cacheName);
          cache = cacheManager.getCache(cacheName);
          log.debug("Cache [" + cacheName + "] started.");
        }
      }
    }
    return cache;
  }

  public static void put(String cacheName, Object key, Object value) {
    getOrAddCache(cacheName).put(new Element(key, value));
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(String cacheName, Object key) {
    Element element = getOrAddCache(cacheName).get(key);
    return element != null ? (T) element.getObjectValue() : null;
  }

  @SuppressWarnings("rawtypes")
  public static List getKeys(String cacheName) {
    return getOrAddCache(cacheName).getKeys();
  }

  public static void remove(String cacheName, Object key) {
    getOrAddCache(cacheName).remove(key);
  }

  public static void removeAll(String cacheName) {
    getOrAddCache(cacheName).removeAll();
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(String cacheName, Object key, IDataLoader dataLoader) {
    Object data = get(cacheName, key);
    if (data == null) {
      data = dataLoader.load();
      put(cacheName, key, data);
    }
    return (T) data;
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(String cacheName, Object key, Class<? extends IDataLoader> dataLoaderClass) {
    Object data = get(cacheName, key);
    if (data == null) {
      try {
        IDataLoader dataLoader = dataLoaderClass.newInstance();
        data = dataLoader.load();
        put(cacheName, key, data);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return (T) data;
  }
}
