package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.RyfxtjService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.tonbu.web.admin.service.important.impl.RyfxMoblieServiceImpl.monthNum;

@Service
public class RyfxtjServiceImpl implements RyfxtjService {


    /**
     * 提交信息查询
     */
    @Override
    public Page<?> findryfxPage(Map<String, String> param) {
        String userId = param.get("userId");
        String sftj = param.get("sftj");
        List<Object> args = new ArrayList<Object>();
        String zw = DBHelper.queryForScalar("select job  from TB_USER t where ss_id=?", String.class, userId);
        StringBuffer sql = new StringBuffer();
        sql.append("select t.id,t.ss_id,(select DEPTNAME NAME  FROM SS_DEPT s WHERE  s.id=t.dwmc)dwmc_name,t.dwmc,t.name, ")
                .append("(SELECT NAME FROM BUSINESS_DICT B where B.TYPE_CODE='sex'  and b.value=t.sex)sex_name,sex,")
                .append("nl,mz,(SELECT NAME FROM BUSINESS_DICT B where B.TYPE_CODE='zw'  and b.value=t.zj)zj_name,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx,")
                .append(" TO_CHAR(t.CREATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  CREATE_TIME, ")
                .append(" TO_CHAR(t.UPDATE_TIME,'YYYY-MM-DD HH24:MI:SS') as  UPDATE_TIME, ")
                .append(" status,czr,sftj,rystatus,")
                .append(" case when t.STATUS=1 then '归档' when t.STATUS =0 then '未归档'  when t.STATUS =2 then '未归档(退回)'end STATUSNAME ")
                .append(" ,case when t.SFTJ= 0 THEN '否'  when t.STATUS =1  then '是' end SFTJNAME ")
                .append(" from TB_RYFX t ")
                .append(" left join TB_USER c on c.SS_ID = t.SS_ID ");
        //排除逻辑删除
        sql.append(" where 1=1 ");
        sql.append(" and t.CZRID=?");
        args.add(userId);


        sql.append(" order by t.CREATE_TIME desc");
        Limit limit = new Limit(param);
        Page<?> result = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());

        return result;
    }

    @Override
    public void bc(Map<String, String> param) {
        UserEntity userEntity = CustomRealm.GetLoginUser();

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
        String ss_id = param.get("name");//用户id
        String stzt = param.get("stzt");//身体状态
        String name = DBHelper.queryForScalar("select name from tb_user where ss_id=?", String.class, ss_id);
        String shgn = param.get("shgn");//社会功能
        String grzxqk = param.get("grzxqk");//个人征信情况
        String czwt = param.get("czwt");//存在问题
        String dcfx = param.get("dcfx");//对策分析
        String rystatus = param.get("rystatus");//对策分析


//        UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
        String uid = userEntity.getUser_id();
        String zw = DBHelper.queryForScalar("select t.job   from TB_USER t  where t.ss_id=?", String.class, uid);
        String czrxm = userEntity.getUsername();
        //System.out.println(czrxm);
        //System.out.println(uid);
        //新增
        if (StringUtils.isBlank(id)){
            String newid = DBHelper.queryForScalar("select SEQ_TB_RYFX.NEXTVAL from  DUAL", String.class);
//                id = StringHelper.getRandomUUID();//22
            DBHelper.execute("insert into TB_RYFX (id,SS_ID,DWMC,NAME,SEX,NL,MZ,ZJ,SG,JBZL,GRCZS,JSZT,STZT,SHGN,GRZXQK,CZWT,DCFX,CREATE_TIME,UPDATE_TIME,STATUS,CZR,SFTJ,CZRID,RYSTATUS)" +
                            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,SYSDATE,SYSDATE,?,?,?,?,?)", newid, ss_id,
                    dwmc, name, sex, nl, mz, zj, sg, jbzl, grczs, jszt, stzt, shgn, grzxqk, czwt, dcfx, 0, czrxm, 0,uid,rystatus);

        }else {
            DBHelper.execute("UPDATE  TB_RYFX SET SS_ID=?,NAME=?,DWMC=?,SEX=?,NL=?,MZ=?,ZJ=?,SG=?,JBZL=?,GRCZS=?,JSZT=?,STZT=?,SHGN=?,GRZXQK=?,CZWT=?,DCFX=?,UPDATE_TIME=SYSDATE,CZR=?,SFTJ=? ,CZRID=?,RYSTATUS=? WHERE ID=?"
                    ,ss_id,name,dwmc,sex,nl,mz,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx,czrxm,0,uid,rystatus,id);
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
        String ss_id = param.get("name");//用户id
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
            DBHelper.execute("UPDATE  TB_RYFX SET SS_ID=?,NAME=?,DWMC=?,SEX=?,NL=?,MZ=?,ZJ=?,SG=?,JBZL=?,GRCZS=?,JSZT=?,STZT=?,SHGN=?,GRZXQK=?,CZWT=?,DCFX=?,UPDATE_TIME=SYSDATE,CZR=?,SFTJ=?,CZRID=?,RYSTATUS=? WHERE ID=?"
                    ,ss_id,name,dwmc,sex,nl,mz,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx,czrxm,1,userId,rystatus,id);
        }
    }

    @Override
    public List<Map<String, Object>> findusers(Map<String, String> param) {
        String ss_id = param.get("userId");
        Date date_year = new Date();
        List<Map<String, Object>> list = DBHelper.queryForList("select * from " +
                "(SELECT  t.* ,rownum" +
                "  FROM tb_ryfx t  " +
                " WHERE TO_CHAR(create_time, 'YYYY-MM') <?" +
                "  and ss_id=? order by create_time desc ) where rownum=1", monthNum(date_year,0),ss_id);
        return list;
    }

    @Override
    public void del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        String querySql ="select t.sftj  from TB_RYFX t where t.ID = ?";
        for (int i = 0; i < id_s.length; i++) {
            Map<String, Object> result = DBHelper.queryForMap(querySql, ids);
            String sftj = result.get("SFTJ").toString();
            if ("0".equals(sftj)) {
                //只有保存状态下可以删除
                DBHelper.execute("delete from  TB_RYFX  where ID = ?", ids);
            } else {
                throw new JSONException("已提交的人员分析不能删除");
            }
        }

    }
}
