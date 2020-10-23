package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.common.UserUtils;
import com.tonbu.web.admin.service.KqglService;
import com.tonbu.web.admin.service.YdxfService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class YdxfServiceImpl implements YdxfService{
    @Override
    public Page<?> loadList(Map<String, String> param) {
//        String key = param.get("key_name");
//        String sj=param.get("sj");
        String dw=param.get("dwmc");
        String name=param.get("key_name");
        String rq=param.get("test6");

        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append( "select b.id,\n" +
                "       b.ss_id,\n" +
                "       b.username,\n" +
                "      to_char(b.create_time, 'YYYY-MM-DD HH24:MI:SS')as CREATETIME,\n" +
                "       b.phone,\n" +
                "       b.dept_id,\n" +
                "       (select t.deptname\n" +
                "          from ss_dept t where t.id=b.dept_id)dwmc_name,\n" +
                "       b.textbody,b.imel \n" +
                "  from TB_MONTHLYCONSUMPTION b\n" +
                "   where 1=1\n"); // 已经删除的记录
        if (StringUtils.isNotBlank(dw)) {
            sql.append(" AND b.dept_id = ? ");
            args.add("" + dw + "");
        }
        if (StringUtils.isNotBlank(name)) {
            sql.append(" AND b.USERNAME LIKE  ? ");
            args.add("%" + name + "%");
        }
        if(StringUtils.isBlank(rq)){
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD ");
            String kssj = sdf.format(new Date());

        }else {
            String ks[]=rq.split(" - ");
            sql.append("AND to_char(b.create_time, 'YYYY-MM-DD ') between ? and  ?");
            args.add(ks[0]);
            args.add(ks[1]);
        }

        Limit limit = new Limit(param);
        Page<?> p = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
        return p;
    }

    @Override
    public void save(Map<String, String> param) {
//        String id = param.get("id");
        String ss_id = param.get("ss_id");
        String imel = param.get("deviceId");
//        String username = param.get("username");
        String phone = param.get("phone");
//        String user_id = param.get("user_id");
        Map<String,Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId =user.get("SS_ID").toString();
        UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
        String name=userEntity.getUsername();
        String dept_id = DBHelper.queryForScalar("select dwmc from tb_user where ss_id=?",String.class,ss_id);
//        String tupian = "XFGL/"+param.get("avatar")+".png";
        String textbody = param.get("textbody");
        String id=DBHelper.queryForScalar("select SEQ_TB_MONTHLYCONSUMPTION.NEXTVAL from  DUAL", String.class);
//        UserEntity u = CustomRealm.GetLoginUser();
        DBHelper.execute("INSERT INTO TB_MONTHLYCONSUMPTION(ID,SS_ID,USERNAME,CREATE_TIME,PHONE,DEPT_ID,TEXTBODY,IMEL) " +
                            "VALUES(?,?,?,SYSDATE,?,?,?,?)",
                    id,ss_id,name,phone,dept_id,textbody,imel);
    }
}
