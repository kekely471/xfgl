package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;

import java.util.Map;

public interface YdxfService {
    Page<?> loadList(Map<String, String> param);

    void save(Map<String,String> param);
}
