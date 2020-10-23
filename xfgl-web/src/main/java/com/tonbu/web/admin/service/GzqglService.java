package com.tonbu.web.admin.service;


import com.tonbu.framework.dao.support.Page;

import java.util.Map;

public interface GzqglService {

    Page<?> loadList(Map<String,String> param);

    void del(Map<String,String> param);

    Page<?> loadPlList(Map<String,String> param);

    void pldel(Map<String,String> param);

}
