package com.jfinal.kit;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({ "rawtypes", "serial" })
public class Kv extends HashMap {

  public Kv() {
  }

  public static Kv of(Object key, Object value) {
    return new Kv().set(key, value);
  }

  public static Kv by(Object key, Object value) {
    return new Kv().set(key, value);
  }

  public static Kv create() {
    return new Kv();
  }

  @SuppressWarnings("unchecked")
  public Kv set(Object key, Object value) {
    super.put(key, value);
    return this;
  }

  public Kv setIfNotNull(Object key, Object value) {
    if (value != null) {
      set(key, value);
    }
    return this;
  }

  @SuppressWarnings("unchecked")
  public Kv set(Map map) {
    super.putAll(map);
    return this;
  }

  @SuppressWarnings("unchecked")
  public Kv set(Kv kv) {
    super.putAll(kv);
    return this;
  }

  public Kv delete(Object key) {
    super.remove(key);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T getAs(Object key) {
    return (T) get(key);
  }

  @SuppressWarnings("unchecked")
  public <T> T getAs(Object key, T defaultValue) {
    Object ret = get(key);
    return ret != null ? (T) ret : defaultValue;
  }

  public String getStr(Object key) {
    Object s = get(key);
    return s != null ? s.toString() : null;
  }

  public Integer getInt(Object key) {
    return TypeKit.toInt(get(key));
  }

  public Long getLong(Object key) {
    return TypeKit.toLong(get(key));
  }

  public BigDecimal getBigDecimal(Object key) {
    return TypeKit.toBigDecimal(get(key));
  }

  public Double getDouble(Object key) {
    return TypeKit.toDouble(get(key));
  }

  public Float getFloat(Object key) {
    return TypeKit.toFloat(get(key));
  }

  public Number getNumber(Object key) {
    return TypeKit.toNumber(get(key));
  }

  public Boolean getBoolean(Object key) {
    return TypeKit.toBoolean(get(key));
  }

  public java.util.Date getDate(Object key) {
    return TypeKit.toDate(get(key));
  }

  public java.time.LocalDateTime getLocalDateTime(Object key) {
    return TypeKit.toLocalDateTime(get(key));
  }

  /**
   * key 存在，并且 value 不为 null
   */
  public boolean notNull(Object key) {
    return get(key) != null;
  }

  /**
   * key 不存在，或者 key 存在但 value 为null
   */
  public boolean isNull(Object key) {
    return get(key) == null;
  }

  /**
   * key 存在，并且 value 为 true，则返回 true
   */
  public boolean isTrue(Object key) {
    Object value = get(key);
    return value != null && TypeKit.toBoolean(value);
  }

  /**
   * key 存在，并且 value 为 false，则返回 true
   */
  public boolean isFalse(Object key) {
    Object value = get(key);
    return value != null && !TypeKit.toBoolean(value);
  }

  public boolean equals(Object kv) {
    return kv instanceof Kv && super.equals(kv);
  }

  @SuppressWarnings("unchecked")
  public Kv keep(String... keys) {
    if (keys != null && keys.length > 0) {
      Kv newKv = Kv.create();
      for (String k : keys) {
        if (containsKey(k)) { // 避免将并不存在的变量存为 null
          newKv.put(k, get(k));
        }
      }

      clear();
      putAll(newKv);
    } else {
      clear();
    }

    return this;
  }

  @SuppressWarnings("unchecked")
  public <K, V> Map<K, V> toMap() {
    return this;
  }
}
