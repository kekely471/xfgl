package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.TzggService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TzggServiceImpl implements TzggService {

    @Override
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        String informationtype = param.get("informationtype");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.ID," +
                "      t.TITLE," +
                "      t.DESCRIBE," +
                "       t.URL," +
                "       u.NAME," +
                "       (SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'information_type' AND BD.VALUE = t.type AND t.STATUS<>-1)TYPE," +
                "       t.type as type_true," +
                "       t.VIEWED," +
                "       t.TUPIAN," +
                "       t.STATUS," +
                "       to_char(t.CREATE_DATE, 'yyyy/MM/dd HH24:mi:ss') AS CREATE_DATE," +
                "       to_char(t.UPDATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME," +
//                "       t.DETAILS ," +//查列表就不需要富文本内容了，会很慢
                "       t.FILE_ID ," +
                "       t.EXTERNALLY" +
                "  from TB_INFORMATION t left join tb_user u on u.ss_id = t.create_ss_id " +
                " where t.type = 5 and 1 = 1   ");
        if (StringUtils.isNotBlank(key)) {
            sql.append(" and  t.TITLE  like ? ");
            args.add("%" + key + "%");
        }
//        if (StringUtils.isNotBlank(informationtype)) {
//            sql.append(" and  t.type  = ? ");
//            args.add("" + informationtype + "");
//        }
        sql.append("and t.isdelete=0 order by t.CREATE_DATE desc ");
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());

    }

    @Override
    public void save(Map<String, String> param) {
        String id = param.get("id");
        String title = param.get("bt");
        String describe = param.get("ms");
        String url = param.get("url");
        String details = param.get("editor");
        String file_id = param.get("avatar");
        String tupian = param.get("userFileIdAVATAR_filepath");
        String type = param.get("type");
        String status = param.get("status") == null ? "0" : "1";
        String externally = param.get("externally") == null ? "0" : "1";
        UserEntity u = CustomRealm.GetLoginUser();
        if (StringUtils.isBlank(id)) {// 新增
            DBHelper.execute("INSERT INTO TB_INFORMATION(ID,TITLE,DESCRIBE,URL,TYPE,VIEWED,CREATE_DATE,CREATE_SS_ID,TUPIAN,UPDATE_TIME,STATUS,DETAILS,ISDELETE,FILE_ID,EXTERNALLY) " +
                            "VALUES(SEQ_TB_INFORMATION.NEXTVAL,?,?,?,5,0,SYSDATE,?,?,SYSDATE,?,?,'0',?,?)",
                    title, describe, url, u.getUser_id(), tupian, status, details, file_id, externally);
        } else {// 修改
            DBHelper.execute(" UPDATE TB_INFORMATION SET TITLE=?,DESCRIBE=?,URL=?,TUPIAN=?,UPDATE_TIME=SYSDATE,DETAILS=?,STATUS=?,FILE_ID=?,EXTERNALLY=? WHERE ID=?",
                    title, describe, url, tupian, details, status, file_id, externally, id);
        }
        System.out.println("新增信息结束");
    }

    @Override
    public void del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("  UPDATE TB_INFORMATION SET ISDELETE='1' WHERE ID=?", id_s[i]);
        }
    }

    @Override
    public void updateStatus(Map<String, String> param) {
        String id = param.get("id");
        String status = param.get("status");
        // 修改
        DBHelper.execute("UPDATE TB_INFORMATION SET STATUS=? WHERE ID=?", status, id);
    }

    @Override
    public Page<?> getPageInformationList(String type, int firstrow, int rowcount) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT t.ID," +
                "      t.TITLE," +
                "      t.DESCRIBE," +
                "       t.URL," +
                "       u.NAME," +
                "       (SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'information_type' AND BD.VALUE = t.type )TYPE," +
                "       t.type as type_true," +
                "       t.VIEWED," +
                "       t.TUPIAN," +
                "       t.STATUS," +
                "       to_char(t.CREATE_DATE, 'yyyy/MM/dd HH24:mi:ss') AS CREATE_DATE," +
                "       to_char(t.UPDATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME," +
                "       t.FILE_ID," +
                "       t.EXTERNALLY" +
                "  from TB_INFORMATION t left join tb_user u on u.ss_id = t.create_ss_id " +
                " where   t.type  = ?  and t.isdelete=0 and status=1 order by t.CREATE_DATE desc ");

        return DBHelper.queryForPage(sql.toString(), firstrow, rowcount, type);
    }

    @Override
    public Map<String, Object> getPageInformationById(String id) {
        DBHelper.execute("UPDATE TB_INFORMATION SET VIEWED=(VIEWED+1) WHERE ID=?", id);

        return DBHelper.queryForMap("SELECT t.ID," +
                "     t.TITLE," +
                "       t.DESCRIBE," +
                "       t.URL," +
                "       u.NAME," +
                "       (SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'information_type' AND BD.VALUE = t.type AND t.STATUS<>-1)TYPE," +
                "       t.type as type_true," +
                "       t.VIEWED," +
                "       t.TUPIAN," +
                "       t.STATUS," +
                "       to_char(t.CREATE_DATE, 'yyyy/MM/dd HH24:mi:ss') AS CREATE_DATE," +
                "       to_char(t.UPDATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME," +
                "       t.FILE_ID," +
                "       t.DETAILS," +
                "       t.EXTERNALLY" +
                "  from TB_INFORMATION t left join tb_user u on u.ss_id = t.create_ss_id " +
                " where   t.id  = ?  and t.isdelete=0  ", id);

    }
}
