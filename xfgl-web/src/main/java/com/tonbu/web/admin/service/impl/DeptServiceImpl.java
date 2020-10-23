package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.exception.JSONException;
import com.tonbu.web.AppConstant;
import com.tonbu.web.admin.service.DeptService;
import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.framework.data.Limit;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DeptServiceImpl implements DeptService {
    Logger logger = LogManager.getLogger(DeptServiceImpl.class.getName());

    /**
     * 获取部门用户zTree树列表
     */
    public List<Map<String,Object>> loadzTreeList(Map<String,String> param){
        List<Map<String,Object>> l = new ArrayList<>();
        String pid = param.get("parentid");
        StringBuilder sql = new StringBuilder();
        //部门的list

        sql.append("SELECT CONCAT('D_',ID) AS ID,DEPTNAME AS NAME,CONCAT('D_',PARENTID) AS PID,'dept' AS TYPE from SS_DEPT   WHERE STATUS <>  " + AppConstant.STATUS_DEL);
        if(StringUtils.isNotBlank(pid)){
            sql.append(" start with id = "+pid+" connect by prior id = parentid ");
        }
        l.addAll(DBHelper.queryForList(sql.toString()));

        //人员的list
        sql = new StringBuilder();
        sql.append("SELECT ID,USERNAME AS NAME, CONCAT('D_',DEPT_ID)AS PID ,'user' AS TYPE FROM SS_USER  WHERE STATUS <> " + AppConstant.STATUS_DEL);
        if(StringUtils.isNotBlank(pid)){
            sql.append("  and dept_id in (select id from ss_dept start with id = "+pid+" connect by prior id = parentid) ");
        }
        l.addAll(DBHelper.queryForList(sql.toString()));
        return l;
    }
    /**
     * 列表
     */
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        String pid = param.get("pid");

        // 获取sql 跟传入参数 不需要过滤是否启用
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT V.ID,V.DEPTNAME,V.DEPTNO,D.DEPTNAME PDEPTNAME,D.DEPTNO PDEPTNO,U.USERNAME UPDATER,V.SORT,V.ISUNIT, " +
                "TO_CHAR(V.UPDATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') UPDATETIME,V.STATUS,v.LEAVE_LIMIT ");
        sql.append(" FROM SS_DEPT V ");
        sql.append(" LEFT JOIN SS_DEPT D ON D.ID=V.PARENTID ");
        sql.append(" LEFT JOIN SS_USER U ON U.ID=V.UPDATE_ID ");
        sql.append(" WHERE V.PARENTID<>0 AND V.STATUS <> " + AppConstant.STATUS_DEL); // 已经删除的记录
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND V.DEPTNAME LIKE ? ");
            args.add("%" + key + "%");
        }
        if (StringUtils.isNotBlank(pid)&&!pid.equals("1")) {
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
     * 删除
     */
    @Transactional(value = "primaryTransactionManager")
    public void del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            if (!StringUtils.equals("1", id_s[i])) { //根目录不能删除
                DBHelper.execute("UPDATE SS_DEPT SET STATUS = ? WHERE PARENTID = ?", AppConstant.STATUS_DEL, id_s[i]);
                DBHelper.execute("UPDATE SS_DEPT SET STATUS = ? WHERE ID = ?", AppConstant.STATUS_DEL, id_s[i]);
            } else {
                throw new JSONException("根目录不能删除");
            }
        }
    }

    /**
     * 彻底删除
     */
    @Transactional(value = "primaryTransactionManager")
    public void realDel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            if (!StringUtils.equals("1", id_s[i])) { //根目录不能删除
                DBHelper.execute("DELETE FROM SS_DEPT WHERE PARENTID = ?", id_s[i]);
                DBHelper.execute("DELETE FROM SS_DEPT WHERE ID = ?", id_s[i]);
            } else {
                throw new JSONException("根目录不能删除");

            }
        }
    }

    /**
     * 修改/保存
     */
    @Transactional(value = "primaryTransactionManager")
    public void save(Map<String, String> param) {
        String id = param.get("id");
        String parentid = param.get("parentid");
        String name = param.get("name");
        String deptno = param.get("deptno");
        String address = param.get("address");
        String location = param.get("location");
        String leave_limit = param.get("leave_limit");
        String sort = param.get("sort");
        String status = param.get("status") == null ? "0" : "1";
        String isunit = param.get("isunit") == null ? "0" : "1";
        String remark = param.get("remark");
        UserEntity u = CustomRealm.GetLoginUser();
        if (StringUtils.isBlank(id)) {// 新增
            DBHelper.execute("INSERT INTO SS_DEPT(ID,PARENTID,DEPTNAME,DEPTNO,ADDRESS,LOCATION,SORT,STATUS,REMARK,CREATE_ID,CREATE_TIME,UPDATE_ID,UPDATE_TIME,ISUNIT,TEL,LEAVE_LIMIT) " +
                            "VALUES(SEQ_SS_DEPT.NEXTVAL,?,?,?,?,?,?,?,?,?,SYSDATE,?,SYSDATE,?,?,?)",
                    parentid, name, deptno, address, location, sort, status, remark, u.getUser_id(), u.getUser_id(), isunit,param.get("tel"),leave_limit);
        } else {// 修改
            DBHelper.execute("UPDATE SS_DEPT SET PARENTID=?,DEPTNAME=?,DEPTNO=?,ADDRESS=?,LOCATION=?,SORT=?,STATUS=?,REMARK=?,UPDATE_ID=?,UPDATE_TIME=SYSDATE,ISUNIT=?,TEL=?,LEAVE_LIMIT=? WHERE ID=?",
                    parentid, name, deptno, address, location, sort, status, remark, u.getUser_id(), isunit,param.get("tel"),leave_limit,id);
        }
    }


}
