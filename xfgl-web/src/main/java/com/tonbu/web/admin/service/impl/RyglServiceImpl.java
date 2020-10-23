package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.support.util.CryptUtils;
import com.tonbu.web.AppConstant;
import com.tonbu.web.admin.service.RyglService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
@Service
public class RyglServiceImpl implements RyglService {

    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        String dwmc = param.get("dwmc");
        String job = param.get("job");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append( "select  t.id,t.phone,t.email,t.idcard,t.name,t.avatar,t.tqxjts,t.nxjts," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'sf'  AND BD.VALUE = t.TQXJTSSFJB)tqxjtssfjb," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'sf'  AND BD.VALUE = t.SFKYLDZS)sfkyldzs,t.acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)job, (SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK)POLICERANK," +
                "to_char(CREATETIME,'YYYY-MM-DD HH24:MI:SS')CREATETIME,to_char(UPDATETIME,'YYYY-MM-DD HH24:MI:SS')UPDATETIME,RWNY,SS_ID,d.DEPTNAME as DWMC,DWMC as DWMC_TRUE" +
                "    from   tb_user  t  LEFT JOIN SS_DEPT D  ON D.id = t.DWMC WHERE 1=1"); // 已经删除的记录
        if(StringUtils.isNotBlank(key)){
            sql.append(" and NAME like ? ");
            args.add("%"+key+"%");
        }
        if(StringUtils.isNotBlank(job)){
            sql.append(" and t.job = ? ");
            args.add(job);
        }
        if(StringUtils.isNotBlank(dwmc)){
            sql.append(" and  t.dwmc= ? ");
            args.add("" + dwmc + "");        }
        sql.append(" order by  UPDATETIME desc ");
        Limit limit = new Limit(param);
        Page<?> p = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
        return p;
    }

    @Override
    @Transactional(value = "primaryTransactionManager")
    public void save(Map<String, String> param) {
        String id = param.get("id");
        String phone = param.get("phone");
        String email = param.get("email");
        String idcard = param.get("idcard");
        String name = param.get("name");
        String avatar = param.get("avatar");
        String job = param.get("job");
        String dwmc = param.get("dwmc");
        String policerank = param.get("policerank");
        //此三项弃用
//        String dwbm = param.get("dwbm");
//        String sjdwbm = param.get("sjdwbm");
//        String sjdwmc = param.get("sjdwmc");
        String dwbm = "";
        String sjdwbm ="";
        String sjdwmc ="";
        String rwny=param.get("rwny");
        String tqxj = param.get("tqxjts");
        String nxjts = param.get("nxjts");
        String tqxjtssfjb = param.get("tqxjtssfjb");
        String sfkyldzs = param.get("sfkyldzs");
        String acc_status = param.get("acc_status") == null ? "0" : "1";

        UserEntity u = CustomRealm.GetLoginUser();
        if (StringUtils.isBlank(id)) {// 新增
            String password ="666666";
            String salt = CryptUtils.getRandomSalt(32);
            int countph = DBHelper.queryForScalar("select count(*) from tb_user where phone=?",Integer.class,phone);
            if(countph==0){
                String oldpwd = CryptUtils.md5(password, salt);
                String account_id=DBHelper.queryForScalar("select SEQ_ACCOUNT.NEXTVAL from  DUAL",String.class);
                DBHelper.execute("INSERT INTO ACCOUNT(ID,LOGINNAME,PASSWORD,STATUS,CREATE_TIME,UPDATE_TIME,SALT)VALUES(?,?,?,?,SYSDATE,SYSDATE,?)",
                        account_id,phone,oldpwd,1,salt);

                DBHelper.execute("insert into  ACCOUNT_ROLE(ACCOUNT_ID,ROLE_ID)VALUES(?,?)",account_id,"412");//412是消防员角色


                String ss_userid=DBHelper.queryForScalar("select SEQ_SS_USER.NEXTVAL from  DUAL",String.class);
                DBHelper.execute("insert into ACCOUNT_USER(ACCOUNT_ID,USER_ID)VALUES(?,?)",account_id,ss_userid);
                DBHelper.execute("insert into SS_USER(ID,USERNAME,MOBILE,EMAIL,CREATE_TIME,UPDATE_TIME,AVATAR,DEPT_ID,USER_TYPE,STATUS)VALUES(?,?,?,?,SYSDATE,SYSDATE,?,?,?,1)",
                        ss_userid,name,phone,email,avatar,1,1);



                String tb_userid=DBHelper.queryForScalar("select SEQ_TB_USER.NEXTVAL from  DUAL",String.class);
                DBHelper.execute("INSERT INTO TB_USER(ACCOUNT,ID,PHONE,EMAIL,IDCARD,NAME,AVATAR,ACC_STATUS,JOB,POLICERANK,DWBM,DWMC,SJDWBM,SJDWMC,RWNY,CREATETIME,UPDATETIME,SS_ID,TQXJTS,NXJTS,TQXJTSSFJB,SFKYLDZS) " +
                                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,to_date(?,'YYYY-MM-DD HH24:MI:SS'),to_date(?,'YYYY-MM-DD HH24:MI:SS'),?,?,?,?,?)",
                        phone,tb_userid,phone,email,idcard,name,avatar,acc_status,job,policerank,dwbm,dwmc,sjdwbm,sjdwmc,rwny,sf.format(new Date()),sf.format(new Date()),ss_userid,tqxj,nxjts,tqxjtssfjb,sfkyldzs);

            }else {
                throw  new JSONException("手机号已经存在");
            }


        }else {// 修改
            String sj=DBHelper.queryForScalar("select id  from tb_user where phone=?",String.class,phone);
            if(id.equals(sj)){
                String account_id=DBHelper.queryForScalar("select ID from  ACCOUNT WHERE LOGINNAME=?",String.class,phone);
                DBHelper.execute("UPDATE TB_USER SET PHONE=?,EMAIL=?,IDCARD=?,NAME=?,AVATAR=?,ACC_STATUS=?,JOB=?,POLICERANK=?,DWBM=?,DWMC=?,SJDWBM=?,UPDATETIME=to_date(?,'YYYY-MM-DD HH24:MI:SS'),SJDWMC=?,RWNY=?,TQXJTS=?,NXJTS=?,TQXJTSSFJB=?,SFKYLDZS=?  WHERE ID=?",
                        phone,email,idcard,name,avatar,acc_status,job,policerank,dwbm,dwmc,sjdwbm,sf.format(new Date()),sjdwmc,rwny,tqxj,nxjts,tqxjtssfjb,sfkyldzs,id);
                DBHelper.execute("UPDATE ACCOUNT SET LOGINNAME=?,UPDATE_TIME=SYSDATE WHERE ID=? ",phone,account_id);
                String uid=DBHelper.queryForScalar("select ID from  SS_USER WHERE mobile=?",String.class,phone);
                DBHelper.execute("UPDATE SS_USER  SET USERNAME=?,MOBILE=?,EMAIL=?,UPDATE_TIME=SYSDATE,AVATAR=? WHERE ID=?",name,phone,email,avatar,uid);

            }
//            DBHelper.execute("UPDATE  SET  LOGINNAME=?,PASSWORD=?,UPDATE_TIME=SYSDATE",name,666666);
        }

    }

    @Override
    public void del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            if (!StringUtils.equals("1", id_s[i])) { //根目录不能删除
                //tb_user寻找ss_id
                String ss_id = DBHelper.queryForScalar("select ss_id FROM TB_USER where  id =?",String.class,id_s[i]);

                DBHelper.execute("DELETE FROM TB_USER WHERE ID = ?", id_s[i]);

                DBHelper.execute("DELETE FROM SS_USER WHERE ID = ?", ss_id);
                //ACCOUNT_USER 表中 查找 account_id
                String account_id = DBHelper.queryForScalar("select ACCOUNT_ID FROM ACCOUNT_USER where  USER_ID =?",String.class,ss_id);

                DBHelper.execute("DELETE FROM ACCOUNT_USER WHERE account_id = ?", account_id);

                DBHelper.execute("DELETE FROM ACCOUNT_ROLE WHERE account_id = ? and role_id= 412", account_id);

                DBHelper.execute("DELETE FROM ACCOUNT WHERE ID = ?",account_id);

            } else {
                throw new JSONException("ROOT用户不能删除");
            }
        }
    }

    @Override
    public  String select(Map<String, String> param) {
        String phone=param.get("phone");
        String  m=DBHelper.queryForScalar("select count(*)as count from account  where loginname=?",String.class,phone);
        return m;
    }
}
