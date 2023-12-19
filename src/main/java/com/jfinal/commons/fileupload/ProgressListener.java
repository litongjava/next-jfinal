package com.jfinal.commons.fileupload;

public interface ProgressListener {

  void update(long bytesRead, long contentLength, int items);
}
