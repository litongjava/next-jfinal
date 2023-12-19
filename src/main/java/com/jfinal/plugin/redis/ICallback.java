package com.jfinal.plugin.redis;

/**
 * ICallback.
 * 将多个 redis 操作放在同一个redis连下中使用，另外也可以让同一个
 * Cache 对象使用 select(int) 方法临时切换数据库
 */
@FunctionalInterface
public interface ICallback<T> {
  T call(Cache cache);
}
