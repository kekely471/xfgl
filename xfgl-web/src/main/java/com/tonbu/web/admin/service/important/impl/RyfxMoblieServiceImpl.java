package com.tonbu.web.admin.service.important.impl;

import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.support.helper.StringHelper;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.common.UserUtils;
import com.tonbu.web.admin.service.important.RyfxMoblieService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class RyfxMoblieServiceImpl implements RyfxMoblieService {

//    @Override
//    @Transactional(value = "primaryTransactionManager")
//    public int saveOrUpdate(Map<String, String> param) {
//        Integer czr = null;
//        String id = param.get("ID");
//        Integer reuslt = null;
//        //手机端保存保存数据
//        String userId = param.get("userId");
//        if (StringUtils.isNotBlank(userId)){
//            Map<String ,Object> user  = UserUtils.getUserByToken(userId);
//            czr =Integer.parseInt(user.get("SS_ID").toString());
//        }else{//PC端保存数据
//            UserEntity userEntity = CustomRealm.GetLoginUser();
//            //当前登录人ID
//            String user_id = userEntity.getUser_id();
//            czr = Integer.parseInt(user_id);//提交人ID
//        }
//        Integer zj = Integer.parseInt(param.get("ZJ"));//职级类型
//        String name = param.get("NAME");//消防员名称
//        Integer sex = Integer.parseInt(param.get("SEX"));//性别
//        String nl = param.get("NL");//年龄
//        String mz = param.get("MZ");//民族
//        Integer dwmc = Integer.parseInt(param.get("DWMC"));//单位类型
//        String sg=param.get("SG");//身高(米)
//        String jbzl=param.get("JBZL");//基本资料
//        String grczs=param.get("GRCZS");//个人成长史
//        String jszt=param.get("JSZT");//精神状态
//        String ss_id=param.get("SS_ID");//精神状态
//        String zw=DBHelper.queryForScalar("select t.policerank   from TB_USER t  where t.ss_id=?",String.class,ss_id);
//        String stzt=param.get("STZT");//身体状态
//        String shgn=param.get("SHGN");//社会功能
//        String grzxqk=param.get("GRZXQK");//个人征信情况
//        String czwt=param.get("CZWT");//存在问题
//        String dcfx=param.get("DCFX");//对策分析
//
//        String status = param.get("STATUS");//归档状态
////        String czr = param.get("CRZ");//操作人
//        String sftj = param.get("SFTJ");//是否提交
//
//        StringBuilder sql = new StringBuilder();
//        if("3".equals(zw)){
//            //新增
//            if (StringUtils.isBlank(id)){
//                id = StringHelper.getRandomUUID();//22
//                sql.append("insert into TB_RYFX (id,SS_ID,DWMC,NAME,SEX,NL,MZ,ZJ,SG,JBZL,GRCZS,JSZT,STZT,SHGN,GRZXQK,CZWT,DCFX,CREATE_TIME,UPDATE_TIME,STATUS,CZR,SFTJ) ");
//                sql.append("values (");
//                sql.append("?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,SYSDATE,SYSDATE,?,?,?");//保存状态
//                sql.append(")");
//                reuslt = DBHelper.execute(sql.toString(),id,ss_id,dwmc,name,sex,nl,mz,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx,status,czr,sftj);
//
//            }else{//修改
//                sql.append("update TB_RYFX set ");
//                Map<String,Object> args = new HashMap<>();
//                if (zj != null){
//                    sql.append("zj = :zj ,");
//                    args.put("zj",zj);
//                }
//                if (StringUtils.isNotBlank(name)){
//                    sql.append("name = :name,");
//                    args.put("name",name);
//                }
//                if (sex !=null){
//                    sql.append("sex = :sex,");
//                    args.put("sex",sex);
//                }
//                if (nl != null ){
//                    sql.append("nl =:nl ,");
//                    args.put("nl",nl);
//                }
//                if (mz != null){
//                    sql.append("mz =:mz ");
//                    args.put("mz",mz);
//                }
//                if (dwmc != null){
//                    sql.append("dwmc =:dwmc ");
//                    args.put("dwmc",dwmc);
//                }
//                if (sg != null){
//                    sql.append("sg =:sg ");
//                    args.put("sg",sg);
//                }
//                if (jbzl != null){
//                    sql.append("jbzl =:jbzl ");
//                    args.put("jbzl",jbzl);
//                }
//                if (grczs != null){
//                    sql.append("grczs =:grczs ");
//                    args.put("grczs",grczs);
//                }
//                if (jszt != null){
//                    sql.append("jszt =:jszt ");
//                    args.put("jszt",jszt);
//                }
//                if (ss_id != null){
//                    sql.append("ss_id =:ss_id ");
//                    args.put("ss_id",ss_id);
//                }
//                if (stzt != null){
//                    sql.append("stzt =:stzt ");
//                    args.put("stzt",stzt);
//                }
//                if (shgn != null){
//                    sql.append("shgn =:shgn ");
//                    args.put("shgn",shgn);
//                }
//                if (grzxqk != null){
//                    sql.append("grzxqk =:grzxqk ");
//                    args.put("grzxqk",grzxqk);
//                }
//                if (czwt != null){
//                    sql.append("czwt =:czwt ");
//                    args.put("czwt",czwt);
//                }
//                if (dcfx != null){
//                    sql.append("dcfx =:dcfx ");
//                    args.put("dcfx",dcfx);
//                }
//                if (StringUtils.isNotBlank(status)){
//                    sql.append("STATUS =:STATUS ,");
//                    args.put("STATUS",status);
//                }
//                if (StringUtils.isNotBlank(sftj)){
//                    sql.append("sftj =:sftj ,");
//                    args.put("sftj",sftj);
//                }
//
//                sql.append(" where id = :id");
//                args.put("id",id);
//                reuslt =  DBHelper.update(sql.toString(),args);
//            }
//            return reuslt;
//        }else {
//            throw new JSONException("该用户没有权限！请联系站长");
//        }
//
//    }

    @Override
    public Page<?> findwtjryfxPage(Map<String, String> param) {
        String userId = param.get("userId");
        String sftj = param.get("sftj");
        List<Object> args = new ArrayList<Object>();
        String zw = DBHelper.queryForScalar("select job  from TB_USER t where ss_id=?", String.class, userId);
        StringBuffer sql = new StringBuffer();
//        if("3".equals(zw)){
            sql.append("select t.id,t.ss_id,(select DEPTNAME NAME  FROM SS_DEPT s WHERE  s.id=t.dwmc)dwmc_name,t.dwmc,t.name, ")
                    .append("(SELECT NAME FROM BUSINESS_DICT B where B.TYPE_CODE='sex'  and b.value=t.sex)sex_name,sex,")
                    .append("nl,mz,(SELECT NAME FROM BUSINESS_DICT B where B.TYPE_CODE='zw'  and b.value=t.zj)zj_name,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx,")
                    .append(" TO_CHAR(t.CREATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  CREATE_TIME, ")
                    .append(" TO_CHAR(t.UPDATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  UPDATE_TIME, ")
                    .append(" status,czr,sftj,")
                    .append(" case when t.STATUS=1 then '归档' when t.STATUS =0 then '未归档'  when t.STATUS =2 then '未归档(退回)'end STATUSNAME ")
                    .append(" ,case when t.SFTJ= 0 THEN '否'  when t.STATUS =1  then '是' end SFTJNAME, ")
                    .append(" t.rystatus ")
                    .append(" from TB_RYFX t ")
                    .append(" left join TB_USER c on c.SS_ID = t.SS_ID ");
            //排除逻辑删除
            sql.append(" where 1=1 and (t.sftj= 0  or t.sftj=2)");
            sql.append(" and t.CZRID=?");
            args.add(userId);


            sql.append(" order by t.CREATE_TIME desc");
            Limit limit = new Limit(param);
            Page<?> result = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());

            return result;
//        }else {
//            throw new JSONException("请联系支队管理");
//        }

    }

    @Override
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
    }

    @Override
    public Page<?> getryfx(Map<String, String> param) {
        String id = param.get("id");
//        String flag = param.get("flag");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append("select t.id,t.ss_id,(select DEPTNAME NAME  FROM SS_DEPT s WHERE  s.id=t.dwmc)dwmc_name,t.dwmc,t.name,(select name from  business_dict b WHERE  b.type_code='sex' AND b.value=t.sex)sexname,sex,\n" +
                "  nl,mz,(select name from  business_dict b WHERE  b.type_code='zw' AND b.value=t.zj)zj_name,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx,TO_CHAR(t.CREATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  CREATE_TIME,\n" +
                "  TO_CHAR(t.UPDATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  UPDATE_TIME,status,czr,sftj,\n" +
                "  case when t.STATUS=1 then '归档' when t.STATUS =0 then '未归档' end STATUSNAME,\n" +
                "  case when t.SFTJ= 0 THEN '否'  when t.STATUS =1  then '是' end SFTJNAME,t.RYSTATUS\n" +
                "   from TB_RYFX t left join TB_USER c on c.SS_ID = t.SS_ID where t.id=");
        sql.append(id);
//        sql.append("and t.sftj=0 or t.sftj=2");
        Limit limit = new Limit(param);
        Page<?> p = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
        return p;
    }

    /**
     * 已提交
     *
     * @param param
     * @return
     */
    @Override
    public Page<?> findytjryfxPage(Map<String, String> param) {

        String userId = param.get("userId");
//        String flag = param.get("flag");
        List<Object> args = new ArrayList<Object>();
        String zw = DBHelper.queryForScalar("select job  from TB_USER t where ss_id=?", String.class, userId);
        StringBuffer sql = new StringBuffer();
//        if("3".equals(zw)){
            sql.append("select t.id,t.ss_id,(select DEPTNAME NAME  FROM SS_DEPT s WHERE  s.id=t.dwmc)dwmc_name,t.dwmc,t.name, ")
                    .append("(SELECT NAME FROM BUSINESS_DICT B where B.TYPE_CODE='sex'  and b.value=t.sex)sex_name,sex,")
                    .append("nl,mz,(SELECT NAME FROM BUSINESS_DICT B where B.TYPE_CODE='zw'  and b.value=t.zj)zj_name,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx,")
                    .append(" TO_CHAR(t.CREATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  CREATE_TIME, ")
                    .append(" TO_CHAR(t.UPDATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  UPDATE_TIME,")
                    .append(" status,czr,sftj,")
                    .append(" case when t.STATUS=1 then '已归档' when t.STATUS =0 then '未归档'  when t.STATUS =2 then '未归档(退回)'end STATUSNAME ")
                    .append(" ,case when t.SFTJ= 0 THEN '否'  when t.STATUS =1  then '是' end SFTJNAME ,t.rystatus")
                    .append(" from TB_RYFX t ")
                    .append(" left join TB_USER c on c.SS_ID = t.SS_ID ");
        sql.append(" where t.CZRID=?");
        args.add(userId);
        sql.append("and t.sftj=1");

            sql.append(" order by t.CREATE_TIME desc");
            Limit limit = new Limit(param);
            Page<?> result = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());

            return result;
     /*   }else {
            throw new JSONException("请联系支队管理");
        }*/

    }

    @Override
    public void del(Map<String, String> param) {
        String ids = param.get("id");
//        String[] id_s = ids.split("\\+");
        StringBuffer querySql = new StringBuffer();
        querySql.append("select t.sftj  from TB_RYFX t where t.ID = ?");
        Map<String, Object> result = DBHelper.queryForMap(querySql.toString(), ids);
            String sftj = result.get("SFTJ").toString();
            if ("0".equals(sftj)) {
                //只有保存状态下可以删除
                DBHelper.execute("delete from  TB_RYFX  where ID = ?", ids);
            } else {
                throw new JSONException("已提交的人员分析不能删除");
            }
    }

    /**
     * 审核
     *
     * @param param
     * @return
     */
    @Override
    public void ryfxsh(Map<String, String> param) {
        String id = param.get("id");
        String status = param.get("status");
        UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
        String czrxm = userEntity.getUsername();
        if (StringUtils.isNotBlank(id)) {
            DBHelper.execute("update  TB_RYFX  set STATUS=?  WHERE ID=?", status, id);
        } else {
            throw new JSONException("归档异常");
        }
    }

    @Override
    public void tj(Map<String, String> param) {
        String id =param.get("id");
        String userId = param.get("userId");
        String zj = param.get("zj");//职级类型

        String sex = param.get("sex");//性别
        String nl = param.get("nl");//年龄
        String mz = param.get("mz");//民族
        String dwmc = param.get("dwmc");//单位类型
        String sg = param.get("sg");//身高(米)
        String jbzl = param.get("jbzl");//基本资料
        String grczs = param.get("grczs");//个人成长史
        String jszt = param.get("jszt");//精神状态
        String ss_id = param.get("ss_id");//用户id
        String stzt = param.get("stzt");//身体状态
        String name = DBHelper.queryForScalar("select name from tb_user where ss_id=?", String.class, ss_id);
        String shgn = param.get("shgn");//社会功能
        String grzxqk = param.get("grzxqk");//个人征信情况
        String czwt = param.get("czwt");//存在问题
        String dcfx = param.get("dcfx");//对策分析
        String rystatus = param.get("rystatus");//状态
        String czrxm = DBHelper.queryForScalar("SELECT NAME FROM TB_USER WHERE SS_ID=?", String.class, userId);
            //新增
            if (StringUtils.isBlank(id)){
            String newid = DBHelper.queryForScalar("select SEQ_TB_RYFX.NEXTVAL from  DUAL", String.class);
            DBHelper.execute("insert into TB_RYFX (id,SS_ID,DWMC,NAME,SEX,NL,MZ,ZJ,SG,JBZL,GRCZS,JSZT,STZT,SHGN,GRZXQK,CZWT,DCFX,CREATE_TIME,UPDATE_TIME,STATUS,CZR,SFTJ,CZRID,RYSTATUS)" +
                            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,SYSDATE,SYSDATE,?,?,?,?,?)", newid, ss_id,
                    dwmc, name, sex, nl, mz, zj, sg, jbzl, grczs, jszt, stzt, shgn, grzxqk, czwt, dcfx, 0, czrxm, 1,userId,rystatus);

            }else {
                DBHelper.execute("UPDATE  TB_RYFX SET SS_ID=?,DWMC=?,NAME=?,SEX=?,NL=?,MZ=?,ZJ=?,SG=?,JBZL=?,GRCZS=?,JSZT=?,STZT=?,SHGN=?,GRZXQK=?,CZWT=?,DCFX=?,UPDATE_TIME=SYSDATE,CZR=?,SFTJ=?,CZRID=?,RYSTATUS=? WHERE ID=?"
                        ,ss_id,dwmc,name,sex,nl,mz,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx,czrxm,1,userId,rystatus,id);
            }

    }

    @Override
    public void bc(Map<String, String> param) {

        String id =param.get("id");
        String zj = param.get("zj");//职级类型

        String sex = param.get("sex");//性别
        String nl = param.get("nl");//年龄
        String mz = param.get("mz");//民族
        String dwmc = param.get("dwmc");//单位类型
        String sg = param.get("sg");//身高(米)
        String jbzl = param.get("jbzl");//基本资料
        String grczs = param.get("grczs");//个人成长史
        String jszt = param.get("jszt");//精神状态
        String ss_id = param.get("ss_id");//用户id
        String stzt = param.get("stzt");//身体状态
        String name = DBHelper.queryForScalar("select name from tb_user where ss_id=?", String.class, ss_id);
        String shgn = param.get("shgn");//社会功能
        String grzxqk = param.get("grzxqk");//个人征信情况
        String czwt = param.get("czwt");//存在问题
        String dcfx = param.get("dcfx");//对策分析
        String rystatus = param.get("rystatus");//对策分析


        UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
        String uid = userEntity.getUser_id();
        String zw = DBHelper.queryForScalar("select t.job   from TB_USER t  where t.ss_id=?", String.class, uid);
        String czrxm = userEntity.getUsername();
        System.out.println(czrxm);
        System.out.println(uid);
            //新增
            if (StringUtils.isBlank(id)){
            String newid = DBHelper.queryForScalar("select SEQ_TB_RYFX.NEXTVAL from  DUAL", String.class);
//                id = StringHelper.getRandomUUID();//22
            DBHelper.execute("insert into TB_RYFX (id,SS_ID,DWMC,NAME,SEX,NL,MZ,ZJ,SG,JBZL,GRCZS,JSZT,STZT,SHGN,GRZXQK,CZWT,DCFX,CREATE_TIME,UPDATE_TIME,STATUS,CZR,SFTJ,CZRID,RYSTATUS)" +
                            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,SYSDATE,SYSDATE,?,?,?,?,?)", newid, ss_id,
                    dwmc, name, sex, nl, mz, zj, sg, jbzl, grczs, jszt, stzt, shgn, grzxqk, czwt, dcfx, 0, czrxm, 0,uid,rystatus);

            }else {
                DBHelper.execute("UPDATE  TB_RYFX SET SS_ID=?,DWMC=?,NAME=?,SEX=?,NL=?,MZ=?,ZJ=?,SG=?,JBZL=?,GRCZS=?,JSZT=?,STZT=?,SHGN=?,GRZXQK=?,CZWT=?,DCFX=?,UPDATE_TIME=SYSDATE,CZR=?,SFTJ=? ,CZRID=?,RYSTATUS=? WHERE ID=?"
                        ,ss_id,dwmc,name,sex,nl,mz,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx,czrxm,0,uid,rystatus,id);
            }
    }

    @Override
    public List<Map<String, Object>> findusers(Map<String, String> param) {
        String ss_id = param.get("ss_id");
//        StringBuffer sql = new StringBuffer("SELECT *\n" +
//                "  FROM tb_ryfx \n" +
//                " WHERE TO_CHAR(create_time, 'YYYY-MM') =\n" +
//                "       TO_CHAR(ADD_MONTHS(SYSDATE, -1), 'YYYY-MM')\n ")
//                .append("  and ss_id=?");
//        sql.append(ss_id);

//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");//注意月份是MM
//        Date date1 = simpleDateFormat.parse(START_TIME);
//        Date date2 = simpleDateFormat.parse(END_TIME);
        Date date_year = new Date();


        List<Map<String, Object>> list = DBHelper.queryForList("select * from " +
                "(SELECT  t.* ,rownum" +
                "  FROM tb_ryfx t  " +
                " WHERE TO_CHAR(create_time, 'YYYY-MM') <?" +
                "  and ss_id=? order by create_time desc ) where rownum=1", monthNum(date_year,0),ss_id);
        return list;

    }
    public static String monthNum(Date time, Integer num){
        SimpleDateFormat formatym = new SimpleDateFormat("yyyy-MM");


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.MONTH, num);
        Date newTime = calendar.getTime();
        return formatym.format(newTime);
    }

    @Override
    public Page<?> findwgdryfxPage(Map<String, String> param) {
        String userId = param.get("userId");
//        String flag = param.get("flag");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
            Map<String, Object> zw = DBHelper.queryForMap("select job,dwmc from tb_user where ss_id=?", userId);
        if ("0".equals(zw.get("job"))||"x".equals(zw.get("job"))) {
            sql.append("select t.id,t.ss_id,\n" +
                    "                s.DEPTNAME dwmc_name,\n" +
                    "                t.dwmc,t.name, (SELECT NAME FROM BUSINESS_DICT B where B.TYPE_CODE='sex' and b.value=t.sex)sex_name,\n" +
                    "                sex,nl,mz,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx, \n" +
                    "                TO_CHAR(t.CREATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  CREATE_TIME,\n" +
                    "                TO_CHAR(t.UPDATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  UPDATE_TIME , t.status,czr,sftj,\n" +
                    "                case when t.STATUS=1 then '归档' when t.STATUS =0 then '未归档' when t.STATUS =2 then '未归档(退回)' end STATUSNAME ,\n" +
                    "                  case when t.SFTJ= 0 THEN '否'  when t.STATUS =1  then '是' end SFTJNAME ,t.rystatus from TB_RYFX t  left join TB_USER c on c.SS_ID = t.SS_ID left join SS_DEPT s on s.id=t.dwmc ");

            sql.append(" where 1=1 AND t.STATUS <> 1  and t.sftj=1");
            sql.append(" order by t.UPDATE_TIME desc");
            Limit limit = new Limit(param);
            Page<?> result = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());

            return result;
        } else if ("1".equals(zw.get("job"))||"2".equals(zw.get("job"))){


            sql.append("select t.id,t.ss_id,\n" +
                    "                s.DEPTNAME dwmc_name,\n" +
                    "                t.dwmc,t.name, (SELECT NAME FROM BUSINESS_DICT B where B.TYPE_CODE='sex' and b.value=t.sex)sex_name,\n" +
                    "                sex,nl,mz,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx, \n" +
                    "                TO_CHAR(t.CREATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  CREATE_TIME,\n" +
                    "                TO_CHAR(t.UPDATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  UPDATE_TIME , t.status,czr,sftj,\n" +
                    "                case when t.STATUS=1 then '归档' when t.STATUS =0 then '未归档' when t.STATUS =2 then '未归档(退回)' end STATUSNAME ,\n" +
                    "                  case when t.SFTJ= 0 THEN '否'  when t.STATUS =1  then '是' end SFTJNAME ,t.rystatus  from TB_RYFX t  left join TB_USER c on c.SS_ID = t.SS_ID  left join SS_DEPT s on s.id=t.dwmc ");

            sql.append(" where 1=1 AND t.STATUS <> 1 and t.sftj=1");
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
    public Page<?> findygdryfxPage(Map<String, String> param) {
        String userId = param.get("userId");
//        String flag = param.get("flag");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        Map<String, Object> zw = DBHelper.queryForMap("select job,dwmc from tb_user where ss_id=?", userId);
        if ("0".equals(zw.get("job"))||"x".equals(zw.get("job"))) {
            sql.append("select t.id,t.ss_id,\n" +
                    "s.DEPTNAME dwmc_name,\n" +
                    "t.dwmc,t.name, (SELECT NAME FROM BUSINESS_DICT B where B.TYPE_CODE='sex' and b.value=t.sex)sex_name,\n" +
                    "sex,nl,mz,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx, \n" +
                    "TO_CHAR(t.CREATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  CREATE_TIME,\n" +
                    "TO_CHAR(t.UPDATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  UPDATE_TIME , t.status,czr,sftj,\n" +
                    "case when t.STATUS=1 then '归档' when t.STATUS =0 then '未归档' when t.STATUS =2 then '未归档(退回)' end STATUSNAME ,\n" +
                    "  case when t.SFTJ= 0 THEN '否'  when t.STATUS =1  then '是' end SFTJNAME ,t.rystatus from TB_RYFX t  left join TB_USER c on c.SS_ID = t.SS_ID left join SS_DEPT s on s.id=t.dwmc");
            //排除逻辑删除
            sql.append(" where 1=1 AND t.STATUS =1 ");
            sql.append(" order by t.UPDATE_TIME desc");
            Limit limit = new Limit(param);
            Page<?> result = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
            return result;
        } else if ("1".equals(zw.get("job"))||"2".equals(zw.get("job"))) {
            sql.append("select t.id,t.ss_id,\n" +
                    "s.DEPTNAME dwmc_name,\n" +
                    "t.dwmc,t.name, (SELECT NAME FROM BUSINESS_DICT B where B.TYPE_CODE='sex' and b.value=t.sex)sex_name,\n" +
                    "sex,nl,mz,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx, \n" +
                    "TO_CHAR(t.CREATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  CREATE_TIME,\n" +
                    "TO_CHAR(t.UPDATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  UPDATE_TIME , t.status,czr,sftj,\n" +
                    "case when t.STATUS=1 then '归档' when t.STATUS =0 then '未归档' when t.STATUS =2 then '未归档(退回)' end STATUSNAME ,\n" +
                    "  case when t.SFTJ= 0 THEN '否'  when t.STATUS =1  then '是' end SFTJNAME ,t.rystatus from TB_RYFX t  left join TB_USER c on c.SS_ID = t.SS_ID left join SS_DEPT s on s.id=t.dwmc");
            //排除逻辑删除
            sql.append(" where 1=1 AND t.STATUS =1 ");
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
    public List<Map<String, Object>> qx(Map<String, String> param) {
        //获取shiro 缓存用户 信息
        UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
        //用户部职级
        return DBHelper.queryForList("select job  from TB_USER t where ss_id=?", userEntity.getUser_id());

    }
}
