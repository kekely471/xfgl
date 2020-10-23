package com.tonbu.web.app.itsm.service;

import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.UserEntity;

import java.util.List;
import java.util.Map;

public interface GzqService {

    Page<?> loadList(Map<String,String> param,UserEntity u);

    /**
     * 个人工作圈列表
     * @param param
     * @param u
     * @return
     */
    Page<?> loadListxq(Map<String,String> param,UserEntity u);

    void save(Map<String, String> param, UserEntity u) ;

    void delete(Map<String,String> param, UserEntity u);

    Map<String, Object> get(Map<String,String> param, UserEntity u);

    /**
     * 新增评论
     */
    void savepl(Map<String, String> param, UserEntity u) ;
    /**
     * 删除评论
    */
    void delpl(Map<String,String> param, UserEntity u);

    /**
     * 点赞/取消点赞
     */
    void savedz(Map<String, String> param, UserEntity u) ;

}
