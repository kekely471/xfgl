package com.tonbu.web.app.itsm.service.impl;

import com.tonbu.web.AppConstant;
import com.tonbu.web.app.itsm.service.SlaService;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.framework.data.Limit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SlaServiceImpl implements SlaService {

    /**
     * 列表
     */
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        String sla_type = param.get("sla_type");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT V.ID,V.NAME,V.CONTENT,V.SOLVETIME_HOUR,V.SOLVETIME_MIN,V.REMARK,V.SLA_TYPE,V.RESPONSE_TIME,V.SORT,V.STATUS,V.EXT1,V.EXT2,V.EXT3," +
                "CONCAT(V.SOLVETIME_HOUR,'小时',V.SOLVETIME_MIN,'分钟') AS SOLVETIME ");
        sql.append(" FROM BUSINESS_SLA V ");
        sql.append(" WHERE V.STATUS <> "+ AppConstant.STATUS_DEL);
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND ( V.NAME LIKE ? OR V.CONTENT LIKE ? ) ");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
        }
        if(StringUtils.isNotBlank(sla_type)){
            sql.append(" AND V.SLA_TYPE=? ");
            args.add(sla_type);
        }
        //拼接排序
        String sord = param.get("sord");
        String sidx = param.get("sidx");
        if (StringUtils.isNotBlank(sidx)) {
            sql.insert(0,"SELECT * FROM (");
            sql.append(") T ORDER BY T." + sidx + " " + sord);
        }else{
            sql.append(" ORDER BY V.SORT ASC");
        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }


    /**
     * 修改/保存
     */
    public void save(Map<String, String> param) {

        String id = param.get("id");
        String sla_type = param.get("sla_type");
        String content = param.get("content");
        String response_time = param.get("response_time");
        String solvetime_hour = param.get("solvetime_hour");
        String solvetime_min = param.get("solvetime_min");
        String name = param.get("name");
        String status = param.get("status") == null ? "0" : "1";
        String sort = param.get("sort");
        String remark = param.get("remark");
        String ext1 = param.get("ext1");
        String ext2 = param.get("ext2");
        String ext3 = param.get("ext3");
        UserEntity u = CustomRealm.GetLoginUser();
        if (StringUtils.isBlank(id)) {// 新增
            DBHelper.execute("INSERT INTO BUSINESS_SLA(SLA_TYPE,NAME,SORT,RESPONSE_TIME,SOLVETIME_HOUR,SOLVETIME_MIN,CONTENT,REMARK,STATUS," +
                            "CREATE_ID,CREATE_TIME,UPDATE_ID,UPDATE_TIME,EXT1,EXT2,EXT3) " +
                            "VALUES(?,?,?,?,?,?,?,?,?,?,SYSDATE,?,SYSDATE,?,?,?)",
                    sla_type, name, sort, response_time, solvetime_hour, solvetime_min, content, remark, status,u.getUser_id(), u.getUser_id(),ext1,ext2,ext3);
        } else {// 修改
            DBHelper.execute("UPDATE BUSINESS_SLA SET SLA_TYPE=?,NAME=?,SORT=?,RESPONSE_TIME=?,SOLVETIME_HOUR=?,SOLVETIME_MIN=?,CONTENT=?," +
                            "REMARK=?,STATUS=?,UPDATE_ID=?,UPDATE_TIME=SYSDATE,EXT1=?,EXT2=?,EXT3=? WHERE ID=?",
                    sla_type, name, sort, response_time, solvetime_hour, solvetime_min, content, remark,status, u.getUser_id(),ext1,ext2,ext3, id);
        }
    }

    /**
     * 删除
     */
    public void del(Map<String, String> param) {
        String ids=param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("UPDATE BUSINESS_SLA SET STATUS = ? WHERE ID = ?", AppConstant.STATUS_DEL, id_s[i]);
        }
    }

    /**
     * 删除
     */
    public void realDel(Map<String, String> param) {
        String ids=param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("DELETE FROM BUSINESS_SLA WHERE ID = ?", id_s[i]);
        }
    }
}
