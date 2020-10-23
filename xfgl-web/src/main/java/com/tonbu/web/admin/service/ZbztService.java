package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;

import java.util.Map;

public interface ZbztService {

    Page<?> loadList(Map<String, String> param);

    void save(Map<String, String> param);

    String query();

}
