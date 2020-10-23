package com.tonbu.web.app.itsm.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;

import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.helper.DateHelper;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.common.MapUtils;
import com.tonbu.web.admin.common.StrUtils;
import com.tonbu.web.app.itsm.service.ImportantPojoService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ImportantPojoServiceImpl implements ImportantPojoService {

    @Override
    public Page<?> loadList(Map<String, String> param) {
      List<Object> args = new ArrayList<Object>();
        Map<String, Object> usermap = getUserInfoObjectMap(param);

        StringBuilder sql = new StringBuilder();
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
         "  left join ss_dept d on d.id = u.dwmc     ");
//        args.add(param.get("saveorsubmit"));


        if(!usermap.get("JOB_TRUE").equals("0")){
            sql.append(" where u.ss_id =?");
            args.add(usermap.get("SS_ID"));
        }




        sql.append("  order by t.CREATE_DATE desc  ");


        Limit limit = new Limit(param);


        Page<?> page =DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());

        return page;


    }


    @Override
    public  Map<String, Object> getById(Map<String, String> param ) {
        String id=param.get("id");
        Map<String, Object> map = getStringObjectMap(id);
        if (map.size()!=0){

            List<Map<String,Object>> TB_YSDJ_CPCS=DBHelper.queryForList("select  id,ysdj_id,jcr,datetime,lby,jwry,jcqk  from TB_YSDJ_CPCS  where ysdj_id=?",id);
            if(TB_YSDJ_CPCS.size()>0){
            map.put("cpcs",MapUtils.listformLowerCase(TB_YSDJ_CPCS) );}
            List<Map<String,Object>> TB_YSDJ_LEAVE=DBHelper.queryForList("select  id,ysdj_id,xm,zw,sy,days,spr,leave_startdate, leave_enddate,leave_excess  from TB_YSDJ_LEAVE  where ysdj_id=?",id);
            if(TB_YSDJ_LEAVE.size()>0){
            map.put("leave",MapUtils.listformLowerCase(TB_YSDJ_LEAVE));}
            List<Map<String,Object>> TB_YSDJ_LSLDQS=DBHelper.queryForList("select  id,ysdj_id,xfyxm,qsxm,gx,time_laid,time_lid from TB_YSDJ_LSLDQS  where ysdj_id=?",id);
            if(TB_YSDJ_LSLDQS.size()>0){
            map.put("lsldqs",MapUtils.listformLowerCase(TB_YSDJ_LSLDQS));}
        }
        return map;
    }

    @Override
    public  List<Map<String,Object>> getByIdForLeave(Map<String, String> param ) {
        Map<String, Object> usermap = getUserInfoObjectMap(param);
        String datetime = StrUtils.trimToEmpty(param.get("date"));
        if ("".equals(datetime)){
            datetime= DateHelper.format(new Date(),"yyyy-MM-dd");

        }

        List<Map<String,Object>> list = new ArrayList<>();
           // List<Map<String,Object>> TB_YSDJ_LEAVE=DBHelper.queryForList("select  id,ysdj_id,xm,zw,sy,days,spr,leave_startdate, leave_enddate,leave_excess  from TB_YSDJ_LEAVE  where deptid=? and to_char(CREATEDATE, 'yyyy-MM-dd')  = ？ ",usermap.get("DWMC_TRUE"),datetime);
            List<Map<String,Object>> TB_YSDJ_LEAVE=DBHelper.queryForList("select  id,ysdj_id,xm,zw,sy,days,spr,leave_startdate, leave_enddate,leave_excess  from TB_YSDJ_LEAVE  where deptid= ? and to_char(CREATEDATE, 'yyyy-MM-dd')  = ? ",usermap.get("DWMC_TRUE"),datetime);
            if(TB_YSDJ_LEAVE.size()>0){
                list.addAll(MapUtils.listformLowerCase(TB_YSDJ_LEAVE));
            }
        return list;
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

    public Map<String, Object> getUserInfo(String userId) {
        //用户信息
        Map<String, Object> usermap = DBHelper.queryForMap("select  t.id,t.phone,t.email,t.idcard,t.name," +
                "t.avatar,t.acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)job,job as job_true," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK) as POLICERANK_NAME,POLICERANK, " +
                "to_char(CREATETIME,'yyyy-mm-dd hh24:mi')CREATETIME,to_char(UPDATETIME,'yyyy-mm-dd hh24:mi')UPDATETIME," +
                "RWNY,SS_ID,SFKYLDZS,TQXJTSSFJB,NXJTS,TQXJTS,NATIVE_PLACE,d.DEPTNAME as DWMC,DWMC as DWMC_TRUE    " +
                "  from   tb_user t  LEFT JOIN SS_DEPT D  ON D.id = t.DWMC  where t.SS_ID=? ", userId);
        return usermap;
    }

    @Override
    public void saveLeave(Map<String, String> param) {
//        String id=param.get("id");
//        Map<String, Object> ysdjList = getStringObjectMap(id);
        String datetime = StrUtils.trimToEmpty(param.get("date"));
        if ("".equals(datetime)){
            datetime=" to_char(sysdate, 'yyyy-MM-dd') ";
        }

        Map<String, Object> usermap = getUserInfoObjectMap(param);

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

    public Map<String, Object> getUserInfoObjectMap(Map<String, String> param) {
        UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

        //用户信息
        return getUserInfo(userEntity.getUser_id());
    }


    public void save(Map<String, String> param) {
        String id=DBHelper.queryForScalar("SELECT SEQ_GZQ.NEXTVAL from  DUAL",String.class);
        String  content=param.get("content");
        String publish_platform=param.get("publish_platform");
        String address=param.get("address");
        String name=param.get("userName");
        String  ss_id=param.get("ss_id");
        String tb_file_id=param.get("tb_file_id");

        if(null==tb_file_id||"".equals(tb_file_id)){
            tb_file_id="";
            DBHelper.execute("INSERT INTO TB_GZQ(ID,ACCOUNT,NAME,CONTENT,REMARK,STAR_NUM,GOOD_NUM,PUBLISH_PLATFORM,ADDRESS,CREATE_TIME,UPDATE_TIME,SS_ID)" +
                            "VALUES(?,?,?,?,?,?,?,?,?,SYSDATE,SYSDATE,?)",
                    id,"",name,content,"","","",publish_platform,address,ss_id);
        }else {
            String []file_id=tb_file_id.split(",");
            DBHelper.execute("INSERT INTO TB_GZQ(ID,ACCOUNT,NAME,CONTENT,REMARK,STAR_NUM,GOOD_NUM,PUBLISH_PLATFORM,ADDRESS,CREATE_TIME,UPDATE_TIME,SS_ID)VALUES(?,?,?,?,?,?,?,?,?,SYSDATE,SYSDATE,?)",
                    id,"",name,content,"","","",publish_platform,address,ss_id);
            for(int i=0;i<file_id.length;i++){
                String fileid=DBHelper.queryForScalar("SELECT SEQ_GZQ_FILES.NEXTVAL FROM DUAL",String.class);
                 String fileId=file_id[i];
                DBHelper.execute("INSERT INTO TB_GZQ_FILES(ID,GZQID,TB_FILE_ID,SMALL_TB_FILE_ID,FILETYPE,CREATE_TIME,UPDATE_TIME)VALUES(?,?,?,?,1,SYSDATE,SYSDATE)",
                        fileid,id,fileId,"");
            }
        }
    }
    public static String trimToEmpty(final Object str) {
        return str == null ? "" : (str + "").trim();
    }

    public static int trimToNum(final Object str) {
        return ((str == null) || str.equals("")) ? 0 : Integer.parseInt((str + "").trim());
    }


}
