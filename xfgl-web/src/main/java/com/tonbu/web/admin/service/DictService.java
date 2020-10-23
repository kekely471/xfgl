package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;

import java.util.Map;

public interface DictService {

    Page<?> loadTypeList(Map<String, String> param);

    void saveType(Map<String, String> param);

    void realDelType(Map<String, String> param);

    Page<?> loadSysList(Map<String, String> param);

    void saveSys(Map<String, String> param);

    void delSys(Map<String, String> param);

    void realDelSys(Map<String, String> param);

    Page<?> loadBusinessList(Map<String, String> param);

    void saveBusiness(Map<String, String> param);

    void delBusiness(Map<String, String> param);

    void realDelBusiness(Map<String, String> param);

}
