package com.jfinal.servlet.http;

import java.util.Enumeration;

import com.jfinal.servlet.ServletContext;

public interface HttpSession {

  public Object getAttribute(String key);

  public void setAttribute(String key, Object value) ;

  public void removeAttribute(String key);

  public Enumeration<String> getAttributeNames();

  public long getCreationTime();

  public String getId();

  public long getLastAccessedTime();

  public int getMaxInactiveInterval();

  public ServletContext getServletContext();

  public HttpSessionContext getSessionContext();

  public Object getValue(String key);

  public String[] getValueNames();

  public void invalidate();

  public boolean isNew();

  public void putValue(String key, Object value);

  public void removeValue(String key);

  public void setMaxInactiveInterval(int maxInactiveInterval);

}
