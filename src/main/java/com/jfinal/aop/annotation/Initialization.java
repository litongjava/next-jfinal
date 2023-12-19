package com.jfinal.aop.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 只启动,不放入bean中
 * @author 
 *
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Initialization {

  String value() default "";

  /**
   * Startup priority, the smaller the value, the higher the startup priority
   * @return
   */
  int priority() default 100;

}
