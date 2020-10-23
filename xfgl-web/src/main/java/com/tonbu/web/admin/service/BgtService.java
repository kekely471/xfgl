package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;

import java.util.Map;

public interface BgtService {

    Page<?> wtsbList(Map<String, String> param);

    Page<?> loadList(Map<String, String> param);

    void updateStatus(Map<String, String> param);

    void save(Map<String, String> param);

    void del(Map<String, String> param);

    Page<?> getPageInformationList(String type, int firstrow, int rowcount);

    Map<String, Object> getPageInformationById(String id);

    //保存
    void bc(Map<String,String> param);

    //删除问题上报
    void wtsb(Map<String,String> param);

    //手机端查看详情
    Page<?> getwtsb(Map<String,String> param);

}
