package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.support.aop.AopLog;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.AppConstant;
import com.tonbu.web.admin.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    /**
     * 用户信息列表
     */
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        String deptid = param.get("pid");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT V.ID,V.USERNAME,V.SEX,V.USER_TYPE,V.MOBILE,V.OFFICETEL,V.STATUS,V.EMAIL,V.SORT,D.DEPTNAME AS DEPTNAME, ");
        sql.append(" (SELECT WM_CONCAT(TO_CHAR(A.LOGINNAME)) FROM  ACCOUNT_USER AU LEFT JOIN ACCOUNT A ON A.ID=AU.ACCOUNT_ID WHERE AU.USER_ID=V.ID) AS LOGINNAME, ");
        sql.append(" TO_CHAR(V.UPDATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') UPDATETIME ");
        sql.append(" FROM SS_USER V ");
        sql.append(" LEFT JOIN SS_DEPT D ON D.ID=V.DEPT_ID ");
        sql.append(" WHERE V.STATUS <>" + AppConstant.STATUS_DEL); // 已经删除的用户
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND (D.DEPTNAME LIKE ? OR V.USERNAME LIKE ? ) ");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
        }
        if (StringUtils.isNotBlank(deptid) && !deptid.equals("1")) {
            sql.append(" AND( V.DEPT_ID = ? )");
            args.add(deptid);
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
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }

    /**
     * 用户新增/保存
     */
    @AopLog(module = "用户管理", method = "新增修改")
    public void save(Map<String, String> param) {
        String id = param.get("id");
        String username = param.get("username");
        String user_type = param.get("user_type");
        String dept_id = param.get("dept_id");
        String mobile = param.get("mobile");
        String email = param.get("email");
        String office_tel = param.get("officetel");
        String remark = param.get("remark");
        String sort = param.get("sort");
        String sex = param.get("sex");
        String status = param.get("status") == null ? "0" : "1";
        UserEntity u = CustomRealm.GetLoginUser();
        if (StringUtils.isBlank(id)) {// 新增
            DBHelper.execute("INSERT INTO SS_USER(ID,USERNAME,USER_TYPE,DEPT_ID,MOBILE,EMAIL,OFFICETEL,SORT,SEX,STATUS,REMARK,CREATE_ID,CREATE_TIME,UPDATE_ID,UPDATE_TIME,NICK_NAME,AVATAR,SIGN) " +
                            "VALUES(SEQ_SS_USER.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,SYSDATE,?,SYSDATE,?,?,?)",
                    username, user_type, dept_id, mobile, email, office_tel, sort, sex, status, remark, u.getUser_id(), u.getUser_id(), param.get("nick_name"), param.get("avatar"), param.get("sign"));
        } else {// 修改
            DBHelper.execute("UPDATE SS_USER SET USERNAME=?,USER_TYPE=?,DEPT_ID=?,MOBILE=?,EMAIL=?,OFFICETEL=?,SORT=?,SEX=?,STATUS=?,REMARK=?,UPDATE_ID=?,UPDATE_TIME=SYSDATE,NICK_NAME=?,AVATAR=?,SIGN=? WHERE ID=?",
                    username, user_type, dept_id, mobile, email, office_tel, sort, sex, status, remark, u.getUser_id(), param.get("nick_name"), param.get("avatar"), param.get("sign"), id);
        }
    }

    /**
     * 删除
     */
    @AopLog(module = "用户管理", method = "删除")
    public void del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            if (!StringUtils.equals("1", id_s[i])) { //根目录不能删除
                DBHelper.execute("UPDATE SS_USER SET STATUS = ? WHERE ID = ?", AppConstant.STATUS_DEL, id_s[i]);
            } else {
                throw new JSONException("ROOT用户不能删除");

            }
        }
    }

    /**
     * 彻底删除
     */
    @AopLog(module = "用户管理", method = "彻底删除")
    public void realDel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            if (!StringUtils.equals("1", id_s[i])) { //根目录不能删除
                DBHelper.execute("DELETE FROM SS_USER WHERE ID = ?", id_s[i]);
            } else {
                throw new JSONException("ROOT用户不能删除");

            }
        }
    }

    @AopLog(module = "用户管理", method = "修改")
    public void updateUserInfo(Map<String, String> param) {
        DBHelper.execute("UPDATE SS_USER SET USERNAME=?,MOBILE=?,EMAIL=?,OFFICETEL=?,SEX=?,REMARK=?,UPDATE_ID=?,NICK_NAME=?,AVATAR=?,SIGN=?,UPDATE_TIME=SYSDATE WHERE ID=? ",
                param.get("username"), param.get("mobile"), param.get("email"), param.get("officetel"), param.get("sex"), param.get("remark"), param.get("id"), param.get("nick_name"), param.get("avatar"), param.get("sign"), param.get("id"));
    }
}
