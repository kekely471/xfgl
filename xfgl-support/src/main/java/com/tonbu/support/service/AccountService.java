package com.tonbu.support.service;

import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.UserEntity;

import java.util.List;
import java.util.Map;

public interface AccountService {

    /**
     * 根据用户名获取
     * @param loginname
     */
    UserEntity findByloginname(String loginname);

    /**
     * 根据用户Id获取用户信息
     * @param userId
     * @return
     */
     UserEntity  findById(String userId);

    Page<?> loadList(Map<String,String> param);

    void save(Map<String,String> param);

    void del(Map<String,String> param);

    void realDel(Map<String,String> param);

    List<Map<String, Object>> getBind();

    List<Map<String, Object>> getBindUser();

    void bind(Map<String, String> param);

    void delBind(Map<String, String> param);

    /**
     * 绑定账号设备
     * @param
     * @return
     */
    void saveAccountDevice( UserEntity reEntity ,Map<String, String> devicetoken);
}
