package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;

import java.util.List;
import java.util.Map;

public interface RyfxtjService {

    //查询提交列表
    Page<?> findryfxPage(Map<String, String> param);

    //保存
    void bc(Map<String, String> param);

    //提交
    void tj(Map<String, String> param);

    //同步上季度数据
    List<Map<String, Object>> findusers(Map<String, String> param);

    //删除数据
    void del(Map<String, String> param);
}
