package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.RandomUtils;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.JhdyService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JhdyServiceImpl implements JhdyService {

    //查看当前用户提出的问题和非当前用户提出的公开问题
    @Override
    public Page<?> wtckList(Map<String, String> param) {
        String userId = param.get("userId");
        String rq = param.get("date");
        String dwmc = param.get("dwmc");
        String job = param.get("job");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        //需要进行判断消防员还是其他管理人员进行查看
        if(job.equals("") || job.equals("4") || job.equals("5")|| job.equals("6")){
            //消防员查看自己的和其他人公开的问题
            sql.append("SELECT " +
                    " t.ID," +
                    " t.TITLE," +
                    " t.DESCRIBE," +
                    " u.name," +
                    " t.STATUS," +
                    " t.SFGK, " +
                    "       to_char(t.CREATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS CREATE_TIME,"+
                    "       to_char(t.UPDATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME "+
                    " FROM TB_JHDYWT t " +
                    " LEFT JOIN tb_user u ON u.SS_ID = t.SS_ID " +
                    " WHERE 1 = 1 and t.SS_ID=? or t.SFGK=1 ");
            args.add(userId);
        }else if(job.equals("0")){
            sql.append("SELECT " +
                    " t.ID," +
                    " t.TITLE," +
                    " t.DESCRIBE," +
                    " u.name," +
                    " t.STATUS," +
                    " t.SFGK, " +
                    "       to_char(t.CREATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS CREATE_TIME,"+
                    "       to_char(t.UPDATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME "+
                    " FROM TB_JHDYWT t " +
                    " LEFT JOIN tb_user u ON u.SS_ID = t.SS_ID " +
                    " WHERE 1 = 1 ");

        }else {
            sql.append("SELECT " +
                    " t.ID," +
                    " t.TITLE," +
                    " t.DESCRIBE," +
                    " u.name," +
                    " t.STATUS," +
                    " t.SFGK, " +
                    "       to_char(t.CREATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS CREATE_TIME,"+
                    "       to_char(t.UPDATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME "+
                    " FROM TB_JHDYWT t " +
                    " LEFT JOIN tb_user u ON u.SS_ID = t.SS_ID " +
                    " WHERE 1 = 1  and t.SFGK=1 ");
        }

        if(StringUtils.isNotEmpty(rq)){
            String ks[]=rq.split(" - ");
            sql.append("AND to_char(t.UPDATE_TIME, 'YYYY-MM-DD ') between ? and  ?");
            args.add(ks[0]);
            args.add(ks[1]);
        }
        sql.append(" and t.ISDELETE=0  order by t.UPDATE_TIME desc");
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(),limit.getStart(),limit.getSize(),args.toArray());
    }


    /*
    手机端问题查看详情
     */
    @Override
    public Page<?> getwtck(Map<String, String> param) {
        String id = param.get("id");
        List<Object> args=new ArrayList<Object>();
        StringBuffer sql=new StringBuffer();
        sql.append("select a.ss_id,b.DWMC, a.id,c.DEPTNAME,b.NAME as FBR,u1.name as CLR,  " +
                "                a.TITLE,  " +
                "                a.DESCRIBE,  " +
                "                a.status,  " +
                "                a.SFGk,  " +
                "                d.FXDESCRIBE,  " +
                "                TO_CHAR( a.UPDATE_TIME , 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME  " +
                "                from  TB_JHDYWT a  " +
                "                LEFT JOIN TB_USER b on b.ss_id = a.ss_id  " +
                "                LEFT JOIN ss_dept c on c.id = b.DWMC  " +
                "                LEFT JOIN TB_JHDYFX d on d.wt_id = a.id " +
                "                left join tb_user u1 on u1.ss_id = d.clr_id where a.id=?");
        args.add(id);
        Limit limit = new Limit(param);

        return DBHelper.queryForPage(sql.toString(),limit.getStart(),limit.getSize(),args.toArray());

    }

    @Override
    public Page<?> wtglList(Map<String, String> param) {
        String userId = param.get("userId");
        String key = param.get("key_name");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT " +
                " t.ID," +
                " t.TITLE," +
                " t.DESCRIBE," +
                " u.name," +
                " t.STATUS," +
                " t.SFGK, " +
                "       to_char(t.CREATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS CREATE_TIME,"+
                "       to_char(t.UPDATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME "+
                " FROM TB_JHDYWT t " +
                " LEFT JOIN tb_user u ON u.SS_ID = t.SS_ID " +
                " WHERE 1= 1 ");
        if (StringUtils.isNotBlank(key)){
            sql.append("and t.TITLE like ?");
            args.add("%"+key+"%");
        }
        sql.append("and t.ISDELETE=0 and t.SS_ID=? order by t.UPDATE_TIME desc");
        args.add(userId);
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(),limit.getStart(),limit.getSize(),args.toArray());
    }

    @Override
    public Page<?> wtglList2(Map<String, String> param) {
        String userId = param.get("userId");
        String status = param.get("status");
        String job = param.get("job");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        //消防员只能查看自己的问题
        if (job.equals("") || job.equals("4") || job.equals("5")|| job.equals("6")){
            sql.append("SELECT " +
                    " t.ID," +
                    " t.TITLE," +
                    " t.DESCRIBE," +
                    " u.name," +
                    " t.STATUS," +
                    " c.deptname, " +
                    " c.id as dwid, " +
                    " d.FXDESCRIBE, " +
                    " t.SFGK, " +
                    "       to_char(t.CREATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS CREATE_TIME,"+
                    "       to_char(t.UPDATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME "+
                    " FROM TB_JHDYWT t " +
                    " LEFT JOIN tb_user u ON u.SS_ID = t.SS_ID " +
                    " LEFT JOIN ss_dept c ON c.ID = u.DWMC  " +
                    " LEFT JOIN TB_JHDYFX d ON d.wt_id = t.id  " +
                    " WHERE 1= 1 and t.status = ? ");
            args.add(status);


            sql.append("and t.ISDELETE=0  ");
            //已解答除了能看到自己的也要能看到别人公开的问题
            if ("1".equals(status)){
                sql.append("and ( t.SS_ID= ? or t.SFGK=1 )");
                args.add(userId);
            }else{
                sql.append("and  t.SS_ID= ?  ");
                args.add(userId);
            }
            sql.append(" order by t.UPDATE_TIME desc");
        }else if (job.equals("0")){
            //警务科科长
            sql.append("SELECT " +
                    "                 a.ID as ID," +
                    "                 a.TITLE, " +
                    "                 a.DESCRIBE, " +
                    "                a.STATUS," +
                    "                 a.SFGK," +
                    "                 b.name," +
                    "                 c.deptname, " +
                    "                 c.id as dwid, " +
                    "                 d.FXDESCRIBE, " +
                    "                 to_char(a.CREATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS CREATE_TIME,  " +
                    "                 to_char(d.UPDATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME  " +
                    "                 FROM TB_JHDYWT a  " +
                    "                 LEFT JOIN tb_user b ON b.SS_ID = a.SS_ID  " +
                    "                 LEFT JOIN ss_dept c ON c.ID = b.DWMC  " +
                    "                 LEFT JOIN TB_JHDYFX d ON d.wt_id = a.id  " +
                    "                 WHERE 1= 1 and a.status = ? ");
            args.add(status);
            if ("1".equals(status)){
                sql.append("and a.ISDELETE=0 order by d.UPDATE_TIME desc ");
            }else{
                sql.append("and a.ISDELETE=0 order by a.CREATE_TIME desc ");
            }
        }else{
            //其他管理员只能看到当前自己单位的消防员提出的问题
             sql.append("SELECT " +
                    "                                        a.ID as ID," +
                    "                                        a.TITLE, " +
                    "                                      a.DESCRIBE, " +
                    "                                     a.STATUS," +
                    "                                       a.SFGK," +
                    "                                        b.name," +
                    "                                       c.deptname, " +
                    "                                        c.id as dwid, " +
                    "                                        d.FXDESCRIBE, " +
                    "                                        to_char(a.CREATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS CREATE_TIME, " +
                     "                                       to_char(d.UPDATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME  " +
                     "                                        FROM TB_JHDYWT a  " +
                    "                                        LEFT JOIN tb_user b ON b.SS_ID = a.SS_ID  " +
                    "                                        LEFT JOIN ss_dept c ON c.ID = b.DWMC  " +
                    "                                        LEFT JOIN TB_JHDYFX d ON d.wt_id = a.id  " +
                    "                                         WHERE 1= 1 and a.status = ?  ");
            String dwmc = param.get("dwmc");
            args.add(status);

            if ("1".equals(status)){
                sql.append("and a.SFGK=1 and a.ISDELETE=0 order by d.UPDATE_TIME desc");
            }else{
                sql.append("and ( a.ss_id in (select SS_ID from tb_user  where dwmc=? and (job!='7'and job !='3' or job is null))) and a.ISDELETE=0 order by a.CREATE_TIME desc");
                args.add(dwmc);
            }



        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(),limit.getStart(),limit.getSize(),args.toArray());
    }

    @Override
    public Page<?> getwtgl(Map<String, String> param) {
        String id = param.get("id");
        List<Object> args=new ArrayList<Object>();
        StringBuffer sql=new StringBuffer();
        sql.append("SELECT t.ID,t.TITLE,t.DESCRIBE,u.name,t.STATUS,t.SFGK,TO_CHAR(t.CREATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS CREATE_TIME,TO_CHAR( t.UPDATE_TIME , 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME " +
                "                FROM TB_JHDYWT t LEFT JOIN tb_user u ON u.SS_ID = t.SS_ID WHERE t.ID=?");
        args.add(id);
        Limit limit = new Limit(param);

        return DBHelper.queryForPage(sql.toString(),limit.getStart(),limit.getSize(),args.toArray());
    }

    @Override
  //  @Transactional(value = "primaryTransactionManager")
    public void wtglsave(Map<String, String> param) {
        String id = param.get("id");
        String title = param.get("title");
        String describe = param.get("describe");
        String ss_id = param.get("userId");

        if (StringUtils.isBlank(id)){
            DBHelper.execute("INSERT INTO TB_JHDYWT(id,title,ss_id,describe,create_time,update_time，status,sfgk,ISDELETE) VALUES(SEQ_JHDYWT.NEXTVAL,?,?,?,SYSDATE,SYSDATE,?,?,?)",title,ss_id,describe,0,0,0);
        }else{
            String querySql ="select t.status from TB_JHDYWT t where t.id= ? " ;
            Map<String, Object> result = DBHelper.queryForMap(querySql, id);
            String status = result.get("STATUS").toString();
            if ("0".equals(status)){
                DBHelper.execute("UPDATE TB_JHDYWT SET title=?,describe=?,update_time=SYSDATE where id = ? "
                        ,title,describe,id);
            }else{
                throw new JSONException("已解答的问题不能修改");
            }
        }
    }


    @Override
    @Transactional(value = "primaryTransactionManager")
    public void wtgldel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        String querySql ="select t.status from TB_JHDYWT t where t.id= ? " ;
        for (int i = 0; i < id_s.length; i++) {
            Map<String, Object> result = DBHelper.queryForMap(querySql, ids);
            String status = result.get("STATUS").toString();
            if ("0".equals(status)){
                DBHelper.execute("UPDATE TB_JHDYWT SET ISDELETE=? WHERE ID = ? ", 1, id_s[i]);
            }else {
                throw new JSONException("已解答的问题不能删除");
            }
        }
    }

    @Override
    public Page<?> wtfxList(Map<String, String> param) {
        String key = param.get("key_name");
        String dw = param.get("dwmc");
        String rq = param.get("date");
        String status = param.get("status");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT " +
                " a.ID as wtid," +
                " a.TITLE," +
                " a.DESCRIBE," +
                " a.STATUS," +
                " a.SFGK," +
                " b.name," +
                " c.deptname, " +
                " c.id as dwid, " +
                " d.FXDESCRIBE," +
                " to_char(a.UPDATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME "+
                " FROM TB_JHDYWT a " +
                " LEFT JOIN tb_user b ON b.SS_ID = a.SS_ID " +
                " LEFT JOIN ss_dept c ON c.ID = b.DWMC " +
                " LEFT JOIN TB_JHDYFX d ON d.wt_id = a.id " +
                " WHERE 1= 1 ");
        if (StringUtils.isNotBlank(key)){
            sql.append("and a.TITLE like ?");
            args.add("%"+key+"%");
        }
        if(StringUtils.isNotEmpty(rq)){

            String ks[]=rq.split(" - ");
            sql.append("AND to_char(a.UPDATE_TIME, 'YYYY-MM-DD ') between ? and  ?");
            args.add(ks[0]);
            args.add(ks[1]);
        }
        if (StringUtils.isNotBlank(dw)){
            sql.append("and c.id like ? ");
            args.add("%"+dw+"%");
        }
        if (StringUtils.isNotBlank(status)){
            sql.append("and a.SFGK = ? ");
            args.add(status);
        }
        sql.append("and a.ISDELETE=0 order by a.UPDATE_TIME desc");
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(),limit.getStart(),limit.getSize(),args.toArray());
    }
    @Override
    public Page<?> wtfxList2(Map<String, String> param) {
        String key = param.get("key_name");
        String dw = param.get("dwmc");
        String rq = param.get("date");
        String SFGK = param.get("SFGK");
        String status = param.get("status");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT " +
                " a.ID as wtid," +
                " a.TITLE," +
                " a.DESCRIBE," +
                " a.STATUS," +
                " a.SFGK," +
                " b.name," +
                " c.deptname, " +
                " c.id as dwid, " +
                " d.FXDESCRIBE," +
                " to_char(a.UPDATE_TIME, 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME "+
                " FROM TB_JHDYWT a " +
                " LEFT JOIN tb_user b ON b.SS_ID = a.SS_ID " +
                " LEFT JOIN ss_dept c ON c.ID = b.DWMC " +
                " LEFT JOIN TB_JHDYFX d ON d.wt_id = a.id " +
                " WHERE 1= 1 and a.status =  ");
        sql.append(status);
        if (StringUtils.isNotBlank(key)){
            sql.append("and a.TITLE like ?");
            args.add("%"+key+"%");
        }
        if(StringUtils.isNotEmpty(rq)){

            String ks[]=rq.split(" - ");
            sql.append("AND to_char(a.UPDATE_TIME, 'YYYY-MM-DD ') between ? and  ?");
            args.add(ks[0]);
            args.add(ks[1]);
        }
        if (StringUtils.isNotBlank(dw)){
            sql.append("and c.id like ? ");
            args.add("%"+dw+"%");
        }
        if (StringUtils.isNotBlank(SFGK)){
            sql.append("and a.SFGK = ? ");
            args.add(SFGK);
        }
        sql.append("and a.ISDELETE=0 order by a.UPDATE_TIME desc");
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(),limit.getStart(),limit.getSize(),args.toArray());
    }

    @Override
    public Page<?> getwtfx(Map<String, String> param) {
        String id = param.get("id");
        List<Object> args=new ArrayList<Object>();
        StringBuffer sql=new StringBuffer();
        sql.append("select a.ss_id,b.DWMC, a.id,c.DEPTNAME,b.NAME as FBR,u1.name as CLR, " +
                "                a.TITLE, " +
                "                a.DESCRIBE," +
                "                a.status, " +
                "                a.SFGk, " +
                "                d.FXDESCRIBE, " +
                "                TO_CHAR( a.CREATE_TIME , 'yyyy/MM/dd HH24:mi:ss') AS CREATE_TIME,  " +
                "                TO_CHAR( d.UPDATE_TIME , 'yyyy/MM/dd HH24:mi:ss') AS UPDATE_TIME " +
                "                from  TB_JHDYWT a " +
                "                LEFT JOIN TB_USER b on b.ss_id = a.ss_id " +
                "                LEFT JOIN ss_dept c on c.id = b.DWMC " +
                "                LEFT JOIN TB_JHDYFX d on d.wt_id = a.id " +
                "                left join tb_user u1 on u1.ss_id = d.clr_id where a.id=?");
        args.add(id);
        Limit limit = new Limit(param);

        return DBHelper.queryForPage(sql.toString(),limit.getStart(),limit.getSize(),args.toArray());
    }

    @Override
    public void wtfxsave(Map<String, String> param) {
        String wid = param.get("id");
        String fxdescribe = param.get("fxdescribe");
        String sfgk = param.get("sfgk");
        String clr_id = param.get("userId");
        int count = DBHelper.queryForScalar("select count(WT_ID) from TB_JHDYFX where 1=1 and wt_id=?",Integer.class, wid);
        if (count==0){
            DBHelper.execute("INSERT INTO TB_JHDYFX(id,fxdescribe,update_time，clr_id,sfgk,wt_id) VALUES(SEQ_JHDYFX.NEXTVAL,?,SYSDATE,?,?,?)",fxdescribe,clr_id,sfgk,wid);
            DBHelper.execute("UPDATE TB_JHDYWT SET status=?，sfgk=? where id = ? ",1,sfgk,wid);
        }else{
            DBHelper.execute("UPDATE TB_JHDYFX set fxdescribe=?,update_time=SYSDATE,clr_id=?,sfgk=? where wt_id=?",fxdescribe,clr_id,sfgk,wid);
            DBHelper.execute("UPDATE TB_JHDYWT set update_time=SYSDATE,sfgk=? where id=?",sfgk,wid);
        }
    }
}
