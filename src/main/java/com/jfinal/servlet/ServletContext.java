package com.jfinal.servlet;

public class ServletContext {
  private String contextPath = null;

  public String getContextPath() {
    return contextPath;
  }

  public String getRealPath(String string) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getMimeType(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public void setContextPath(String contextPath) {
    this.contextPath=contextPath;
  }

}
