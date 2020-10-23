package com.tonbu.web.admin.service;

import com.tonbu.framework.dao.support.Page;

import java.util.Map;

public interface ZznlService {

    Page<?> loadList(Map<String, String> param);

    void updateStatus(Map<String, String> param);

    void save(Map<String, String> param);

    void del(Map<String, String> param);

    Page<?> getPageInformationList(String type, int firstrow, int rowcount);

    Map<String,Object>  getPageInformationById(String id);

}
