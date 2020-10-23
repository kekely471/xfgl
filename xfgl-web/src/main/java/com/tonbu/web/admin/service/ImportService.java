package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;

import java.util.List;
import java.util.Map;

public interface ImportService {

    //列表页面
    Page<?> loadList(Map<String, String> param);

    void saveLeave(Map<String,String> param);

    //根据查看详情
    //List<Map<String,Object>> getById(Map<String,String> param);

    List<Map<String,Object>> getByIdForLeave(Map<String,String> param);

    //保存
    void save(Map<String,String> param);

    //删除
    void del(Map<String,String> param);

    //提交
    void tj(Map<String,String> param);

    /*//查找选中日期是否有数据
    List<Map<String,Object>> getLeave(Map<String,String> param);*/
}
