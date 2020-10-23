package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.exception.JSONException;
import com.tonbu.web.AppConstant;
import com.tonbu.web.admin.service.DictService;
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
public class DictServiceImpl implements DictService {

    /**
     * 字典类型列表
     */
    public Page<?> loadTypeList(Map<String, String> param) {
        String key = param.get("key_name");
        List<Object> args = new ArrayList<Object>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT D.ID,D.CODE,D.NAME,D.TYPE,D.REMARK ");
        sql.append("FROM SS_DICT_TYPE D ");
        sql.append("WHERE 1=1 ");
        if (StringUtils.isNotBlank(key)) {
            sql.append("AND D.NAME LIKE ? ");
            args.add("%" + key + "%");
        }
        //拼接排序
        String sord = param.get("sord");
        String sidx = param.get("sidx");
        if (StringUtils.isNotBlank(sidx)) {
            sql.insert(0,"SELECT * FROM (");
            sql.append(") T ORDER BY T." + sidx + " " + sord);
        }else{
            sql.append(" ORDER BY D.ID ASC");
        }

        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }

    /**
     * 新增/保存字典类型
     */
    public void saveType(Map<String, String> param) {
        String id = param.get("id");
        String name = param.get("name");
        String remark = param.get("remark");
        String type=param.get("type");
        String code = param.get("code");
        // 新增
        if (StringUtils.isBlank(id)) {
            int count = DBHelper.queryForScalar("SELECT COUNT(*) FROM SS_DICT_TYPE WHERE CODE=?", Integer.class, code);
            if (count > 0) {
                throw new JSONException("该类型标识已存在！");
            }
            count = DBHelper.queryForScalar("SELECT COUNT(*) FROM SS_DICT_TYPE WHERE NAME=?", Integer.class, name);
            if (count > 0) {
                throw new JSONException("该类型名称已存在！");
            }
            DBHelper.execute("INSERT INTO SS_DICT_TYPE(ID,NAME,TYPE,REMARK,CODE) VALUES(SEQ_SS_DICT_TYPE.NEXTVAL,?,?,?,?)",name,type,remark,code);
        // 修改
        } else {
            int count = DBHelper.queryForScalar("SELECT COUNT(*) FROM SS_DICT_TYPE WHERE CODE<>? AND NAME=?", Integer.class, code, name);
            if (count > 0) {
                throw new JSONException("类型名称已存在！");
            }
            DBHelper.execute("UPDATE SS_DICT_TYPE SET NAME=?,TYPE=?,REMARK=?,CODE=? WHERE ID=?", name,type, remark,code,id);
        }
    }

    /**
     * 彻底删除删除字典类型
     */
    @Transactional(value = "primaryTransactionManager")
    public void realDelType(Map<String, String> param) {
        String ids=param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("DELETE FROM SS_DICT WHERE TYPE_CODE=?", id_s[i]);
            DBHelper.execute("DELETE FROM BUSINESS_DICT WHERE TYPE_CODE=?", id_s[i]);
            DBHelper.execute("DELETE FROM SS_DICT_TYPE WHERE ID=?", id_s[i]);
        }
    }

    /**
     * 字典列表
     */
    public Page<?> loadSysList(Map<String, String> param) {
        String key = param.get("key_name");
        String type_code = param.get("type_code");
        List<Object> args = new ArrayList<Object>();

        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT D.ID,D.NAME,D.TYPE_CODE,D.VALUE,D.SORT,D.CN,D.STATUS,D.EXT1,D.EXT2,D.EXT3,DT.NAME TYPENAME ");
        sql.append(" FROM SS_DICT D ");
        sql.append(" LEFT JOIN SS_DICT_TYPE DT ON DT.CODE=D.TYPE_CODE ");
        sql.append("WHERE DT.CODE=D.TYPE_CODE AND DT.TYPE=0 AND D.STATUS <> "+AppConstant.STATUS_DEL);
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND (D.VALUE LIKE ? OR D.TYPE_CODE LIKE ? OR D.NAME LIKE ? OR DT.NAME LIKE ? )");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
        }
        if (StringUtils.isNotBlank(type_code)&&!StringUtils.equals("0",type_code)) {
            sql.append(" AND D.TYPE_CODE = ? ");
            args.add(type_code);
        }
        //拼接排序
        String sord = param.get("sord");
        String sidx = param.get("sidx");
        if (StringUtils.isNotBlank(sidx)) {
            sql.insert(0,"SELECT * FROM (");
            sql.append(" ) T ORDER BY T." + sidx + " " + sord);
        }else{
            sql.append(" ORDER BY D.TYPE_CODE ASC");
        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }

    /**
     * 新增/保存字典
     */
    public void saveSys(Map<String, String> param) {
        String id = param.get("id");
        String type_code = param.get("type_code");
        String value = param.get("value");
        String name = param.get("name");
        String cn = param.get("cn");
        String sort = param.get("sort");
        String status = param.get("status") == null ? "0" : "1";
        String ext1 = param.get("ext1");
        String ext2 = param.get("ext2");
        String ext3 = param.get("ext3");
        if (StringUtils.isBlank(id)) {// 新增
            int count = DBHelper.queryForScalar("SELECT COUNT(*) FROM SS_DICT WHERE VALUE=? AND TYPE_CODE=?", Integer.class, value,type_code);
            if (count > 0) {
                throw new JSONException("字典值已存在！");
            }
            DBHelper.execute("INSERT INTO SS_DICT(ID,TYPE_CODE,VALUE,NAME,SORT,CN,STATUS,EXT1,EXT2,EXT3) VALUES(SEQ_SS_DICT.NEXTVAL,?,?,?,?,?,?,?,?,?)", type_code, value,name, sort, cn, status, ext1, ext2, ext3);
        } else {// 修改
            DBHelper.execute("UPDATE SS_DICT SET TYPE_CODE=?,VALUE=?,NAME=?,CN=?,SORT=?,STATUS=?,EXT1=?,EXT2=?,EXT3=? WHERE ID=?", type_code,value,name, cn, sort, status, ext1, ext2, ext3, id);
        }
    }

    /**
     * 删除字典
     */
    public void delSys(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("UPDATE SS_DICT SET STATUS=? WHERE ID=?", AppConstant.STATUS_DEL, id_s[i]);
        }
    }
    /**
     * 删除字典
     */
    public void realDelSys(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("DELETE FROM SS_DICT WHERE ID=?",id_s[i]);
        }
    }



    /**
     * 单位字典列表(左边GRID)
     */
    public Page<?> loadBusinessList(Map<String, String> param) {
        String key = param.get("key_name");
        String type_code = param.get("type_code");
        List<Object> args = new ArrayList<Object>();

        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT D.ID,D.NAME,D.TYPE_CODE,D.VALUE,D.SORT,D.CN,D.STATUS,D.EXT1,D.EXT2,D.EXT3,DT.NAME TYPENAME ");
        sql.append(" FROM BUSINESS_DICT D ");
        sql.append(" LEFT JOIN SS_DICT_TYPE DT ON DT.CODE=D.TYPE_CODE ");
        sql.append("WHERE DT.CODE=D.TYPE_CODE AND DT.TYPE=1 AND D.STATUS <> "+AppConstant.STATUS_DEL);
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND (D.NAME LIKE ? OR D.VALUE LIKE ? OR D.TYPE_CODE LIKE ? OR DT.NAME LIKE ? )");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
        }
        if (StringUtils.isNotBlank(type_code)&&!StringUtils.equals("0",type_code)) {
            sql.append(" AND D.TYPE_CODE = ? ");
            args.add(type_code);
        }
        //拼接排序
        String sord = param.get("sord");
        String sidx = param.get("sidx");
        if (StringUtils.isNotBlank(sidx)) {
            sql.insert(0,"SELECT * FROM (");
            sql.append(" ) T ORDER BY T." + sidx + " " + sord);
        }else{
            sql.append(" ORDER BY D.TYPE_CODE ASC");
        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }


    /**
     * 新增/保存字典类型
     */
    public void saveBusiness(Map<String, String> param) {
        String id = param.get("id");
        String type_code = param.get("type_code");
        String value = param.get("value");
        String name = param.get("name");
        String sort = param.get("sort");
        String cn = param.get("cn");
        String remark = param.get("remark");
        String ext1 = param.get("ext1");
        String ext2 = param.get("ext2");
        String ext3 = param.get("ext3");
        String status = param.get("status") == null ? "0" : "1";
        if (StringUtils.isBlank(id)) {// 新增
            int count = DBHelper.queryForScalar("SELECT COUNT(*) FROM BUSINESS_DICT WHERE VALUE=? AND TYPE_CODE=?", Integer.class, value,type_code);
            if (count > 0) {
                throw new JSONException("字典值已存在！");
            }
            DBHelper.execute("INSERT INTO BUSINESS_DICT(ID,TYPE_CODE,VALUE,NAME,SORT,CN,STATUS,REMARK,EXT1,EXT2,EXT3) VALUES(SEQ_SS_DICT.NEXTVAL,?,?,?,?,?,?,?,?,?,?)", type_code,value,name, sort, cn,status, remark,ext1,ext2,ext3);
        } else {// 修改
            DBHelper.execute("UPDATE BUSINESS_DICT SET TYPE_CODE=?,VALUE=?,NAME=?,SORT=?,CN=?,REMARK=?,EXT1=?,EXT2=?,EXT3=?,STATUS=? WHERE ID=?", type_code,value, name,sort, cn, remark,ext1,ext2,ext3,status,id);
        }
    }

    /**
     * 删除字典类型
     */
    public void delBusiness(Map<String, String> param) {
        String ids=param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("UPDATE BUSINESS_DICT SET STATUS = ? WHERE ID = ?", AppConstant.STATUS_DEL, id_s[i]);
        }
    }

    /**
     * 彻底删除字典类型
     */
    public void realDelBusiness(Map<String, String> param) {
        String ids=param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("DELETE FROM BUSINESS_DICT WHERE ID = ?", id_s[i]);
        }
    }


}
