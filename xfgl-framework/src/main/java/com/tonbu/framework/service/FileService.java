package com.tonbu.framework.service;

import java.util.List;
import java.util.Map;

public interface FileService {


    List<Map<String, Object>> loadFileList(Map<String, String> param);

    void Upload(Map<String, String> param);

    void UploadBatch(List<Map<String, String>> param);

    Map<String, Object> download(String id);

    Map<String, Object> getFileInfo(Map<String, String> param);

    void UploadBatchBytes(List<Map<String, Object>> param);

    Map<String, Object> downloadBytes(String id) ;

    }
