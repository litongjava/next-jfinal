/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.jfinal.aop;

import java.lang.reflect.Method;

/**
 * InvocationWrapper invoke the InterceptorStack.
 */
class InvocationWrapper extends Invocation {

  private Interceptor[] inters;
  private Invocation invocation;
  private int index = 0;

  InvocationWrapper(Invocation invocation, Interceptor[] inters) {
    this.invocation = invocation;
    this.inters = inters;
  }

  /**
   * Invoke the action
   */
  @Override
  public final Object invoke() {
    if (index < inters.length) {
      inters[index++].intercept(this);
    } else if (index++ == inters.length) {
      return invocation.invoke();
    }

    return null;
  }

  @Override
  public Object getArg(int index) {
    return invocation.getArg(index);
  }

  @Override
  public Object[] getArgs() {
    return invocation.getArgs();
  }

  @Override
  public void setArg(int index, Object value) {
    invocation.setArg(index, value);
  }

  /**
   * Get the target object which be intercepted
   * <pre>
   * Example:
   * OrderService os = getTarget();
   * </pre>
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> T getTarget() {
    return (T) invocation.getTarget();
  }

  /**
   * Return the method of this action.
   * <p>
   * You can getMethod.getAnnotations() to get annotation on action method to do more things
   */
  @Override
  public Method getMethod() {
    return invocation.getMethod();
  }

  /**
   * Return the method name of this action's method.
   */
  @Override
  public String getMethodName() {
    return invocation.getMethodName();
  }

  /**
   * Get the return value of the target method
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> T getReturnValue() {
    return (T) invocation.getReturnValue();
  }

  /**
   * Set the return value of the target method
   */
  @Override
  public void setReturnValue(Object returnValue) {
    invocation.setReturnValue(returnValue);
  }
}
