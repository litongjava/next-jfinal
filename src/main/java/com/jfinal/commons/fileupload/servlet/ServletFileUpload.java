package com.jfinal.commons.fileupload.servlet;

import java.util.List;

import com.jfinal.commons.fileupload.FileItem;
import com.jfinal.commons.fileupload.FileItemFactory;
import com.jfinal.commons.fileupload.ProgressListener;
import com.jfinal.servlet.http.HttpServletRequest;

public class ServletFileUpload {

  public ServletFileUpload(FileItemFactory factory) {
    // TODO Auto-generated constructor stub
  }

  public static boolean isMultipartContent(HttpServletRequest request) {
    // TODO Auto-generated method stub
    return false;
  }

  public void setProgressListener(ProgressListener progressListener) {
    // TODO Auto-generated method stub
    
  }

  public List<FileItem> parseRequest(HttpServletRequest request) {
    // TODO Auto-generated method stub
    return null;
  }

}
