package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.web.admin.service.LbtService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
public class LbtServiceImpl implements LbtService{
    @Override
    public Page<?> loadList(Map<String, String> param) {
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("select * from (SELECT ID,\n" +
                "       TITLE,\n" +
                "       DESCRIBE,\n" +
                "       URL,\n" +
                "       USER_ID,\n" +
                "       TYPE,\n" +
                "       TUPIAN,\n" +
                "       FILE_ID,\n" +
                "       to_char(CREATE_DATE, 'yyyy-MM-dd HH24:mi:ss') AS CREATE_DATE,\n" +
                "       to_char(UPDATE_TIME, 'yyyy-MM-dd HH24:mi:ss') AS UPDATE_TIME,\n" +
                "       DETAILS \n" +
                "  from TB_CAROUSEL\n" +
                " where 1 = 1 and STATUS<>0 \n" +
                " order by CREATE_DATE desc)where rownum<6");
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }
}
