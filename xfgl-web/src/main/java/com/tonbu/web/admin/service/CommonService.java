package com.tonbu.web.admin.service;

import java.util.List;
import java.util.Map;

public interface CommonService {

    List<Object> getTreeList(Map<String, String> param);

    List<Map<String, Object>> loadSSDictList(Map<String, String> param);

    List<Map<String, Object>> loadBusDictList(Map<String, String> param);

    List<Map<String, Object>> loadDeptDictList(Map<String, String> param);

    List<Map<String, Object>> loadJobDictList(Map<String, String> param);

    List<Map<String, Object>> loadkqDeptDictList(Map<String, String> param);

    List<Map<String, Object>> informationTypeList(Map<String, String> param);

    List<Map<String, Object>> zbztTypeList(Map<String, String> param);

    List<Map<String, Object>> loadRoleList();

    List<Map<String, Object>> loadDeptList();

    List<Map<String, Object>> loadDictTypeList(String type);

    List<Map<String, Object>> loadUserList();

    List<Map<String, Object>> loadServiceDirectoryList();

    List<Map<String, Object>> loadSlaList();

    List<Map<String, Object>> loadMenuList();

    List<Map<String, Object>> loadContractList(String dept_id);

    List<Map<String, Object>> loadFlowList();

    List<Map<String, Object>> loadStepList(String flow_id);

    List<Map<String, Object>> loadTree(Map<String, String> param);

    List<Map<String, Object>> loadFlowInfoTree(Map<String, String> param);

    List<Map<String, Object>> loadMenuZTree(Map<String, String> param);

    List<Map<String, Object>> loadTreeList(Map<String, String> param);

    List<Map<String, Object>> loadzTreeList(Map<String, String> param);

    List<Map<String, Object>> getDictTypeList();

    List<Map<String, Object>> getDataByTable(Map<String, String> param);

    List<Map<String, Object>> getRenderingKeyBySSFORMFRAME(Map<String, String> param);

    Integer getFormFrameId();
}

