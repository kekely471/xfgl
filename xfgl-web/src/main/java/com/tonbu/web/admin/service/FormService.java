package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;

import java.util.List;
import java.util.Map;

public interface FormService {

    Page<?> extendTableListLoad(Map<String, String> param);

    void extendAddOrUpdate(Map<String, String> param);

    ResultEntity extendDel(Map<String, String> param);

    ResultEntity extendRealDel(Map<String, String> param);

    List<Map<String, Object>> formListSelect();

    Page<?> tableListLoad(Map<String, String> param);

    void addOrUpdate(Map<String, String> param);

    ResultEntity del(Map<String, String> param);

    ResultEntity realDel(Map<String, String> param);

    List<Map<String, Object>> parentDirectoryList();

    List<Map<String, Object>> loadFormZTreeList(Map<String, String> param);

    List<Map<String, Object>> positionSelectList(Map<String, String> param);
}