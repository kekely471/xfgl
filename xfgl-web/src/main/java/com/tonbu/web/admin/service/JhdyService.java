package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;

import java.util.Map;

public interface JhdyService {

    //问题查看
    Page<?> wtckList(Map<String,String> param);

    //手机端问题查问题详情
    Page<?> getwtck(Map<String,String> param);

    //问题管理
    Page<?> wtglList(Map<String,String> param);

    //手机端问题管理
    Page<?> wtglList2(Map<String,String> param);

    //手机端问题管理查看详情
    Page<?> getwtgl(Map<String,String> param);

    //问题管理保存
    void wtglsave(Map<String, String> param);

    //问题管理删除
    void wtgldel(Map<String, String> param);


    //问题分析
    Page<?> wtfxList(Map<String,String> param);
    //手机端问题分析
    Page<?> wtfxList2(Map<String,String> param);

    //手机端问题分析查看详情
    Page<?> getwtfx(Map<String,String> param);

    //问题分析保存
    void wtfxsave(Map<String, String> param);

}
