package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.web.admin.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @program: xfgl
 * @author: keke
 * @create: 2020-04-26 10:22
 **/

public class BaseAction {


    @Autowired
    LeaveService leaveService;

    public static String trimToEmpty(final Object str) {
        return str == null ? "" : (str+"").trim();
    }

    public static int trimToNum(final Object str) {
        return ((str == null)||str.equals("")) ? 0 : Integer.parseInt ((str+"").trim());
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


    //计算ss_id的人员的请假剩余可用天数
    public int outLeftDays(String item){
        UserEntity userEntity = new UserEntity();
        userEntity.setUser_id(item);

        //用户信息
        Map<String, Object> usermap = getUserInfo(item);
        int allcount = 0;
        if ("1".equals(trimToEmpty(trimToNum(usermap.get("TQXJTSSFJB"))))) {
            allcount = (trimToNum(usermap.get("NXJTS")) + trimToNum(usermap.get("TQXJTS"))) / 2;
        } else {
            allcount = trimToNum(usermap.get("NXJTS")) + trimToNum(usermap.get("TQXJTS"));
        }
        //剩余
        int surplus = 0;


        BigDecimal b1 = new BigDecimal("0");
        if (usermap.size() > 1) {


            //探亲假
            List<Map<String, Object>> leavehistory2 = leaveService.selectLeaveHistory(userEntity, "2");
            if (leavehistory2.size() != 0) {
                for (Map<String, Object> leave : leavehistory2) {
                    BigDecimal b3 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                    b1 = b1.add(b3);
                }
            }


            //年假
//                if ((trimToEmpty(usermap.get("POLICERANK_NAME"))).indexOf("消防长")>-1) {
            List<Map<String, Object>> leavehistory3 = leaveService.selectLeaveHistory(userEntity, "3");
            if (leavehistory3.size() != 0) {
                for (Map<String, Object> leave : leavehistory3) {
                    BigDecimal b3 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                    b1 = b1.add(b3);
                }
            }
//                }


            //事假，一天以上
            List<Map<String, Object>> leavehistory1 = leaveService.selectLeaveHistory(userEntity, "1");
            //计算总天数
            if (leavehistory1.size() != 0) {
                BigDecimal b2 = new BigDecimal("1");
                for (Map<String, Object> leave : leavehistory1) {


                    BigDecimal b3 = new BigDecimal(trimToNum(leave.get("leave_days_true")));
                    //大于一天,计算入内(减去一天)
                    if (b3.compareTo(b2) > 0) {
                        BigDecimal b4 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                        if (b4.intValue() < 1) {
                            b4 = new BigDecimal("1");
                        }
                        b1 = b1.add(b4).subtract(b2);
                    }
                }
            }


            //计算还剩多少假期（探亲，年假）
            surplus = allcount - b1.intValue();
            if (surplus < 0) {
                surplus = 0;
            }
/*
                //离队住宿
                if ((trimToEmpty(usermap.get("SFKYLDZS"))).equals("1")){
                    List<Map<String, Object>> leavehistory7 = leaveService.selectLeaveHistory(userEntity,"7");
                    if (leavehistory7.size()!=0){
                        for (Map<String, Object> leave:leavehistory7) {
                            BigDecimal b3 = new BigDecimal(trimToEmpty(leave.get("leave_days_true"))) ;
                            b1 = b1.add(b3);
                        }
                    }
                }*/
        }
//        item.put("SYTS",surplus);

        return surplus ;
    }
}
