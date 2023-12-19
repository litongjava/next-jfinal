package com.jfinal.plugin.redis;

/**
 * IKeyNamingPolicy.
 * 架构师可以通过实现此类制定全局性的 key 命名策略，
 * 例如 Integer、String、OtherType 这些不同类型的对象
 * 选择不同的命名方式，默认命名方式是  Object.toString()
 */
@FunctionalInterface
public interface IKeyNamingPolicy {

  String getKeyName(Object key);

  static final IKeyNamingPolicy defaultKeyNamingPolicy = new IKeyNamingPolicy() {
    public String getKeyName(Object key) {
      return key.toString();
    }
  };
}
