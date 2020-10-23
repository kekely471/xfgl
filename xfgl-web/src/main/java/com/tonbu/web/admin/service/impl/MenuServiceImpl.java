package com.tonbu.web.admin.service.impl;


import com.tonbu.framework.exception.JSONException;
import com.tonbu.web.AppConstant;
import com.tonbu.web.admin.service.MenuService;
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
public class MenuServiceImpl implements MenuService {

    public List getList(){
        return DBHelper.queryForList("select case instr(url,'/',1,1) when 1 then url else concat('/', url) end url  from ss_menu where url is not null\n");
    }

    /**
     * 菜单节点下属菜单列表
     */
    public Page<?> loadList(Map<String, String> param) {


        String key = param.get("key_name");
        String pid = param.get("pid");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();

        sql.append(" SELECT V.ID,V.PARENTID,V.MENUNAME,V.URL,V.SORT,V.STATUS,V.MENUTYPE,V.REMARK,V.SYMBOL,V.ICON,P.MENUNAME PNAME ");
        sql.append(" FROM SS_MENU V ");
        sql.append(" LEFT JOIN SS_MENU P ON P.ID=V.PARENTID ");
        sql.append(" WHERE V.PARENTID<>'0' AND V.STATUS <> " + AppConstant.STATUS_DEL); // 已经删除的记录
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND V.MENUNAME LIKE ? ");
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
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(),args.toArray());
    }


    /**
     * 保存菜单信息,区分同级新增、下级新增、修改保存 新增菜单的时候触发 存储过程
     */
    public void save(Map<String, String> param) {
        String id = param.get("id");
        String parentid = param.get("parentid");
        String menuname = param.get("menuname");
        String url = param.get("url");
        String symbol = param.get("symbol");
        String icon = param.get("icon");
        String sort = param.get("sort");
        String status = param.get("status") == null ? "0" : "1";
        String menutype = param.get("menutype");
        String remark = param.get("remark");
        if(StringUtils.isEmpty(icon)){
            icon="layui-icon layui-icon-component";
        }

        if (StringUtils.isBlank(id)) {// 新增
            Integer sycnt = DBHelper.queryForScalar(" SELECT COUNT(*) FROM SS_MENU WHERE SYMBOL = ? AND MENUTYPE=1 ", Integer.class, symbol);
            if (sycnt > 0) {
                throw new JSONException("标识符已经存在,不允许使用");
            }
            String parentidMod = StringUtils.equals(parentid, "00") ? "" : parentid;
            String nodeid = DBHelper.queryForScalar("select lpad( NVL(max(to_number(id)),'" + parentidMod + "00') +1,nvl(length('" + parentidMod + "'),0) + 6,'0') from ss_menu where parentid=? ", String.class, parentid); // 占位符有点问题
            DBHelper.execute("INSERT INTO SS_MENU(ID,PARENTID,MENUNAME,URL,SYMBOL,SORT,STATUS,REMARK,MENUTYPE,ICON) VALUES(?,?,?,?,?,?,?,?,?,?)", nodeid, parentid, menuname, url, symbol, sort, status, remark, menutype,icon);
        } else {// 修改
            DBHelper.execute("UPDATE SS_MENU SET PARENTID=?,MENUNAME=?,URL=?,SYMBOL=?,SORT=?,STATUS=?,REMARK=?,MENUTYPE=?,ICON=? WHERE ID=?", parentid, menuname, url, symbol, sort, status, remark, menutype,icon, id);
        }
    }

    /**
     * 菜单树节点信息删除
     */
    @Transactional(value = "primaryTransactionManager")
    public void del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            if (!StringUtils.equals("00", id_s[i])) { //根目录不能删除
                DBHelper.execute("UPDATE SS_MENU SET STATUS = ? WHERE PARENTID = ?", AppConstant.STATUS_DEL, id_s[i]);
                DBHelper.execute("UPDATE SS_MENU SET STATUS = ? WHERE ID = ?", AppConstant.STATUS_DEL, id_s[i]);
            } else {
                throw new JSONException("根节点不能删除");

            }
        }
    }

    /**
     * 菜单树节点信息彻底删除
     */
    @Transactional(value = "primaryTransactionManager")
    public void realDel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            if (!StringUtils.equals("00", id_s[i])) { //根目录不能删除
                DBHelper.execute("DELETE FROM SS_MENU WHERE PARENTID = ?", id_s[i]);
                DBHelper.execute("DELETE FROM SS_MENU WHERE ID = ?", id_s[i]);
            } else {
                throw new JSONException("根节点不能删除");
            }
        }
    }
}
