package com.tonbu.web.admin.service.important;

import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.UserEntity;

import java.util.List;
import java.util.Map;

public interface RyfxMoblieService {


    /**
     * 新增人员分析保存或者更新
     * @param param
     * @return
     */
//    int saveOrUpdate(Map<String, String> param);

    /**
     * 手机端未提交列表
     * @param param
     * @return
     */
    Page<?> findwtjryfxPage(Map<String, String> param);
    /**
     * 手机端人员归档详情
     * @param param
     * @return
     */
    Page<?> getrygd(Map<String, String> param);
    /**
     * 手机端人员分析详情
     * @param param
     * @return
     */
    Page<?> getryfx(Map<String, String> param);
    /**
     * 手机端已提交列表
     * @param param
     * @return
     */
    Page<?> findytjryfxPage(Map<String, String> param);
    /**
     * 人员分析删除
     * @param param
     */
    void del(Map<String, String> param);

    /**
     * 审核（归档退回）人员分析
     * @param param
     * @return
     */
     void ryfxsh(Map<String, String> param);

    /**
     * 提交
     */
    void tj(Map<String, String> param);

    /**
     * 保存
     * @param param
     */
    void bc(Map<String, String> param);

    /**
     * 同步上月数据
     */
    List<Map<String, Object>> findusers(Map<String, String> param);


    /**
     * 手机端未归档列表
     * @param param
     * @return
     */
    Page<?> findwgdryfxPage(Map<String, String> param);


    /**
     * 手机端已归档列表
     * @param param
     * @return
     */
    Page<?> findygdryfxPage(Map<String, String> param);
    /**
     *权限
     */

    List<Map<String, Object>> qx(Map<String, String> param);
}
