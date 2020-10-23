package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;

import java.util.List;
import java.util.Map;

public interface ImportCheckService {
    //列表页面
    Page<?> loadList(Map<String, String> param);

    void saveLeave(Map<String,String> param);

    List<Map<String,Object>> getByIdForLeave(Map<String,String> param);
}
