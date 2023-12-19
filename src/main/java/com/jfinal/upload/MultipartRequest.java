/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.jfinal.upload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.servlet.http.HttpServletRequest;
import com.jfinal.servlet.multipart.DefaultFileRenamePolicy;
import com.jfinal.servlet.multipart.FileRenamePolicy;

/**
 * MultipartRequest.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MultipartRequest extends com.jfinal.servlet.http.HttpServletRequestWrapper {

  static FileRenamePolicy fileRenamePolicy = new DefaultFileRenamePolicy() {
    @Override
    public File rename(File f) {
      String name = f.getName();
      int lastIndexOf = name.lastIndexOf(".");
      if (lastIndexOf > -1) {
        String suffix = name.substring(lastIndexOf).toLowerCase().trim();
        if (".jsp".equals(suffix) || ".jspx".equals(suffix)) {
          File safeFile = new File(f.getParent(), name + "_unsafe");
          return super.rename(safeFile);
        }
      }
      return super.rename(f);
    }
  };

  private List<UploadFile> uploadFiles;
  private com.jfinal.servlet.multipart.MultipartRequest multipartRequest;

  public MultipartRequest(HttpServletRequest request, String uploadPath, long maxPostSize, String encoding) {
    super(request);
    wrapMultipartRequest(request, getFinalPath(uploadPath), maxPostSize, encoding);
  }

  public MultipartRequest(HttpServletRequest request, String uploadPath, long maxPostSize) {
    super(request);
    wrapMultipartRequest(request, getFinalPath(uploadPath), maxPostSize, UploadConfig.encoding);
  }

  public MultipartRequest(HttpServletRequest request, String uploadPath) {
    super(request);
    wrapMultipartRequest(request, getFinalPath(uploadPath), UploadConfig.maxPostSize, UploadConfig.encoding);
  }

  public MultipartRequest(HttpServletRequest request) {
    super(request);
    wrapMultipartRequest(request, UploadConfig.baseUploadPath, UploadConfig.maxPostSize, UploadConfig.encoding);
  }

  /**
   * 路径允许为 "" 值，表示直接使用基础路径 baseUploadPath
   */
  private String getFinalPath(String uploadPath) {
    if (uploadPath == null) {
      throw new IllegalArgumentException("uploadPath can not be null.");
    }

    uploadPath = uploadPath.trim();
    if (uploadPath.startsWith("/") || uploadPath.startsWith("\\")) {
      if (UploadConfig.baseUploadPath.equals("/")) {
        return uploadPath;
      } else {
        return UploadConfig.baseUploadPath + uploadPath;
      }
    } else {
      return UploadConfig.baseUploadPath + File.separator + uploadPath;
    }
  }

  private void wrapMultipartRequest(HttpServletRequest request, String uploadPath, long maxPostSize, String encoding) {
    File dir = new File(uploadPath);
    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        throw new RuntimeException("Directory " + uploadPath + " not exists and can not create directory.");
      }
    }

//		String content_type = request.getContentType();
//        if (content_type == null || content_type.indexOf("multipart/form-data") == -1) {
//        	throw new RuntimeException("Not multipart request, enctype=\"multipart/form-data\" is not found of form.");
//        }

    uploadFiles = new ArrayList<UploadFile>();

    try {
      multipartRequest = new com.jfinal.servlet.multipart.MultipartRequest(request, uploadPath, maxPostSize, encoding,
          fileRenamePolicy);
      Enumeration files = multipartRequest.getFileNames();
      while (files.hasMoreElements()) {
        String name = (String) files.nextElement();
        String filesystemName = multipartRequest.getFilesystemName(name);

        // 文件没有上传则不生成 UploadFile, 这与 cos的解决方案不一样
        if (filesystemName != null) {
          String originalFileName = multipartRequest.getOriginalFileName(name);
          String contentType = multipartRequest.getContentType(name);
          UploadFile uploadFile = new UploadFile(name, uploadPath, filesystemName, originalFileName, contentType);
          if (isSafeFile(uploadFile)) {
            uploadFiles.add(uploadFile);
          }
        }
      }
    } catch (com.jfinal.servlet.multipart.ExceededSizeException e) {
      throw new ExceededSizeException(e);
    }
  }

  private boolean isSafeFile(UploadFile uploadFile) {
    String fileName = uploadFile.getFileName().trim().toLowerCase();
    if (fileName.endsWith(".jsp") || fileName.endsWith(".jspx")) {
      uploadFile.getFile().delete();
      return false;
    }
    return true;
  }

  public List<UploadFile> getFiles() {
    return uploadFiles;
  }

  /**
   * Methods to replace HttpServletRequest methods
   */
  public Enumeration getParameterNames() {
    return multipartRequest.getParameterNames();
  }

  public String getParameter(String name) {
    return multipartRequest.getParameter(name);
  }

  public String[] getParameterValues(String name) {
    return multipartRequest.getParameterValues(name);
  }

  public Map getParameterMap() {
    Map map = new HashMap();
    Enumeration enumm = getParameterNames();
    while (enumm.hasMoreElements()) {
      String name = (String) enumm.nextElement();
      map.put(name, multipartRequest.getParameterValues(name));
    }
    return map;
  }
}
