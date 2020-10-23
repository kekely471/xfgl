package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;

import java.util.Map;

public interface KqglService {
    Page<?> loadList(Map<String,String> param);
}
