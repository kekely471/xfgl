package com.tonbu.web.app.itsm.service.impl;

import com.tonbu.web.AppConstant;
import com.tonbu.web.app.itsm.service.BusinessDirectoryService;
import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.framework.data.Limit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ServiceDirectoryServiceImpl implements BusinessDirectoryService {


    /**
     * 列表
     */
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        String pid = param.get("pid");
        // 获取sql 跟传入参数 不需要过滤是否启用
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT V.ID,V.NAME,V.DIRECTORYNO,V.DEFAULT_SLA,V.PARENTID,V.REMARK,V.SORT,V.STATUS,V.EXT1,V.EXT2,V.EXT3,D.NAME PNAME,D.DIRECTORYNO PDIRECTORYNO," +
                "U.USERNAME UPDATER, TO_CHAR(V.UPDATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') UPDATETIME,SLA.NAME  DEFAULT_SLA_NAME ");
        sql.append(" FROM BUSINESS_DIRECTORY V ");
        sql.append(" LEFT JOIN BUSINESS_DIRECTORY D ON D.ID=V.PARENTID ");
        sql.append(" LEFT JOIN SS_USER U ON U.ID=V.UPDATE_ID ");
        sql.append(" LEFT JOIN BUSINESS_SLA SLA ON SLA.ID=V.DEFAULT_SLA ");
        sql.append(" WHERE V.PARENTID<>0 AND V.STATUS <> " + AppConstant.STATUS_DEL); // 已经删除的记录
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND V.NAME LIKE ? ");
            args.add("%" + key + "%");
        }
        if (StringUtils.isNotBlank(pid)) {
            sql.append(" AND(V.PARENTID = ? OR V.ID = ? )");
            args.add(pid);
            args.add(pid);
        }
        //拼接排序
        String sord = param.get("sord");
        String sidx = param.get("sidx");
        if (StringUtils.isNotBlank(sidx)) {
            sql.insert(0, "SELECT * FROM (");
            sql.append(") T ORDER BY T." + sidx + " " + sord);
        } else {
            sql.append(" ORDER BY V.SORT ASC");
        }
        Limit limit = new Limit(param);
        Page<?> p = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
        return p;
    }


    /**
     * 修改/保存
     */
    public void save(Map<String, String> param) {
        String id = param.get("id");
        String parentid = param.get("parentid");
        String name = param.get("name");
        String directoryno = param.get("directoryno");
        String default_sla = param.get("default_sla");
        String sort = param.get("sort");
        String status = param.get("status") == null ? "0" : "1";
        String ext1 = param.get("ext1");
        String ext2 = param.get("ext2");
        String ext3 = param.get("ext3");
        String remark = param.get("remark");
        UserEntity u = CustomRealm.GetLoginUser();
        if (StringUtils.isBlank(id)) {// 新增
            DBHelper.execute("INSERT INTO BUSINESS_DIRECTORY(PARENTID,NAME,DIRECTORYNO,DEFAULT_SLA,SORT,STATUS,REMARK,CREATE_ID,CREATE_TIME,UPDATE_ID,UPDATE_TIME,EXT1,EXT2,EXT3) VALUES(?,?,?,?,?,?,?,?,SYSDATE,?,SYSDATE,?,?,?)", parentid, name, directoryno, default_sla, sort, status, remark, u.getUser_id(), u.getUser_id(), ext1, ext2, ext3);
        } else {// 修改
            DBHelper.execute("UPDATE BUSINESS_DIRECTORY SET PARENTID=?,NAME=?,DIRECTORYNO=?,DEFAULT_SLA=?,SORT=?,STATUS=?,REMARK=?,UPDATE_ID=?,UPDATE_TIME=SYSDATE(),EXT1=?,EXT2=?,EXT3=? WHERE ID=?", parentid, name, directoryno, default_sla, sort, status, remark, u.getUser_id(), ext1, ext2, ext3, id);
        }
    }

    /**
     * 删除
     */
    @Transactional(value = "primaryTransactionManager")
    public void del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            if (!StringUtils.equals("1", id_s[i])) {
                DBHelper.execute("UPDATE BUSINESS_DIRECTORY SET STATUS = ? WHERE PARENTID = ?", AppConstant.STATUS_DEL, id_s[i]);
                DBHelper.execute("UPDATE BUSINESS_DIRECTORY SET STATUS = ? WHERE ID = ?", AppConstant.STATUS_DEL, id_s[i]);
            } else {
                throw new RuntimeException("根目录不能删除");
            }
        }
    }

    /**
     * 删除
     */
    @Transactional(value = "primaryTransactionManager")
    public void realDel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            if (!StringUtils.equals("1", id_s[i])) {
                DBHelper.execute("DELETE FROM BUSINESS_DIRECTORY  WHERE PARENTID = ?", id_s[i]);
                DBHelper.execute("DELETE FROM BUSINESS_DIRECTORY  WHERE ID = ?", id_s[i]);
            } else {
                throw new RuntimeException("根目录不能删除");
            }
        }
    }
}
