package com.jfinal.servlet.multipart;

import java.util.Enumeration;

import com.jfinal.servlet.http.HttpServletRequest;

public class MultipartRequest {

  public MultipartRequest(HttpServletRequest request, String uploadPath, long maxPostSize, String encoding,
      FileRenamePolicy fileRenamePolicy) {
    // TODO Auto-generated constructor stub
  }

  @SuppressWarnings("rawtypes")
  public Enumeration getFileNames() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getFilesystemName(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getOriginalFileName(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getContentType(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public Enumeration getParameterNames() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getParameter(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public String[] getParameterValues(String name) {
    // TODO Auto-generated method stub
    return null;
  }

}
