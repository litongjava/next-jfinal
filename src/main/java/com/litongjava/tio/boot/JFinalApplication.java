package com.litongjava.tio.boot;

import com.jfinal.aop.Aop;
import com.litongjava.tio.boot.context.Context;
import com.litongjava.tio.boot.context.JFinalApplicationContext;

public class JFinalApplication {

  public static Context run(Class<?> primarySource, String[] args) {
    return run(new Class<?>[] { primarySource }, args);
  }

  public static Context run(Class<?>[] primarySources, String[] args) {
    Context context = Aop.get(JFinalApplicationContext.class);
    return context.run(primarySources, args);
  }

}
