package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;

import java.util.Map;

public interface ConfigService {

    ResultEntity addConfig(Map<String, String> param);

    ResultEntity delConfig(Map<String, String> param);

    ResultEntity updateConfig(Map<String, String> param);

    Page<?> queryConfig(Map<String, String> param);
}
