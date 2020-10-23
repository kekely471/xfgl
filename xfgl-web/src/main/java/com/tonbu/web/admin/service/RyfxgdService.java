package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;

import java.util.Map;

public interface RyfxgdService {

    //查询所有归档数据
    Page<?> findrygdPage(Map<String,String> param);

    //归档按钮
    void ryfxgd(Map<String, String> param);
}
