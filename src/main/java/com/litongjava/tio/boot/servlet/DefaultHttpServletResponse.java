package com.litongjava.tio.boot.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import com.jfinal.servlet.ServletOutputStream;
import com.jfinal.servlet.http.Cookie;
import com.jfinal.servlet.http.HttpServletResponse;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;

public class DefaultHttpServletResponse implements HttpServletResponse {
  private HttpResponse httpResponse;
  private ServletOutputStream outputStream;
  private PrintWriter printWriter;

  public HttpResponse getHttpResponse() {
    return httpResponse;
  }

  public void setHttpResponse(HttpResponse httpResponse) {
    this.httpResponse = httpResponse;
  }

  public DefaultHttpServletResponse(HttpRequest request) {
    httpResponse = new HttpResponse(request);
  }

  @Override
  public void setStatus(int errorCode) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addCookie(Cookie cookie) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setContentType(String contentType) {
    this.httpResponse.setContentType(contentType);

  }

  @Override
  public void setHeader(String string, String url) {
    // TODO Auto-generated method stub

  }

  @Override
  public void sendRedirect(String url) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void setCharacterEncoding(String defaultEncoding) {
    // TODO Auto-generated method stub

  }

  @Override
  public ServletOutputStream getOutputStream() {
    if (outputStream == null) {
      outputStream = new ServletOutputStream();
    }

    return outputStream;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (outputStream == null) {
      // 创建一个 ByteArrayOutputStream
      outputStream = new ServletOutputStream();
      // 将 PrintWriter 关联到 ByteArrayOutputStream
      printWriter = new PrintWriter(outputStream);
    } else {
      throw new IOException("output stream already opened");
    }
    return printWriter;
  }

  @Override
  public void setDateHeader(String string, int i) {
    // TODO Auto-generated method stub

  }

  public HttpResponse finish() {
    if (printWriter != null) {
      printWriter.flush();
    }
    if (outputStream != null) {
      // 将输出流转换为字节数组
      byte[] bytes = outputStream.toByteArray();
      httpResponse.setBody(bytes);
      
    }
    return httpResponse;
    
  }

}
