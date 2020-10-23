package com.tonbu.web.admin.service;


import com.tonbu.framework.dao.support.Page;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface MeetingService {

    /**
     * 会议列表
     * @param param 参数
     * @return Page
     */
    Page<?> loadList(Map<String, String> param);

    /**
     * 我的会议列表
     * @param param 参数
     * @return Page
     */
    Page<?> loadMyList(Map<String, String> param);

    /**
     * 保存/修改通知
     * @param param 参数
     */
    void save(Map<String, String> param) throws ParseException;

    /**
     * 消息彻底删除
     * @param param 参数
     */
    void realDel(Map<String, String> param);

    /**
     * 修改查看状态
     * @param param 参数
     */
    void update(Map<String, String> param);
}
