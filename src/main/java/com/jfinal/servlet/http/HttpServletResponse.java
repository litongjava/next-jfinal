package com.jfinal.servlet.http;

import java.io.IOException;
import java.io.PrintWriter;

import com.jfinal.servlet.ServletOutputStream;
import com.litongjava.tio.http.common.HttpResponse;

/**
 * HttpServletResponse 使用太广泛,支持一下,但是内部使用HttpResponse实现
 * @author Tong
 *
 */
public interface HttpServletResponse {

  int SC_NOT_FOUND = 0;
  int SC_MOVED_PERMANENTLY = 0;
  int SC_PARTIAL_CONTENT = 206;

  void setStatus(int errorCode);

  void addCookie(Cookie cookie);

  void setContentType(String contentType);

  PrintWriter getWriter() throws IOException;

  void setHeader(String string, String url);

  void sendRedirect(String url) throws IOException;

  void setCharacterEncoding(String defaultEncoding);

  ServletOutputStream getOutputStream();

  void setDateHeader(String string, int i);

  HttpResponse finish();

}
