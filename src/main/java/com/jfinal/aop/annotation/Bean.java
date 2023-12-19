package com.jfinal.aop.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {

  String value() default "";
  
  String destroyMethod() default "";

  String initMethod() default "";
  
  /**
   * Startup priority, the smaller the value, the higher the startup priority
   * @return
   */
  int priority() default 100;

}
