package com.jfinal.plugin.redis;

import com.jfinal.plugin.redis.serializer.FstSerializer;
import com.jfinal.plugin.redis.serializer.ISerializer;

/**
 * Serializer 用于 Redis.call(...)、Redis.use().call(...) 对数据进行序列化与反序列化
 */
public class Serializer {

  /*
   * 与 RedisPlugin.setSerializer(...) 同步持有序列化策略类
   */
  static ISerializer serializer = FstSerializer.me;

  /**
   * 序列化
   */
  public static byte[] to(Object value) {
    try {
      return serializer.valueToBytes(value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 反序列化
   */
  @SuppressWarnings({ "unchecked" })
  public static <T> T from(byte[] bytes) {
    try {
      return (T) serializer.valueFromBytes(bytes);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
