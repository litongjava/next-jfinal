package com.litongjava.tio.boot.http.handler;

import com.jfinal.handler.Handler;
import com.jfinal.servlet.http.HttpServletRequest;
import com.jfinal.servlet.http.HttpServletResponse;
import com.litongjava.tio.boot.http.interceptor.DefaultHttpServerInterceptor;
import com.litongjava.tio.boot.servlet.DefaultHttpServletRequest;
import com.litongjava.tio.boot.servlet.DefaultHttpServletResponse;
import com.litongjava.tio.http.common.HttpConfig;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.common.RequestLine;
import com.litongjava.tio.http.common.handler.HttpRequestHandler;
import com.litongjava.tio.http.server.util.Resps;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JFinalHttpRequestHandler implements HttpRequestHandler {
  private HttpConfig httpConfig;
  private Handler handler;
  private int contextPathLength;

  public JFinalHttpRequestHandler(HttpConfig httpConfig, DefaultHttpServerInterceptor defaultHttpServerInterceptor) {
    this.httpConfig = httpConfig;
    String contextPath = httpConfig.getContextPath();
    this.contextPathLength = contextPath != null && !"/".equals(contextPath) ? contextPath.length() : 0;
  }

  @Override
  public HttpResponse handler(HttpRequest request) throws Exception {
    RequestLine requestLine = request.getRequestLine();

    HttpServletRequest servletRequest = new DefaultHttpServletRequest(request);
    HttpServletResponse servletResponse = new DefaultHttpServletResponse(request);

    String target = servletRequest.getRequestURI();
    if (this.contextPathLength != 0) {
      target = target.substring(this.contextPathLength);
    }

    boolean[] isHandled = new boolean[] { false };

    try {
      this.handler.handle(target, servletRequest, servletResponse, isHandled);
      return servletResponse.finish();
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        String qs = servletRequest.getQueryString();
        log.error(qs == null ? target : target + "?" + qs, e);
        return this.resp500(request, requestLine, e);
      }
    }

    if (!isHandled[0]) {
      return this.resp404(request, requestLine);
    }
    return null;
  }

  @Override
  public HttpResponse resp404(HttpRequest request, RequestLine requestLine) throws Exception {
//    if (httpRoutes != null) {
//      String page404 = httpConfig.getPage404();
//      RouteHandler handler = httpRoutes.find(page404);
//      if (handler != null) {
//        return handler.handle(request);
//      }
//    }

    return Resps.resp404(request, requestLine, httpConfig);

  }

  @Override
  public HttpResponse resp500(HttpRequest request, RequestLine requestLine, Throwable throwable) throws Exception {

//    if (throwableHandler != null) {
//      return throwableHandler.handler(request, requestLine, throwable);
//    }

//    if (httpRoutes != null) {
//      String page404 = httpConfig.getPage404();
//      RouteHandler handler = httpRoutes.find(page404);
//      if (handler != null) {
//        return handler.handle(request);
//      }
//    }

    return Resps.resp500(request, requestLine, httpConfig, throwable);
  }

  @Override
  public HttpConfig getHttpConfig(HttpRequest request) {
    return httpConfig;
  }

  @Override
  public void clearStaticResCache() {

  }

  public void setHandler(Handler handler) {
    this.handler = handler;
  }

}
