package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.UserEntity;

import java.util.List;
import java.util.Map;

public interface RyglService {


    Page<?> loadList(Map<String,String> param);

    void save(Map<String,String> param);

    void del(Map<String,String> param);

    String select(Map<String,String> param);
}
