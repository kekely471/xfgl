package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.RyfxgdService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RyfxgdServiceImpl implements RyfxgdService {
    @Override
    public Page<?> findrygdPage(Map<String, String> param) {
        String userId = param.get("userId");
        ArrayList<Object> args = new ArrayList<>();
        StringBuffer sql= new StringBuffer();
        Map<String, Object> zw = DBHelper.queryForMap("select job,dwmc from tb_user where ss_id=?", userId);
        if ("0".equals(zw.get("job"))||"x".equals(zw.get("job"))) {
            sql.append("select t.id,t.ss_id,\n" +
                    "s.DEPTNAME dwmc_name,\n" +
                    "t.dwmc,t.name, (SELECT NAME FROM BUSINESS_DICT B where B.TYPE_CODE='sex' and b.value=t.sex)sex_name,\n" +
                    "sex,nl,mz,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx,rystatus, \n" +
                    "TO_CHAR(t.CREATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  CREATE_TIME,\n" +
                    "TO_CHAR(t.UPDATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  UPDATE_TIME , t.status,czr,sftj,\n" +
                    "case when t.STATUS=1 then '归档' when t.STATUS =0 then '未归档' when t.STATUS =2 then '未归档(退回)' end STATUSNAME ,\n" +
                    "  case when t.SFTJ= 0 THEN '否'  when t.STATUS =1  then '是' end SFTJNAME  from TB_RYFX t  left join TB_USER c on c.SS_ID = t.SS_ID left join SS_DEPT s on s.id=t.dwmc");
            //排除逻辑删除
            sql.append(" where 1=1  and t.sftj=1");
            sql.append(" order by t.UPDATE_TIME desc");
            Limit limit = new Limit(param);
            Page<?> result = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
            return result;
        } else if ("1".equals(zw.get("job"))||"2".equals(zw.get("job"))) {
            sql.append("select t.id,t.ss_id,\n" +
                    "s.DEPTNAME dwmc_name,\n" +
                    "t.dwmc,t.name, (SELECT NAME FROM BUSINESS_DICT B where B.TYPE_CODE='sex' and b.value=t.sex)sex_name,\n" +
                    "sex,nl,mz,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx,rystatus, \n" +
                    "TO_CHAR(t.CREATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  CREATE_TIME,\n" +
                    "TO_CHAR(t.UPDATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  UPDATE_TIME , t.status,czr,sftj,\n" +
                    "case when t.STATUS=1 then '归档' when t.STATUS =0 then '未归档' when t.STATUS =2 then '未归档(退回)' end STATUSNAME ,\n" +
                    "  case when t.SFTJ= 0 THEN '否'  when t.STATUS =1  then '是' end SFTJNAME  from TB_RYFX t  left join TB_USER c on c.SS_ID = t.SS_ID left join SS_DEPT s on s.id=t.dwmc");
            //排除逻辑删除
            sql.append(" where 1=1   and t.sftj=1");
            sql.append("   and s.parentid=? ");
            args.add(zw.get("dwmc"));
            sql.append(" order by t.UPDATE_TIME desc");
            Limit limit = new Limit(param);
            Page<?> result = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
            return result;
        }else{
            throw new JSONException("请联系支队管理");
        }
    }

    @Override
    public void ryfxgd(Map<String, String> param) {
        String id = param.get("id");
        String status = "1";
        Subject subject = SecurityUtils.getSubject();
        UserEntity userEntity = (UserEntity) subject.getPrincipal();
        String czrxm = userEntity.getUsername();
        if (StringUtils.isNotBlank(id)) {
            DBHelper.execute("update  TB_RYFX  set STATUS=? WHERE ID=?", status, id);
        } else {
            throw new JSONException("归档异常");
        }
    }

   /* @Override
    public Page<?> getrygd(Map<String, String> param) {
        String id = param.get("id");

//        String flag = param.get("flag");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append("select t.id,t.ss_id,(select DEPTNAME NAME  FROM SS_DEPT s WHERE  s.id=t.dwmc)dwmc_name,t.dwmc,t.name,(select name from  business_dict b WHERE  b.type_code='sex' AND b.value=t.sex)sexname,sex,\n" +
                "  nl,mz,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx,TO_CHAR(t.CREATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  CREATE_TIME,\n" +
                "  TO_CHAR(t.UPDATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  UPDATE_TIME,status,czr,sftj,\n" +
                "  case when t.STATUS=1 then '归档' when t.STATUS =0 then '未归档' end STATUSNAME,\n" +
                "  case when t.SFTJ= 0 THEN '否'  when t.STATUS =1  then '是' end SFTJNAME,t.RYSTATUS\n" +
                "   from TB_RYFX t left join TB_USER c on c.SS_ID = t.SS_ID where t.id=");
        sql.append(id);
        Limit limit = new Limit(param);
        Page<?> p = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
        return p;
    }*/
}
