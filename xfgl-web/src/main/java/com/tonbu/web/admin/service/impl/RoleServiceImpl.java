package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.exception.JSONException;
import com.tonbu.web.AppConstant;
import com.tonbu.web.admin.service.RoleService;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RoleServiceImpl implements RoleService {


    /**
     * 角色列表
     */
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT V.ID,V.ROLENAME,V.ROLE_TYPE,V.STATUS,V.REMARK ");
        //sql.append(" (SELECT WM_CONCAT(TO_CHAR(A.LOGINNAME)) FROM  ACCOUNT_ROLE AR LEFT JOIN ACCOUNT A ON A.ID=AR.ACCOUNT_ID WHERE AR.ROLE_ID=V.ID) AS RELACCOUNT ");
        sql.append(" FROM SS_ROLE V ");
        sql.append(" WHERE V.STATUS <> " + AppConstant.STATUS_DEL);
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND (V.ROLENAME LIKE ?) ");
            args.add("%" + key + "%");
        }
        //拼接排序
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
     * 删除角色组
     */
    public void del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("UPDATE SS_ROLE SET STATUS = ? WHERE ID = ?", AppConstant.STATUS_DEL, id_s[i]);
        }
    }

    /**
     * 删除角色组
     */
    @Transactional(value = "primaryTransactionManager")
    public void realDel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("DELETE FROM SS_ROLE WHERE ID = ?", id_s[i]);
            DBHelper.execute("DELETE FROM ACCOUNT_ROLE WHERE ROLE_ID=? ", id_s[i]);// 删除角色关联的用户
            DBHelper.execute("DELETE FROM SS_ROLE_MENU  WHERE ROLE_ID=? ", id_s[i]);// 删除角色关联的菜单
        }
    }


    /**
     * 角色新增/保存
     */
    public void save(Map<String, String> param) {
        String id = param.get("id");
        String rolename = param.get("rolename");
        String role_type = param.get("role_type");
        String status = param.get("status") == null ? "0" : "1";
        String remark = param.get("remark");
        String sort = param.get("sort");
        // 检查名称是否已存在
        String sql = "SELECT 1 FROM SS_ROLE WHERE ROLENAME = ? ";
        Map<String, Object> m;
        if (StringUtils.isBlank(id)) {// 新增
            m = DBHelper.queryForMap(sql, rolename);
        } else {// 修改
            sql += "AND ID <>?";
            m = DBHelper.queryForMap(sql, rolename, id);
        }
        if (m != null) {
            throw new JSONException("已存在此角色名称，请重新输入！");
        }
        if (StringUtils.isBlank(id)) {// 新增
            DBHelper.execute("INSERT INTO SS_ROLE(ID,ROLENAME,ROLE_TYPE,STATUS,REMARK,SORT) VALUES(SEQ_SS_ROLE.NEXTVAL,?,?,?,?,?)", rolename, role_type, status, remark, sort);
        } else {// 修改
            DBHelper.execute("UPDATE SS_ROLE SET ROLENAME=?,ROLE_TYPE=?,STATUS=?,REMARK=?,SORT=? WHERE ID=?", rolename, role_type, status, remark, sort, id);

        }
    }


    /**
     * 角色的用户列表
     */
    public List<?> loadAccountList(Map<String, String> param) {
        String id = param.get("id");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT  A.ID,A.LOGINNAME, CASE WHEN AR.ROLE_ID IS NULL THEN '0' ELSE '1' END SELECTED   FROM ACCOUNT A ");
        sql.append(" LEFT JOIN ACCOUNT_ROLE AR ON AR.ACCOUNT_ID=A.ID AND AR.ROLE_ID=? ");
        args.add(id);
        sql.append("ORDER BY A.ID");
        return DBHelper.queryForList(sql.toString(), args.toArray());
    }


    /**
     * 保存角色菜单
     */
    @Transactional(value = "primaryTransactionManager")
    public void saveSet(Map<String, String> param) {
        String id = param.get("id");
        String menus = param.get("menus");
        if (StringUtils.isBlank(menus)) {
            throw new JSONException("缺少参数！");
        }
        DBHelper.execute("DELETE FROM SS_ROLE_MENU WHERE ROLE_ID=?", id);
        if (StringUtils.isNotBlank(menus)) {
            String[] menusids = menus.split(",");
            for (String menuid : menusids) {
                if (StringUtils.isNotBlank(menuid)) {
                    DBHelper.execute("INSERT INTO SS_ROLE_MENU(ROLE_ID, MENU_ID) VALUES(?,?)", id, menuid);
                }
            }
        }
    }


    /**
     * 保存角色用户
     */
    public void saveSetUser(Map<String, String> param) {
        String accountids = param.get("accountids");
        String id = param.get("id");
        DBHelper.execute("DELETE FROM ACCOUNT_ROLE WHERE ROLE_ID=? ", id);
        if (StringUtils.isNotBlank(accountids)) {
            String[] ids = accountids.split(",");
            for (String userid : ids) {
                if (StringUtils.isNotBlank(userid)) {
                    DBHelper.execute("INSERT INTO ACCOUNT_ROLE (ACCOUNT_ID,ROLE_ID) VALUES (?,?)", userid, id);
                }
            }
        }
    }


}
