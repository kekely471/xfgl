package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.aop.AopLog;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.ConfigService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ConfigServiceImpl implements ConfigService {

    Logger logger = LogManager.getLogger(ConfigServiceImpl.class.getName());


    @Override
    @AopLog(module = "配置管理",method="添加")
    public ResultEntity addConfig(Map<String, String> param) {
        ResultEntity r = new ResultEntity(1);
        UserEntity u = CustomRealm.GetLoginUser();
        StringBuffer sql = new StringBuffer(" INSERT INTO B_CONFIG(ID, KEY, VALUE, CREATE_ID, CREATE_TIME, UPDATE_ID, UPDATE_TIME, CODE, REMARK) " +
                " VALUES(SEQ_CONFIG.Nextval,?,?,?,SYSDATE,?,SYSDATE,?,?) ");
        Integer affectedRows = DBHelper.execute(sql.toString(), param.get("key"), param.get("value"), u.getId(), u.getId(), param.get("code"), param.get("remark"));
        if (affectedRows == 1) {
            r.setMsg("添加成功");
        } else {
            r.setMsg("添加失败");
        }
        return r;
    }

    @Override
    @AopLog(module="配置管理",method = "删除")
    public ResultEntity delConfig(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        StringBuffer sql = new StringBuffer(" delete from B_CONFIG " +
                " where id=? ");
        ResultEntity r = new ResultEntity(1);
        String errorMes = "错误信息：";
        try {
            for (int i = 0; i < id_s.length; i++) {
                Integer affectRow = DBHelper.execute(sql.toString(), id_s[i]);
                if (affectRow != 1) {
                    r.setCode(-1);
                    errorMes += "编号为" + id_s[i] + "的记录删除失败 ";
                }
            }
            if (r.getCode() == -1) {
                r.setMsg(errorMes);
            }
            r.setMsg(errorMes);
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    @Override
    @AopLog(module = "配置管理",method="更新")
    public ResultEntity updateConfig(Map<String, String> param) {
        ResultEntity r = new ResultEntity(1);
        UserEntity u = CustomRealm.GetLoginUser();
        StringBuffer sql = new StringBuffer(" UPDATE B_CONFIG SET KEY=?, VALUE=?, UPDATE_ID=?, UPDATE_TIME=SYSDATE, CODE=?, REMARK=? " +
                " WHERE ID=? ");
        Integer affectedRows = DBHelper.execute(sql.toString(), param.get("key"), param.get("value"), u.getId(), param.get("code"), param.get("remark"), param.get("id"));
        if (affectedRows == 1) {
            r.setMsg("修改成功");
        } else {
            r.setMsg("修改失败");
        }
        return r;
    }

    @Override
    public Page<?> queryConfig(Map<String, String> param) {
        String key = param.get("key_name");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer(" SELECT C.ID, C.KEY, C.VALUE, A.LOGINNAME CREATENAME, to_char(C.CREATE_TIME,'yyyy-MM-dd HH24:mi:ss') CREATE_TIME, B.LOGINNAME UPDATENAME, C.UPDATE_TIME, C.CODE, C.REMARK FROM B_CONFIG C " +
                " LEFT JOIN ACCOUNT A " +
                " ON C.CREATE_ID=A.ID " +
                " LEFT JOIN ACCOUNT B " +
                " ON C.UPDATE_ID=B.ID ");
        if (StringUtils.isNotBlank(key)) {
            sql.append(" WHERE (C.KEY LIKE ?) ");
            args.add("%" + key + "%");
        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }
}
