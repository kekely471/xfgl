package com.tonbu.web.admin.service;

import com.tonbu.framework.data.UserEntity;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


public interface DqService {
    /**
     * 打卡
     * @param param
     * @param u
     */
    String Dq(Map<String,String> param, UserEntity u, HttpServletRequest request);

    /**
     * 获取打卡详情
     * @param param
     * @return
     */
    List<Map<String, Object>> getDq(Map<String,String> param, UserEntity u);

    /**
     * 修改个人信息接口
     * @param param
     */
    void save(Map<String,String> param);
    /**
     * 抽查打卡
     * @param param
     * @param u
     */
    String spotDq(Map<String,String> param, UserEntity u, HttpServletRequest request);

}
