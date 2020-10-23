package com.tonbu.web.app.itsm.service;

import com.tonbu.framework.dao.support.Page;

import java.util.Map;

public interface FlowManageService {

    /**
     * 列表
     */
    Page<?> loadList(Map<String, String> param);


    /**
     * 修改/保存
     */
    void save(Map<String, String> param);

    /**
     * 删除
     */
    void del(Map<String, String> param);

    /**
     * 删除
     */
    void realDel(Map<String, String> param);




    /**
     * 列表
     */
    Page<?> loadStepList(Map<String, String> param);


    /**
     * 修改/保存
     */
    void saveStep(Map<String, String> param);

    /**
     * 删除
     */
    void delStep(Map<String, String> param);

    /**
     * 删除
     */
    void realDelStep(Map<String, String> param);


}
