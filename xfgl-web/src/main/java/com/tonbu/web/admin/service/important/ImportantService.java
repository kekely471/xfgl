package com.tonbu.web.admin.service.important;

import com.tonbu.framework.dao.support.Page;

import java.util.List;
import java.util.Map;

public interface ImportantService {
    /**
     * 要事登记列表
     * @param param
     * @return
     */
    Page<?> findImportantPage(Map<String, String> param);

    /**
     * 要事登记保存或者更新
     * @param param
     * @return
     */
    int saveOrUpdate(Map<String, String> param);

    /**
     * 要事逻辑删除
     * @param param
     */
    void del(Map<String, String> param);

    /**
     * 查询审核人员
     * @param param
     * @return
     */
    List<Map<String, Object>> findAuditUsers(Map<String, String> param);

    /**
     * 待审核要事登记列表
     * @param param
     * @return
     */
    Page<?> findImportantHanderPage(Map<String, String> param);

    /**
     * 审核要事登记
     * @param param
     * @return
     */
    int auditImportant(Map<String, String> param);

    /**
     * 手机端要事登记列表
     * @param param
     * @return
     */
    Page<?> findImportantMobilePage(Map<String, String> param);

    /**
     * 根据用户tonken查找角色
     * @param param
     * @return
     */
    Integer findJobByUserID(Map<String, String> param);

    /**
     * 手机端根据用户id查找登记列表
     * @param param
     * @return
     */
    Page<?> findImportantPageByStatus(Map<String, String> param);

    /**
     * 手机端要事登记归档列表
     * @param param
     * @return
     */
    Page<?> gDlist(Map<String, String> param);

    Map<String, Object> findImportantById(Map<String, String> param);

    Integer auditImportantById(Map<String, String> param);


    Page<?> findImportantPcPage(Map<String, String> param);
}
