package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;

import java.util.List;
import java.util.Map;

public interface InvoiceService {
    Page<?> tableListLoad(Map<String, String> param);

    void addOrUpdate(Map<String, String> param);

    ResultEntity del(Map<String, String> param);

    ResultEntity realDel(Map<String, String> param);

    List<Map<String, Object>> getRenderingKey(Map<String, String> param);
}
