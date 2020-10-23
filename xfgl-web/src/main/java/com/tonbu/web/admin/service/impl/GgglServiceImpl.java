package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.GgglService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
public class GgglServiceImpl implements GgglService{

    @Override
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("select * from (SELECT ID,\n" +
                "       TITLE,\n" +
                "       DESCRIBE,\n" +
                "       URL,\n" +
                "       USER_ID,\n" +
                "       (SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'sf' AND BD.VALUE = t.type AND t.STATUS<>-1)TYPE,\n" +
                "       TUPIAN,\n" +
                "       FILE_ID,\n" +
                "       STATUS,\n" +
                "       to_char(CREATE_DATE, 'yyyy-MM-dd HH24:mi:ss') AS CREATE_DATE,\n" +
                "       to_char(UPDATE_TIME, 'yyyy-MM-dd HH24:mi:ss') AS UPDATE_TIME,\n" +
                "       DETAILS \n" +
                "  from TB_CAROUSEL t\n" +
                " where 1 = 1  \n" +
                " order by CREATE_DATE desc)where rownum<6");
        if(StringUtils.isNotBlank(key)){
            sql.append(" and  TITLE  like ? ");
            args.add("%" + key + "%");
        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());

    }

    @Override
    public void save(Map<String, String> param) {
        String id = param.get("id");
        String title = param.get("bt");
        String describe = param.get("ms");
        String url = param.get("url");
//        String user_id = param.get("user_id");
        String details = param.get("nr");
        String tupian = param.get("userFileIdAVATAR_filepath");
        String file_id = param.get("avatar");
        String type = param.get("type");
        String status = param.get("status") == null ? "0" : "1";
        UserEntity u = CustomRealm.GetLoginUser();
        if (StringUtils.isBlank(id)) {// 新增
            DBHelper.execute("INSERT INTO TB_CAROUSEL(ID,TITLE,DESCRIBE,URL,USER_ID,CREATE_DATE,DETAILS,TUPIAN,FILE_ID,TYPE,UPDATE_TIME,STATUS) " +
                            "VALUES(SEQ_TB_CAROUSEL.NEXTVAL,?,?,?,?,SYSDATE,?,?,?,?,SYSDATE,?)",
                    title,describe,url,u.getUsername(),details,tupian,file_id,type,status);
        } else {// 修改
            DBHelper.execute("UPDATE TB_CAROUSEL SET TITLE=?,DESCRIBE=?,URL=?,USER_ID=?,DETAILS=?,TUPIAN=?,FILE_ID=?,TYPE=?,UPDATE_TIME=SYSDATE,STATUS=? WHERE ID=?",
                    title,describe,url,u.getUsername(),details,tupian,file_id,type,status,id);
        }
    }

    @Override
    public void del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split(",");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute(" delete from TB_CAROUSEL where id=?",id_s[i]);
        }
    }
}
