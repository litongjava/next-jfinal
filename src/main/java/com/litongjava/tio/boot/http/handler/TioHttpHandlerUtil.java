package com.litongjava.tio.boot.http.handler;

import java.lang.reflect.Method;

import com.litongjava.tio.http.common.HttpConfig;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.RequestLine;
import com.litongjava.tio.utils.hutool.StrUtil;
import com.litongjava.tio.utils.hutool.Validator;

/**
 * Created by litonglinux@qq.com on 11/9/2023_2:22 AM
 */
public class TioHttpHandlerUtil {
  public static Method getActionMethod(HttpConfig httpConfig, TioBootHttpRoutes routes, HttpRequest request,
      RequestLine requestLine) {
    Method method = null;
    String path = requestLine.path;
    if (routes != null) {
      method = routes.getMethodByPath(path, request);
      path = requestLine.path;
    }
    if (method == null) {
      if (StrUtil.isNotBlank(httpConfig.getWelcomeFile())) {
        if (StrUtil.endWith(path, "/")) {
          path = path + httpConfig.getWelcomeFile();
          requestLine.setPath(path);

          if (routes != null) {
            method = routes.getMethodByPath(path, request);
            path = requestLine.path;
          }
        }
      }
    }

    return method;
  }

  public static String getDomain(HttpRequest request) {
    String domain = request.getDomain();

    boolean isip = Validator.isIpv4(domain);
    if (!isip) {
      String[] dms = StrUtil.split(domain, ".");
      if (dms.length > 2) {
        domain = "." + dms[dms.length - 2] + "." + dms[dms.length - 1];
      }
    }
    return domain;
  }

}
