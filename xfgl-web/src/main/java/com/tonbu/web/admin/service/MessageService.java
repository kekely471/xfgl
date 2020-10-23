package com.tonbu.web.admin.service;


import com.tonbu.framework.dao.support.Page;

import java.util.List;
import java.util.Map;

public interface MessageService {

    Page<?> loadList(Map<String, String> param);

    void save(Map<String, String> param);

    void del(Map<String, String> param);

    void realDel(Map<String, String> param);

    List<Map<String, Object>> loadUserList();

    void update(String id);
}
