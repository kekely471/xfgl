package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.VersionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VersionServiceImpl  implements VersionService {
    @Override
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        String platform = param.get("platform");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT V.ID,V.VERSIONNAME,V.VERSIONCODE,V.FILESIZE,V.DOWNLOADURL,V.REMARK,to_char(V.CREATE_TIME,'yyyy-MM-dd HH24:mi:ss')CREATE_TIME,(SELECT U.USERNAME FROM SS_USER U WHERE U.ID = V.CREATE_ID) AS CREATE_ID,V.VERSIONSTATE,V.USEPLATFORM" +
                " FROM B_VERSION V " +
                " WHERE 1=1 ");
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND VERSIONNAME LIKE ?");
            args.add("%" + key + "%");
        }
        if (StringUtils.isNotBlank(platform)) {
            sql.append(" AND USEPLATFORM = ? ");
            args.add(param.get("platform"));
        }
        sql.append(" ORDER BY ID DESC ");
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }

    @Override
    public ResultEntity addInfo(Map<String, String> param) {
        ResultEntity r = new ResultEntity(1);
        UserEntity u = CustomRealm.GetLoginUser();
        StringBuffer sql = new StringBuffer(" INSERT INTO B_VERSION(ID,VERSIONNAME,VERSIONCODE,FILESIZE,DOWNLOADURL,REMARK,CREATE_TIME,CREATE_ID,VERSIONSTATE,USEPLATFORM) " +
                " VALUES (SEQ_B_VERSION.nextval,?,?,?,?,?,SYSDATE,?,?,?) ");
        Integer affectRow = DBHelper.execute(sql.toString(), param.get("versionname"), param.get("versioncode"), param.get("filesize"),
                param.get("downloadurl"), param.get("remark"), u.getUser_id(), param.get("versionstate"),param.get("useplatform"));
        if (affectRow != 1) {
            r.setCode(-1);
            r.setMsg("新增失败！");
        }
        return r;
    }

    @Override
    public ResultEntity updateInfo(Map<String, String> param) {
        ResultEntity r = new ResultEntity(1);
        UserEntity u = CustomRealm.GetLoginUser();
        StringBuffer sql = new StringBuffer(" UPDATE B_VERSION SET VERSIONNAME=?,VERSIONCODE=?,FILESIZE=?,DOWNLOADURL=?,REMARK=?,CREATE_TIME=SYSDATE,CREATE_ID=?,VERSIONSTATE=?,USEPLATFORM=? " +
                " WHERE ID=? ");
        Integer affectRow = DBHelper.execute(sql.toString(), param.get("versionname"), param.get("versioncode"), param.get("filesize"),
                param.get("downloadurl"), param.get("remark"), u.getUser_id(), param.get("versionstate"),param.get("useplatform"), param.get("id"));
        if (affectRow != 1) {
            r.setCode(-1);
            r.setMsg("修改失败！");
        }
        return r;
    }

    @Override
    public ResultEntity relDeleteByIdList(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        StringBuffer sql = new StringBuffer(" DELETE FROM B_VERSION " +
                " WHERE ID=? ");
        ResultEntity r = new ResultEntity(1);
        for (int i = 0; i < id_s.length; i++) {
            Integer affectRow = DBHelper.execute(sql.toString(), id_s[i]);
            if (affectRow != 1) {
                throw new RuntimeException("编号为" + id_s[i] + "的信息删除失败 ");
            }
        }
        return r;
    }
}
