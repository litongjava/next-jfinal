package com.jfinal.servlet.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import com.jfinal.servlet.AsyncContext;
import com.jfinal.servlet.DispatcherType;
import com.jfinal.servlet.RequestDispatcher;
import com.jfinal.servlet.ServletContext;
import com.jfinal.servlet.ServletException;
import com.jfinal.servlet.ServletInputStream;
import com.jfinal.servlet.ServletRequest;
import com.jfinal.servlet.ServletResponse;
import com.jfinal.servlet.multipart.Part;

/**
 * HttpServletRequest 使用太广泛,支持一下,但是内部使用HttpRequest实现
 * @author Tong Li
 *
 */
public interface HttpServletRequest {

  Map<String, String[]> getParameterMap();

  String getParameter(String name);

  /**
   * 该方法将触发 createParaMap()，框架内部应尽可能避免该事情发生，以优化性能
   */
  String[] getParameterValues(String name);

  Enumeration<String> getParameterNames();

  ServletInputStream getInputStream() throws IOException;

  BufferedReader getReader() throws IOException;

  Object getAttribute(String name);

  Enumeration<String> getAttributeNames();

  String getCharacterEncoding();

  void setCharacterEncoding(String env) throws UnsupportedEncodingException;

  int getContentLength();

  long getContentLengthLong();

  String getContentType();

  String getProtocol();

  String getScheme();

  String getServerName();

  int getServerPort();

  String getRemoteAddr();

  String getRemoteHost();

  void setAttribute(String name, Object o);

  void removeAttribute(String name);

  Locale getLocale();

  Enumeration<Locale> getLocales();

  boolean isSecure();

  RequestDispatcher getRequestDispatcher(String path);

  String getRealPath(String path);

  int getRemotePort();

  String getLocalName();

  String getLocalAddr();

  int getLocalPort();

  ServletContext getServletContext();

  AsyncContext startAsync() throws IllegalStateException;

  AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException;

  boolean isAsyncStarted();

  boolean isAsyncSupported();

  AsyncContext getAsyncContext();

  DispatcherType getDispatcherType();

  String getAuthType();

  Cookie[] getCookies();

  long getDateHeader(String name);

  String getHeader(String name);

  Enumeration<String> getHeaders(String name);

  Enumeration<String> getHeaderNames();

  int getIntHeader(String name);

  String getMethod();

  String getPathInfo();

  String getPathTranslated();

  String getContextPath();

  String getQueryString();

  String getRemoteUser();

  boolean isUserInRole(String role);

  Principal getUserPrincipal();

  String getRequestedSessionId();

  String getRequestURI();

  StringBuffer getRequestURL();

  String getServletPath();

  HttpSession getSession(boolean create);

  HttpSession getSession();

  String changeSessionId();

  boolean isRequestedSessionIdValid();

  boolean isRequestedSessionIdFromCookie();

  boolean isRequestedSessionIdFromURL();

  boolean isRequestedSessionIdFromUrl();

  boolean authenticate(HttpServletResponse response) throws IOException;

  void login(String username, String password);

  void logout();

  Collection<Part> getParts() throws IOException;

  Part getPart(String name) throws IOException, ServletException;

  <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException;

}
