package com.jfinal.model;

import java.lang.reflect.Method;

public class DestroyableBean {
  private Object bean;
  private Method destroyMethod;

  public DestroyableBean(Object bean, Method destroyMethod) {
    this.bean = bean;
    this.destroyMethod = destroyMethod;
  }

  public Object getBean() {
    return bean;
  }

  public Method getDestroyMethod() {
    return destroyMethod;
  }
}