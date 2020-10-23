package com.tonbu.web.admin.service.impl;

import com.alibaba.fastjson.*;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.helper.DateHelper;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.common.MapUtils;
import com.tonbu.web.admin.common.StrUtils;
import com.tonbu.web.admin.service.ImportService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.SimpleFormatter;

import static com.tonbu.web.admin.action.BaseAction.trimToEmpty;

@Service
public class ImportServiceImpl implements ImportService {



    //列表页面sql
    @Override
    public Page<?> loadList(Map<String, String> param) {
        List<Object> args= new ArrayList<Object>();
        String sj = param.get("key_name");
        Map<String, Object> usermap = getUserInfoObjectMap();
        StringBuilder sql=new StringBuilder();
        sql.append("select t.ID " +
                " ,t.DATETIME " +
                " ,t.WEEK " +
                " ,t.WEATHER " +
                " ,t.ZBY " +
                " ,t.CREATE_SS_ID " +
                " ,t.BHJCLQK " +
                " ,d.DEPTNAME " +
                " ,u.NAME " +
                " ,t.status " +
                "  from TB_YSDJ t" +
                "  left join tb_user u on u.ss_id = t.CREATE_SS_ID " +
                "  left join ss_dept d on d.id = u.dwmc  ");
        if (!usermap.get("JOB_TRUE").equals("0")){
            sql.append("where u.ss_id = ?");
            args.add(usermap.get("SS_ID"));
        }
        if(StringUtils.isNotBlank(sj)){
            sql.append(" and t.DATETIME like ? ");
            args.add("%" + sj + "%");
        }
        sql.append(" order by t.CREATE_DATE desc");
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(),limit.getStart(),limit.getSize(),args.toArray());
    }

    @Override
    public void saveLeave(Map<String, String> param) {
        String datetime = StrUtils.trimToEmpty(param.get("datetime"));
        if ("".equals(datetime)){
            datetime=" to_char(sysdate, 'yyyy-MM-dd') ";
        }
        Map<String, Object> usermap = getUserInfoObjectMap();
        String deptid=usermap.get("DWMC_TRUE")+"";
        List<Map<String, Object>> leaveList = DBHelper.queryForList("select * from TB_YSDJ_LEAVE where DEPTID=? and to_char(CREATEDATE, 'yyyy-MM-dd')  =? ",deptid,datetime);
        StringBuilder sql = new StringBuilder();
        sql.append("select t.*, tu.name,tu.DWMC,dept.deptname,j.name as zwname,to_char(t.start_time ,'yyyy/MM/dd HH24:mi:ss') as leavestart,to_char(t.end_time ,'yyyy/MM/dd HH24:mi:ss') as leaveend" +
                "  from TB_LEAVEAPPLY t " +
                "  left join tb_user tu " +
                "    on t.user_id = tu.ss_id " +
                "  left join business_dict d " +
                "    on d.value = t.leave_type and d.type_code = 'qjlx' " +
                "  left join business_dict j " +
                "    on j.value = tu.job and j.type_code = 'zw' " +
                "  LEFT JOIN SS_DEPT dept " +
                "    ON dept.id = tu.DWMC  " +
                " where tu.dwmc = ? " +
                "   and to_char(t.start_time, 'yyyy-MM-dd') <= ? " +
                "   and to_char(t.end_time, 'yyyy-MM-dd') >= ? " +
                "   and complete = 2 ");
        List<Map<String, Object>> haveLeaveList = DBHelper.queryForList(sql.toString(),deptid,datetime,datetime);
        if (leaveList.size()==0){
            if (haveLeaveList.size()>0){
                String finalDatetime = datetime;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date date = sdf.parse(finalDatetime);
                    //System.out.println(finalDatetime);
                    haveLeaveList.stream().forEach(item->{

                        DBHelper.execute("INSERT INTO TB_YSDJ_LEAVE(ID,XM,ZW,SY,DAYS,SPR,LEAVE_STARTDATE, LEAVE_ENDDATE,LEAVE_EXCESS,DEPTID,CREATEDATE)" +
                                        "VALUES(SEQ_ysdj_leave.NEXTVAL,?,?,?,?,?,?,?,?,?,?)",
                                //                    id,
                                item.get("NAME"),trimToEmpty(item.get("ZWNAME")),item.get("REASON"),item.get("LEAVE_DAYS_TRUE"),item.get("DEPTNAME"),item.get("LEAVESTART"),item.get("LEAVEEND"),"0",deptid,new Timestamp(date.getTime()));

                    });
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    /*@Override
    public List<Map<String,Object>> getById(Map<String, String> param) {
        String id=param.get("id");



            List<Map<String,Object>> TB_YSDJ_CPCS=DBHelper.queryForList("select  id,ysdj_id,jcr,datetime,lby,jwry,jcqk  from TB_YSDJ_CPCS  where ysdj_id=?",id);

            return TB_YSDJ_CPCS;

           *//* if(TB_YSDJ_CPCS.size()>0){
                map.put("cpcs",MapUtils.listformLowerCase(TB_YSDJ_CPCS) );}
            List<Map<String,Object>> TB_YSDJ_LEAVE=DBHelper.queryForList("select  id,ysdj_id,xm,zw,sy,days,spr,leave_startdate, leave_enddate,leave_excess  from TB_YSDJ_LEAVE  where ysdj_id=?",id);
            if(TB_YSDJ_LEAVE.size()>0){
                map.put("leave",MapUtils.listformLowerCase(TB_YSDJ_LEAVE));}
            List<Map<String,Object>> TB_YSDJ_LSLDQS=DBHelper.queryForList("select  id,ysdj_id,xfyxm,qsxm,gx,time_laid,time_lid from TB_YSDJ_LSLDQS  where ysdj_id=?",id);
            if(TB_YSDJ_LSLDQS.size()>0){
                map.put("lsldqs",MapUtils.listformLowerCase(TB_YSDJ_LSLDQS));}*//*



    }*/

    @Override
    public  List<Map<String,Object>> getByIdForLeave(Map<String, String> param ) {
        Map<String, Object> usermap = getUserInfoObjectMap();
        String datetime = StrUtils.trimToEmpty(param.get("datetime"));
        if ("".equals(datetime)){
           datetime= DateHelper.format(new Date(),"yyyy-MM-dd");
        }

        List<Map<String,Object>> list = new ArrayList<>();
        List<Map<String,Object>> TB_YSDJ_LEAVE=DBHelper.queryForList("select  id,ysdj_id,xm,zw,sy,days,spr,leave_startdate, leave_enddate,leave_excess  from TB_YSDJ_LEAVE  where deptid=? and to_char(CREATEDATE, 'yyyy-MM-dd')  = ? " ,usermap.get("DWMC_TRUE"),datetime);
        if(TB_YSDJ_LEAVE.size()>0){
            list.addAll(MapUtils.listformLowerCase(TB_YSDJ_LEAVE));
        }
        return list;
    }

    @Override
    public void save(Map<String, String> param) {
        String ss_id = param.get("userId");

        String id = param.get("id");
        String datetime = param.get("time");
        String week = param.get("WEEK");
        String weather = param.get("WEATHER");
        String zby = param.get("ZBY");
        String bz_gb_count = param.get("BZ_GB_COUNT");
        String bz_xfy_count = param.get("BZ_XFY_COUNT");
        String bz_count = param.get("BZ_COUNT");
        String xy_gb_count = param.get("XY_GB_COUNT");
        String xy_xfy_count = param.get("XY_XFY_COUNT");
        String xy_count = param.get("XY_COUNT");
        String rwqk_zc_nr = param.get("RWQK_ZC_NR");
        String rwqk_zc_yd_count = param.get("RWQK_ZC_YD_COUNT");
        String rwqk_zc_sd_count = param.get("RWQK_ZC_SD_COUNT");
        String rwqk_zc_percent = param.get("RWQK_ZC_PERCENT");
        String rwqk_sw_nr = param.get("RWQK_SW_NR");
        String rwqk_sw_yd_count = param.get("RWQK_SW_YD_COUNT");
        String rwqk_sw_sd_count = param.get("RWQK_SW_SD_COUNT");
        String rwqk_sw_percent = param.get("RWQK_SW_PERCENT");
        String rwqk_xw_nr = param.get("RWQK_XW_NR");
        String rwqk_xw_yd_count = param.get("RWQK_XW_YD_COUNT");
        String rwqk_xw_sd_count = param.get("RWQK_XW_SD_COUNT");
        String rwqk_xw_percent = param.get("RWQK_XW_PERCENT");
        String rwqk_ws_nr = param.get("RWQK_WS_NR");
        String rwqk_ws_yd_count = param.get("RWQK_WS_YD_COUNT");
        String rwqk_ws_sd_count = param.get("RWQK_WS_SD_COUNT");
        String rwqk_ws_percent = param.get("RWQK_WS_PERCENT");
        String gcqw = param.get("GCQW");
        String ryzbbd = param.get("RYZBBD");
        String fjjcqk = param.get("FJJCQK");
        String pbqk_zby_jiaobz = param.get("PBQK_ZBY_JIAOBZ");
        String pbqk_zby_jiebz = param.get("PBQK_ZBY_JIEBZ");
        String pbqk_zry_jiaobz = param.get("PBQK_ZRY_JIAOBZ");
        String pbqk_zry_jiebz = param.get("PBQK_ZRY_JIEBZ");
        String pbqk_cfzby_jiaobz = param.get("PBQK_CFZBY_JIAOBZ");
        String pbqk_cfzby_jiebz = param.get("PBQK_CFZBY_JIEBZ");
        String pbqk_zzz = param.get("PBQK_ZZZ");
        String pbqk_jj_date = param.get("PBQK_JJ_DATE");
        String pbqk_jj_qk = param.get("PBQK_JJ_QK");
        String bhjclqk = param.get("BHJCLQK");
        String zysx = param.get("ZYSX");
        Map<String, Object> usermap = getUserInfoObjectMap();
        String deptid=usermap.get("DWMC_TRUE")+"";



        //获取查铺查哨的数据循环获取
        //获取法人履历信息
        JSONArray cpcsxz = JSONObject.parseArray(param.get("cpcsxz"));
        JSONArray linshixz = JSONObject.parseArray(param.get("linshixz"));


        if (StringUtils.isBlank(id)) {

            String newid = DBHelper.queryForScalar("SELECT SEQ_YSDJ_CPCS.NEXTVAL FROM DUAL ", String.class);
            DBHelper.execute("insert into TB_YSDJ (id,datetime,week,weather,zby,bz_gb_count,bz_xfy_count,bz_count,xy_gb_count,xy_xfy_count,xy_count,rwqk_zc_nr,rwqk_zc_yd_count,rwqk_zc_sd_count,rwqk_sw_nr,rwqk_sw_yd_count,rwqk_sw_sd_count,rwqk_xw_nr,rwqk_xw_yd_count,rwqk_xw_sd_count,rwqk_ws_nr,rwqk_ws_yd_count,rwqk_ws_sd_count,gcqw,ryzbbd,fjjcqk,pbqk_zby_jiaobz,pbqk_zby_jiebz,pbqk_zry_jiaobz,pbqk_zry_jiebz,pbqk_cfzby_jiaobz,pbqk_cfzby_jiebz,pbqk_zzz,pbqk_jj_date,pbqk_jj_qk,bhjclqk,zysx,status,CREATE_SS_ID,CREATE_DATE,RWQK_ZC_PERCENT,RWQK_SW_PERCENT,RWQK_XW_PERCENT,RWQK_WS_PERCENT)" +
                    " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
                    "?,?,?,?,?,?,?,?,?,?,?,?,to_char(SYSDATE,'yyyy-mm-dd hh24:mi:ss'),?,?,?,?)", newid, datetime, week, weather, zby, bz_gb_count, bz_xfy_count, bz_count, xy_gb_count, xy_xfy_count, xy_count, rwqk_zc_nr, rwqk_zc_yd_count, rwqk_zc_sd_count, rwqk_sw_nr, rwqk_sw_yd_count, rwqk_sw_sd_count, rwqk_xw_nr, rwqk_xw_yd_count, rwqk_xw_sd_count, rwqk_ws_nr, rwqk_ws_yd_count, rwqk_ws_sd_count, gcqw, ryzbbd, fjjcqk, pbqk_zby_jiaobz, pbqk_zby_jiebz, pbqk_zry_jiaobz, pbqk_zry_jiebz, pbqk_cfzby_jiaobz, pbqk_cfzby_jiebz, pbqk_zzz, pbqk_jj_date, pbqk_jj_qk, bhjclqk, zysx, 0, ss_id,rwqk_zc_percent,rwqk_sw_percent,rwqk_xw_percent,rwqk_ws_percent);

            for (int i = 0; i < cpcsxz.size(); i++) {
                Map<String,Object> map = (Map<String, Object>) cpcsxz.get(i);
                String jcr = map.get("jcr").toString();
                String datetime2 = map.get("datetime2").toString();
                String lby = map.get("lby").toString();
                String jwry = map.get("jwry").toString();
                String jcqk = map.get("jcqk").toString();
                if (StringUtils.isBlank(jcr)){
                    continue;
                }


                String cpcs_id = DBHelper.queryForScalar("SELECT SEQ_YSDJ_CPCS.NEXTVAL FROM DUAL", String.class);

                DBHelper.execute("insert into TB_YSDJ_CPCS (id,YSDJ_ID,JCR,DATETIME,LBY,JWRY,JCQK) " +
                        " VALUES(?,?,?,?,?,?,?) ", cpcs_id, newid, jcr, datetime2, lby, jwry, jcqk);
            }

            for (int j=0;j<linshixz.size();j++){
                Map<String,Object> map = (Map<String, Object>) linshixz.get(j);
                String xfyxm = map.get("xfyxm").toString();
                String qsxm = map.get("qsxm").toString();
                String gx = map.get("gx").toString();
                String time_laid = map.get("time_laid").toString();
                String time_lid = map.get("time_lid").toString();
                if (StringUtils.isBlank(xfyxm)){
                    continue;
                }

                String linshi_id = DBHelper.queryForScalar("SELECT SEQ_YSDJ_LSLDQS.NEXTVAL FROM DUAL", String.class);

                DBHelper.execute("insert into TB_YSDJ_LSLDQS (id,YSDJ_ID,XFYXM,QSXM,GX,TIME_LAID,TIME_LID) " +
                        " VALUES(?,?,?,?,?,?,?) ", linshi_id, newid, xfyxm, qsxm, gx, time_laid, time_lid);

            }
            DBHelper.execute("update TB_YSDJ_LEAVE set ysdj_id=?  where deptid=? and to_char(CREATEDATE, 'yyyy-MM-dd')  = ?",newid,deptid,datetime);

        }else{
            DBHelper.execute("UPDATE TB_YSDJ SET weather=? ,zby=?, bz_gb_count=?,bz_xfy_count=?,bz_count=?,xy_gb_count=?,xy_xfy_count=?,xy_count=?,rwqk_zc_nr=?,rwqk_zc_yd_count=?,rwqk_zc_sd_count=?,rwqk_sw_nr=?,rwqk_sw_yd_count=?,rwqk_sw_sd_count=?,rwqk_xw_nr=?,rwqk_xw_yd_count=?,rwqk_xw_sd_count=?,rwqk_ws_nr=?,rwqk_ws_yd_count=?,rwqk_ws_sd_count=?,gcqw=?,ryzbbd=?,fjjcqk=?,pbqk_zby_jiaobz=?,pbqk_zby_jiebz=?,pbqk_zry_jiaobz=?,pbqk_zry_jiebz=?,pbqk_cfzby_jiaobz=?,pbqk_cfzby_jiebz=?,pbqk_zzz=?,pbqk_jj_date=?,pbqk_jj_qk=?,bhjclqk=?,zysx=?,RWQK_ZC_PERCENT=?,RWQK_SW_PERCENT=?,RWQK_XW_PERCENT=?,RWQK_WS_PERCENT=? where id=?",
                    weather,zby,bz_gb_count,bz_xfy_count,bz_count,xy_gb_count,xy_xfy_count,xy_count,rwqk_zc_nr,rwqk_zc_yd_count,rwqk_zc_sd_count,rwqk_sw_nr,rwqk_sw_yd_count,rwqk_sw_sd_count,rwqk_xw_nr,rwqk_xw_yd_count,rwqk_xw_sd_count,rwqk_ws_nr,rwqk_ws_yd_count,rwqk_ws_sd_count,gcqw,ryzbbd,fjjcqk,pbqk_zby_jiaobz,pbqk_zby_jiebz,pbqk_zry_jiaobz,pbqk_zry_jiebz,pbqk_cfzby_jiaobz,pbqk_cfzby_jiebz,pbqk_zzz,pbqk_jj_date,pbqk_jj_qk,bhjclqk,zysx,rwqk_zc_percent,rwqk_sw_percent,rwqk_xw_percent,rwqk_ws_percent,id);
           if (cpcsxz.size()>0){
                DBHelper.execute("delete from TB_YSDJ_CPCS where YSDJ_ID = ?",id);
               for (int i = 0; i < cpcsxz.size(); i++) {
                   Map<String,Object> map = (Map<String, Object>) cpcsxz.get(i);
                   String jcr = map.get("jcr").toString();
                   String datetime2 = map.get("datetime2").toString();
                   String lby = map.get("lby").toString();
                   String jwry = map.get("jwry").toString();
                   String jcqk = map.get("jcqk").toString();
                   if (StringUtils.isBlank(jcr)){
                       continue;
                   }
                   String cpcs_id = DBHelper.queryForScalar("SELECT SEQ_YSDJ_CPCS.NEXTVAL FROM DUAL", String.class);

                   DBHelper.execute("insert into TB_YSDJ_CPCS (id,YSDJ_ID,JCR,DATETIME,LBY,JWRY,JCQK) " +
                           " VALUES(?,?,?,?,?,?,?) ", cpcs_id, id, jcr, datetime2, lby, jwry, jcqk);
               }
           }

           if (linshixz.size()>0){
               DBHelper.execute("delete from TB_YSDJ_LSLDQS where YSDJ_ID = ?",id);

               for (int j=0;j<linshixz.size();j++){
                   Map<String,Object> map = (Map<String, Object>) linshixz.get(j);
                   String xfyxm = map.get("xfyxm").toString();
                   String qsxm = map.get("qsxm").toString();
                   String gx = map.get("gx").toString();
                   String time_laid = map.get("time_laid").toString();
                   String time_lid = map.get("time_lid").toString();
                   if (StringUtils.isBlank(xfyxm)){
                       continue;
                   }

                   String linshi_id = DBHelper.queryForScalar("SELECT SEQ_YSDJ_LSLDQS.NEXTVAL FROM DUAL", String.class);

                   DBHelper.execute("insert into TB_YSDJ_LSLDQS (id,YSDJ_ID,XFYXM,QSXM,GX,TIME_LAID,TIME_LID) " +
                           " VALUES(?,?,?,?,?,?,?) ", linshi_id, id, xfyxm, qsxm, gx, time_laid, time_lid);

               }
           }



        }



    }

    @Override
    public void del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        String querySql= "select t.status from TB_YSDJ t where t.ID = ?";
        for (int i = 0; i <id_s.length ; i++) {
            Map<String, Object> result = DBHelper.queryForMap(querySql, id_s[i]);
            String status = result.get("STATUS").toString();
            if ("0".equals(status)){
                //只有保存才可以删除
                DBHelper.execute("delete from TB_YSDJ where ID = ?",id_s[i]);
                DBHelper.execute("delete from TB_YSDJ_CPCS where YSDJ_ID = ?",id_s[i]);
                DBHelper.execute("delete from TB_YSDJ_LSLDQS where YSDJ_ID = ?",id_s[i]);
            }else {
                throw new JSONException("已提交的人员分析不能删除");
            }
        }

    }

    @Override
    public void tj(Map<String, String> param) {
        String ss_id = param.get("userId");

        String id = param.get("id");
        String datetime = param.get("time");
        String week = param.get("WEEK");
        String weather = param.get("WEATHER");
        String zby = param.get("ZBY");
        String bz_gb_count = param.get("BZ_GB_COUNT");
        String bz_xfy_count = param.get("BZ_XFY_COUNT");
        String bz_count = param.get("BZ_COUNT");
        String xy_gb_count = param.get("XY_GB_COUNT");
        String xy_xfy_count = param.get("XY_XFY_COUNT");
        String xy_count = param.get("XY_COUNT");
        String rwqk_zc_nr = param.get("RWQK_ZC_NR");
        String rwqk_zc_yd_count = param.get("RWQK_ZC_YD_COUNT");
        String rwqk_zc_sd_count = param.get("RWQK_ZC_SD_COUNT");
        String rwqk_zc_percent = param.get("RWQK_ZC_PERCENT");
        String rwqk_sw_nr = param.get("RWQK_SW_NR");
        String rwqk_sw_yd_count = param.get("RWQK_SW_YD_COUNT");
        String rwqk_sw_sd_count = param.get("RWQK_SW_SD_COUNT");
        String rwqk_sw_percent = param.get("RWQK_SW_PERCENT");
        String rwqk_xw_nr = param.get("RWQK_XW_NR");
        String rwqk_xw_yd_count = param.get("RWQK_XW_YD_COUNT");
        String rwqk_xw_sd_count = param.get("RWQK_XW_SD_COUNT");
        String rwqk_xw_percent = param.get("RWQK_XW_PERCENT");
        String rwqk_ws_nr = param.get("RWQK_WS_NR");
        String rwqk_ws_yd_count = param.get("RWQK_WS_YD_COUNT");
        String rwqk_ws_sd_count = param.get("RWQK_WS_SD_COUNT");
        String rwqk_ws_percent = param.get("RWQK_WS_PERCENT");
        String gcqw = param.get("GCQW");
        String ryzbbd = param.get("RYZBBD");
        String fjjcqk = param.get("FJJCQK");
        String pbqk_zby_jiaobz = param.get("PBQK_ZBY_JIAOBZ");
        String pbqk_zby_jiebz = param.get("PBQK_ZBY_JIEBZ");
        String pbqk_zry_jiaobz = param.get("PBQK_ZRY_JIAOBZ");
        String pbqk_zry_jiebz = param.get("PBQK_ZRY_JIEBZ");
        String pbqk_cfzby_jiaobz = param.get("PBQK_CFZBY_JIAOBZ");
        String pbqk_cfzby_jiebz = param.get("PBQK_CFZBY_JIEBZ");
        String pbqk_zzz = param.get("PBQK_ZZZ");
        String pbqk_jj_date = param.get("PBQK_JJ_DATE");
        String pbqk_jj_qk = param.get("PBQK_JJ_QK");
        String bhjclqk = param.get("BHJCLQK");
        String zysx = param.get("ZYSX");
        Map<String, Object> usermap = getUserInfoObjectMap();
        String deptid=usermap.get("DWMC_TRUE")+"";


        //获取查铺查哨的数据循环获取
        //获取法人履历信息
        JSONArray cpcsxz = JSONObject.parseArray(param.get("cpcsxz"));
        JSONArray linshixz = JSONObject.parseArray(param.get("linshixz"));

        //如果没有保存就直接保存，状态保存为1
        if (StringUtils.isBlank(id)) {
            String flag = DBHelper.queryForScalar("SELECT id FROM TB_YSDJ y where  substr(y.DATETIME,0,10)=? and y.create_ss_id=? " , String.class,datetime,ss_id);
            if(!trimToEmpty(flag).equals("")){
                throw new JSONException("已有数据");
            }

            String newid = DBHelper.queryForScalar("SELECT SEQ_YSDJ_CPCS.NEXTVAL FROM DUAL ", String.class);
            DBHelper.execute("insert into TB_YSDJ (id,datetime,week,weather,zby,bz_gb_count,bz_xfy_count,bz_count,xy_gb_count,xy_xfy_count,xy_count,rwqk_zc_nr,rwqk_zc_yd_count,rwqk_zc_sd_count,rwqk_sw_nr,rwqk_sw_yd_count,rwqk_sw_sd_count,rwqk_xw_nr,rwqk_xw_yd_count,rwqk_xw_sd_count,rwqk_ws_nr,rwqk_ws_yd_count,rwqk_ws_sd_count,gcqw,ryzbbd,fjjcqk,pbqk_zby_jiaobz,pbqk_zby_jiebz,pbqk_zry_jiaobz,pbqk_zry_jiebz,pbqk_cfzby_jiaobz,pbqk_cfzby_jiebz,pbqk_zzz,pbqk_jj_date,pbqk_jj_qk,bhjclqk,zysx,status,CREATE_SS_ID,CREATE_DATE,RWQK_ZC_PERCENT,RWQK_SW_PERCENT,RWQK_XW_PERCENT,RWQK_WS_PERCENT)" +
                    " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
                    "?,?,?,?,?,?,?,?,?,?,?,?,to_char(SYSDATE,'yyyy-mm-dd hh24:mi:ss'),?,?,?,?)", newid, datetime, week, weather, zby, bz_gb_count, bz_xfy_count, bz_count, xy_gb_count, xy_xfy_count, xy_count, rwqk_zc_nr, rwqk_zc_yd_count, rwqk_zc_sd_count, rwqk_sw_nr, rwqk_sw_yd_count, rwqk_sw_sd_count, rwqk_xw_nr, rwqk_xw_yd_count, rwqk_xw_sd_count, rwqk_ws_nr, rwqk_ws_yd_count, rwqk_ws_sd_count, gcqw, ryzbbd, fjjcqk, pbqk_zby_jiaobz, pbqk_zby_jiebz, pbqk_zry_jiaobz, pbqk_zry_jiebz, pbqk_cfzby_jiaobz, pbqk_cfzby_jiebz, pbqk_zzz, pbqk_jj_date, pbqk_jj_qk, bhjclqk, zysx, 1, ss_id,rwqk_zc_percent,rwqk_sw_percent,rwqk_xw_percent,rwqk_ws_percent);

            DBHelper.execute("update TB_YSDJ_LEAVE set ysdj_id=?  where deptid=? and to_char(CREATEDATE, 'yyyy-MM-dd')  =?",newid,deptid,datetime);

            for (int i = 0; i < cpcsxz.size(); i++) {
                Map<String,Object> map = (Map<String, Object>) cpcsxz.get(i);
                String jcr = map.get("jcr").toString();
                String datetime2 = map.get("datetime2").toString();
                String lby = map.get("lby").toString();
                String jwry = map.get("jwry").toString();
                String jcqk = map.get("jcqk").toString();
                if (StringUtils.isBlank(jcr)){
                    continue;
                }


                String cpcs_id = DBHelper.queryForScalar("SELECT SEQ_YSDJ_CPCS.NEXTVAL FROM DUAL", String.class);

                DBHelper.execute("insert into TB_YSDJ_CPCS (id,YSDJ_ID,JCR,DATETIME,LBY,JWRY,JCQK) " +
                        " VALUES(?,?,?,?,?,?,?) ", cpcs_id, newid, jcr, datetime2, lby, jwry, jcqk);
            }

            for (int j=0;j<linshixz.size();j++){
                Map<String,Object> map = (Map<String, Object>) linshixz.get(j);
                String xfyxm = map.get("xfyxm").toString();
                String qsxm = map.get("qsxm").toString();
                String gx = map.get("gx").toString();
                String time_laid = map.get("time_laid").toString();
                String time_lid = map.get("time_lid").toString();
                if (StringUtils.isBlank(xfyxm)){
                    continue;
                }

                String linshi_id = DBHelper.queryForScalar("SELECT SEQ_YSDJ_LSLDQS.NEXTVAL FROM DUAL", String.class);

                DBHelper.execute("insert into TB_YSDJ_LSLDQS (id,YSDJ_ID,XFYXM,QSXM,GX,TIME_LAID,TIME_LID) " +
                        " VALUES(?,?,?,?,?,?,?) ", linshi_id, newid, xfyxm, qsxm, gx, time_laid, time_lid);

            }
        }else {
            //若以保存则修改，并且状态改为1
            DBHelper.execute("UPDATE TB_YSDJ SET weather=? ,zby=?, bz_gb_count=?,bz_xfy_count=?,bz_count=?,xy_gb_count=?,xy_xfy_count=?,xy_count=?,rwqk_zc_nr=?,rwqk_zc_yd_count=?,rwqk_zc_sd_count=?,rwqk_sw_nr=?,rwqk_sw_yd_count=?,rwqk_sw_sd_count=?,rwqk_xw_nr=?,rwqk_xw_yd_count=?,rwqk_xw_sd_count=?,rwqk_ws_nr=?,rwqk_ws_yd_count=?,rwqk_ws_sd_count=?,gcqw=?,ryzbbd=?,fjjcqk=?,pbqk_zby_jiaobz=?,pbqk_zby_jiebz=?,pbqk_zry_jiaobz=?,pbqk_zry_jiebz=?,pbqk_cfzby_jiaobz=?,pbqk_cfzby_jiebz=?,pbqk_zzz=?,pbqk_jj_date=?,pbqk_jj_qk=?,bhjclqk=?,zysx=?,RWQK_ZC_PERCENT=?,RWQK_SW_PERCENT=?,RWQK_XW_PERCENT=?,RWQK_WS_PERCENT=?,STATUS=? where id=?",
                    weather,zby,bz_gb_count,bz_xfy_count,bz_count,xy_gb_count,xy_xfy_count,xy_count,rwqk_zc_nr,rwqk_zc_yd_count,rwqk_zc_sd_count,rwqk_sw_nr,rwqk_sw_yd_count,rwqk_sw_sd_count,rwqk_xw_nr,rwqk_xw_yd_count,rwqk_xw_sd_count,rwqk_ws_nr,rwqk_ws_yd_count,rwqk_ws_sd_count,gcqw,ryzbbd,fjjcqk,pbqk_zby_jiaobz,pbqk_zby_jiebz,pbqk_zry_jiaobz,pbqk_zry_jiebz,pbqk_cfzby_jiaobz,pbqk_cfzby_jiebz,pbqk_zzz,pbqk_jj_date,pbqk_jj_qk,bhjclqk,zysx,rwqk_zc_percent,rwqk_sw_percent,rwqk_xw_percent,rwqk_ws_percent,1,id);
            if (cpcsxz.size()>0){
                DBHelper.execute("delete from TB_YSDJ_CPCS where YSDJ_ID = ?",id);
                for (int i = 0; i < cpcsxz.size(); i++) {
                    Map<String,Object> map = (Map<String, Object>) cpcsxz.get(i);
                    String jcr = map.get("jcr").toString();
                    String datetime2 = map.get("datetime2").toString();
                    String lby = map.get("lby").toString();
                    String jwry = map.get("jwry").toString();
                    String jcqk = map.get("jcqk").toString();
                    if (StringUtils.isBlank(jcr)){
                        continue;
                    }
                    String cpcs_id = DBHelper.queryForScalar("SELECT SEQ_YSDJ_CPCS.NEXTVAL FROM DUAL", String.class);

                    DBHelper.execute("insert into TB_YSDJ_CPCS (id,YSDJ_ID,JCR,DATETIME,LBY,JWRY,JCQK) " +
                            " VALUES(?,?,?,?,?,?,?) ", cpcs_id, id, jcr, datetime2, lby, jwry, jcqk);
                }
            }

            if (linshixz.size()>0){
                DBHelper.execute("delete from TB_YSDJ_LSLDQS where YSDJ_ID = ?",id);

                for (int j=0;j<linshixz.size();j++){
                    Map<String,Object> map = (Map<String, Object>) linshixz.get(j);
                    String xfyxm = map.get("xfyxm").toString();
                    String qsxm = map.get("qsxm").toString();
                    String gx = map.get("gx").toString();
                    String time_laid = map.get("time_laid").toString();
                    String time_lid = map.get("time_lid").toString();
                    if (StringUtils.isBlank(xfyxm)){
                        continue;
                    }

                    String linshi_id = DBHelper.queryForScalar("SELECT SEQ_YSDJ_LSLDQS.NEXTVAL FROM DUAL", String.class);

                    DBHelper.execute("insert into TB_YSDJ_LSLDQS (id,YSDJ_ID,XFYXM,QSXM,GX,TIME_LAID,TIME_LID) " +
                            " VALUES(?,?,?,?,?,?,?) ", linshi_id, id, xfyxm, qsxm, gx, time_laid, time_lid);

                }
            }
        }
    }

   /* @Override
    public void getLeave(Map<String, String> param) {
        String time = param.get("time");
        String ss_id = param.get("userId");
        String flag = DBHelper.queryForScalar("SELECT id FROM TB_YSDJ y where  substr(y.DATETIME,0,10)=? and y.create_ss_id=? and y.STATUS=1" , String.class,time,ss_id);

    }*/


    public Map<String,Object> getUserInfoObjectMap(){
        UserEntity userEntity = CustomRealm.GetLoginUser();
        return getUserInfo(userEntity.getUser_id());

    }

    public Map<String,Object> getUserInfo(String userId){
        //用户信息
        Map<String,Object> usermap= DBHelper.queryForMap("select  t.id,t.phone,t.email,t.idcard,t.name,"  +
                                "t.avatar,t.acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)job,job as job_true," +
                                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK) as POLICERANK_NAME,POLICERANK, " +
                                " to_char(CREATETIME,'yyyy-mm-dd hh24:mi')CREATETIME,to_char(UPDATETIME,'yyyy-mm-dd hh24:mi')UPDATETIME," +
                                " RWNY,SS_ID,SFKYLDZS,TQXJTSSFJB,NXJTS,TQXJTS,NATIVE_PLACE,d.DEPTNAME as DWMC,DWMC as DWMC_TRUE    " +
                                "  from   tb_user t  LEFT JOIN SS_DEPT D  ON D.id = t.DWMC  where t.SS_ID=?",userId);
        return usermap;
    }

    public static String trimToEmpty(final Object str) {
        return str == null ? "" : (str + "").trim();
    }

    private Map<String, Object> getStringObjectMap(String id) {
        StringBuilder sql = new StringBuilder();
        sql.append("select t.ID " +
                " ,t.DATETIME " +
                " ,t.WEEK " +
                " ,t.WEATHER " +
                " ,t.ZBY " +
                " ,t.BZ_GB_COUNT " +
                " ,t.BZ_XFY_COUNT " +
                " ,t.BZ_COUNT " +
                " ,t.XY_GB_COUNT " +
                " ,t.XY_XFY_COUNT " +
                " ,t.XY_COUNT " +
                " ,t.RWQK_ZC_NR " +
                " ,t.RWQK_ZC_YD_COUNT " +
                " ,t.RWQK_ZC_SD_COUNT " +
                " ,t.RWQK_ZC_PERCENT " +
                " ,t.RWQK_SW_NR " +
                " ,t.RWQK_SW_YD_COUNT " +
                " ,t.RWQK_SW_SD_COUNT " +
                " ,t.RWQK_SW_PERCENT " +
                " ,t.RWQK_XW_NR " +
                " ,t.RWQK_XW_YD_COUNT " +
                " ,t.RWQK_XW_SD_COUNT " +
                " ,t.RWQK_XW_PERCENT " +
                " ,t.RWQK_WS_NR " +
                " ,t.RWQK_WS_YD_COUNT " +
                " ,t.RWQK_WS_SD_COUNT " +
                " ,t.RWQK_WS_PERCENT " +
                " ,t.GCQW " +
                " ,t.RYZBBD " +
                " ,t.FJJCQK " +
                " ,t.PBQK_ZBY_JIAOBZ " +
                " ,t.PBQK_ZBY_JIEBZ " +
                " ,t.PBQK_ZRY_JIAOBZ " +
                " ,t.PBQK_ZRY_JIEBZ " +
                " ,t.PBQK_CFZBY_JIAOBZ " +
                " ,t.PBQK_CFZBY_JIEBZ " +
                " ,t.PBQK_JJ_QK " +
                " ,t.PBQK_JJ_DATE " +
                " ,t.PBQK_ZZZ " +
                " ,t.ZYSX " +
                " ,t.STATUS " +
                " ,t.CREATE_SS_ID " +
                " ,t.CREATE_DATE " +
                " ,t.BHJCLQK " +
                " ,d.DEPTNAME " +
                " ,d.id as DEPTID " +
                "  from TB_YSDJ t" +
                "  left join tb_user u on u.ss_id = t.CREATE_SS_ID " +
                "  left join ss_dept d on d.id = u.dwmc " +
                "  where t.id = ? " +
                "  order by t.CREATE_DATE desc  ");

        return MapUtils.transformLowerCase(DBHelper.queryForMap(sql.toString(),id));
    }


}
