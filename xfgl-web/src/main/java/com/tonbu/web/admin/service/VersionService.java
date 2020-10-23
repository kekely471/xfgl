package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;

import java.util.Map;

public interface VersionService {

    Page<?> loadList(Map<String, String> param);

    ResultEntity addInfo(Map<String, String> param);

    ResultEntity updateInfo(Map<String, String> param);

    ResultEntity relDeleteByIdList(Map<String, String> param);
}
