package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.GgglService;
import com.tonbu.web.admin.service.ZbztService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ZbztServiceImpl implements ZbztService {

    @Override
    public Page<?> loadList(Map<String, String> param) {
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT  ID," +
                " '消防指挥中心' as NAME," +
                " (SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zbtype' AND BD.VALUE = t.status )STATUS," +
                "   to_char(START_DATE, 'yyyy-MM-dd HH24:mi:ss') AS START_DATE," +
                "   to_char(END_DATE, 'yyyy-MM-dd HH24:mi:ss') AS END_DATE," +
                "   to_char(UPDATE_TIME, 'yyyy-MM-dd HH24:mi:ss') AS UPDATE_TIME" +
                "  from TB_ZBZT t" +
                " where 1 = 1  ");
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }



    @Override
    public void save(Map<String, String> param) {
        String status = param.get("status").equals("") ?  "3" :param.get("status");
            DBHelper.execute("UPDATE TB_ZBZT SET STATUS=?, UPDATE_TIME=SYSDATE WHERE ID=1",
                    status);

    }

    @Override
    public String query() {
        return DBHelper.queryForScalar("SELECT STATUS FROM TB_ZBZT WHERE ID=1",String.class);

    }




}
