package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;

import java.util.List;
import java.util.Map;

public interface FormFrameService {
    List extendTableListLoad(Map<String, String> param);

    void extendAddOrUpdate(Map<String, String> param);

    ResultEntity extendDel(Map<String, String> param);

    ResultEntity extendRealDel(Map<String, String> param);

    Page<?> newExtendTableListLoad(Map<String, String> param);

    int checkTableNameIsExist(String tablename) ;
}
