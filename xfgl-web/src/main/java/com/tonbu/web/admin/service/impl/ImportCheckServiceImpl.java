package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.common.MapUtils;
import com.tonbu.web.admin.service.ImportCheckService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
public class ImportCheckServiceImpl implements ImportCheckService {

    @Override
    public Page<?> loadList(Map<String, String> param) {
        List<Object> args = new ArrayList<Object>();
        String key = param.get("key_name");
        Map<String, Object> usermap = getUserInfoObjectMap();
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
                "  left join ss_dept d on d.id = u.dwmc  where 1=1 and t.status=1 ");
        if(StringUtils.isNotBlank(key)){
            sql.append(" and u.NAME like ? ");
            args.add("%" + key + "%");
        }

        sql.append("order by t.CREATE_DATE desc");
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }

    @Override
    public void saveLeave(Map<String, String> param) {
        Map<String, Object> usermap = getUserInfoObjectMap();
        String deptid=usermap.get("DWMC_TRUE")+"";
        List<Map<String, Object>> leaveList = DBHelper.queryForList("select * from TB_YSDJ_LEAVE where DEPTID=? and to_char(CREATEDATE, 'yyyy-MM-dd')  =to_char(SYSDATE, 'yyyy-MM-dd') ",deptid);
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
                "   and to_char(t.start_time, 'yyyy-MM-dd') <= to_char(sysdate, 'yyyy-MM-dd') " +
                "   and to_char(t.end_time, 'yyyy-MM-dd') >= to_char(sysdate, 'yyyy-MM-dd') " +
                "   and complete = 2 ");
        List<Map<String, Object>> haveLeaveList = DBHelper.queryForList(sql.toString(),deptid);
        if (leaveList.size()==0){
            if (haveLeaveList.size()>0){
                haveLeaveList.stream().forEach(item->{
                    DBHelper.execute("INSERT INTO TB_YSDJ_LEAVE(ID,XM,ZW,SY,DAYS,SPR,LEAVE_STARTDATE, LEAVE_ENDDATE,LEAVE_EXCESS,DEPTID,CREATEDATE)" +
                                    "VALUES(SEQ_ysdj_leave.NEXTVAL,?,?,?,?,?,?,?,?,?,SYSDATE)",
//                    id,
                            item.get("NAME"),trimToEmpty(item.get("ZWNAME")),item.get("REASON"),item.get("LEAVE_DAYS_TRUE"),item.get("DEPTNAME"),item.get("LEAVESTART"),item.get("LEAVEEND"),"0",deptid);
                });
            }
        }
    }

    @Override
    public List<Map<String, Object>> getByIdForLeave(Map<String, String> param) {
        Map<String, Object> usermap = getUserInfoObjectMap();

        List<Map<String,Object>> list = new ArrayList<>();
        List<Map<String,Object>> TB_YSDJ_LEAVE=DBHelper.queryForList("select  id,ysdj_id,xm,zw,sy,days,spr,leave_startdate, leave_enddate,leave_excess  from TB_YSDJ_LEAVE  where deptid=? and to_char(CREATEDATE, 'yyyy-MM-dd')  =to_char(SYSDATE, 'yyyy-MM-dd')",usermap.get("DWMC_TRUE"));
        if(TB_YSDJ_LEAVE.size()>0){
            list.addAll(MapUtils.listformLowerCase(TB_YSDJ_LEAVE));
        }
        return list;
    }


    public Map<String, Object> getUserInfoObjectMap() {
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

}

