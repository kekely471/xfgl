package com.tonbu.web.app.itsm.service;

import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.UserEntity;

import java.util.List;
import java.util.Map;

public interface ImportantPojoService {

    Page<?> loadList(Map<String, String> param);

    Map<String, Object> getById(Map<String,String> param);

    List<Map<String,Object>> getByIdForLeave(Map<String,String> param);

    void saveLeave(Map<String,String> param);


    Map<String, Object> getUserInfoObjectMap(Map<String, String> param);
}
