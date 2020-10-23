package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.UserEntity;

import java.util.Map;

public interface LogService {

    Page<?> loadLoginLogs(Map<String,String> param);

    Page<?> loadOpLogs(Map<String,String> param);

    void realDelLogin(Map<String,String> param);

    void realDelOp(Map<String,String> param);

    Page<?> loadBusinessLogs(Map<String,String> param);

}
