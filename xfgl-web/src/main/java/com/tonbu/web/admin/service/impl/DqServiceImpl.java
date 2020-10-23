package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.DqService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.awt.geom.Point2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
@Service
public class DqServiceImpl implements DqService{

    @Override
    public String Dq(Map<String, String> param, UserEntity u, HttpServletRequest request) {
        //        String id= param.get("id");
        String jd=param.get("jd");//经度
        String wd=param.get("wd");//纬度
        String account=u.getLoginname();//账号
        String dz=param.get("dz");//地址
        String username=u.getUsername();//用户id
//        String rq =param.get("moblietime");
        String rq =param.get("rq");//日期年月日
        String sj =param.get("sj");//时分秒
        String type=param.get("type");//上下班1上班2下班
        String dklx=param.get("dklx");//打卡类型 1手动 2自动
        String imel=param.get("deviceId");//设备编码
        String mobile=rq+" "+sj;//手机时间
        String sbtime = "";//上班是否打卡
        String xbtime = "";//下班是否打卡
        Map<String, Object> usermap = getUserInfo(u.getUser_id());
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String str = sf.format(date);//当前时间时分
/*        String isunit = DBHelper.queryForScalar("select ISUNIT from ss_dept where status=1 and id=?",String.class,usermap.get("DWMC_TRUE"));
        if(!StringUtils.equals("1",isunit)){
            if(dklx.equals("2")) {
                return "";
            }else{
                return "此模块尚未对您开放！";
            }
        }*/
        Map<String, Object> map = DBHelper.queryForMap("select * from TB_KQPZ where bm=?", usermap.get("DWMC_TRUE"));
        if (map == null) {
            if(dklx.equals("2")) {
                return "";
            }else{
                return "您的部门未添加考勤配置，请先添加！";
            }

        }

        String jd1 = map.get("jd").toString();
        String wd1 = map.get("wd").toString();
        String fw = map.get("fw").toString();

        Point2D pointDD = new Point2D.Double(Double.valueOf(jd),Double.valueOf(wd));
        Point2D pointXD = new Point2D.Double(Double.valueOf(jd1),Double.valueOf(wd1));
        double doc = getDistance(pointDD, pointXD);

        if(doc>Double.valueOf(fw)){
            if(dklx.equals("2")) {
                return "";
            }else{
                return "不在范围内";
            }
        }

        //判断自动打卡还是手动打卡的
        if(dklx.equals("2")) {
            String sbsj = map.get("sbsj").toString();
            String xbsj = map.get("xbsj").toString();
            String[] sbsjs = sbsj.split("-");
            String[] xbsjs = xbsj.split("-");
            sbsjs[0].replace(" ", "");
            sbsjs[1].replace(" ", "");
            xbsjs[0].replace(" ", "");
            xbsjs[1].replace(" ", "");

            if(isEffectiveDate(str, sbsjs[0], sbsjs[1])){//上班时间段
                type = "1";
                sbtime=DBHelper.queryForScalar("select sbtime from TB_KQ_ATTENDANCE where to_char(create_time,'yyyy-MM-DD')=? and account=?  and checktype is null",String.class,rq,account);
                if(StringUtils.isNotBlank(sbtime)){
                    return "";
                }
            }else if(isEffectiveDate(str, xbsjs[0], xbsjs[1])){//下班时间段
                type = "2";
                xbtime=DBHelper.queryForScalar("select xbtime from TB_KQ_ATTENDANCE where to_char(create_time,'yyyy-MM-DD')=? and account=? and checktype is null ",String.class,rq,account);
                if(StringUtils.isNotBlank(xbtime)){
                    return "";
                }
            }else{
                return "";
            }
        }

        String id = DBHelper.queryForScalar("select SEQ_TB_DQ.nextval from dual ",String.class);
        if(type!=null){
            String sfjt=DBHelper.queryForScalar("select case when to_char(v.update_time,'yyyy-mm-dd')=to_char(sysdate,'yyyy-mm-dd') then '1' else '0' end as sfjt\n" +
                            " from (select * from TB_KQ_ATTENDANCE where account=?  and checktype is null order by update_time desc)v where rownum=1",
                    String.class,account);
            if("1".equals(sfjt)){
//                if("1".equals(type)){
//                    DBHelper.execute("UPDATE TB_KQ_ATTENDANCE SET SBTIME=SYSDATE WHERE ACCOUNT=?",u.getLoginname());
//                }
                if("2".equals(type)){
                    xbtime=DBHelper.queryForScalar("select xbtime from TB_KQ_ATTENDANCE where to_char(create_time,'yyyy-MM-DD')=? and account=?  and checktype is null",String.class,rq,account);
                    if(StringUtils.isNotBlank(xbtime)){
                        return "请勿重复打卡！";
                    }
                    DBHelper.execute("UPDATE TB_KQ_ATTENDANCE SET XBTIME=SYSDATE WHERE ACCOUNT=?  and checktype is null",u.getLoginname());
                    DBHelper.execute("INSERT INTO SS_LOG_LOGIN (ID,USER_ID,LOGINNAME,USERNAME,LOGINTIME,TYPE,LOGINIP,REMARK,STATUS) VALUES (SEQ_SS_LOG.NEXTVAL,?,?,?,SYSDATE,?,?,?,?)",
                            u.getUser_id(), account, username, "下班打卡", request.getRemoteAddr(), "", 1);
                }
            }
            if ("0".equals(sfjt)||null==sfjt) {
                if("1".equals(type)){
                    sbtime=DBHelper.queryForScalar("select sbtime from TB_KQ_ATTENDANCE where to_char(create_time,'yyyy-MM-DD')=? and account=?  and checktype is null",String.class,rq,account);
                    if(StringUtils.isNotBlank(sbtime)){
                        return "请勿重复打卡！";
                    }
                    DBHelper.execute("insert  into TB_KQ_ATTENDANCE(ID,ACCOUNT,SBTIME,USERNAME,CREATE_TIME,UPDATE_TIME,JD,WD,MOBLIETIME,DZ,IMEL)VALUES(?,?,SYSDATE,?,SYSDATE,SYSDATE,?,?,to_date(?,'yyyy-mm-dd hh24:mi:ss'),?,?)",
                            id,account,username,jd,wd,mobile,dz,imel);
                    DBHelper.execute("INSERT INTO SS_LOG_LOGIN (ID,USER_ID,LOGINNAME,USERNAME,LOGINTIME,TYPE,LOGINIP,REMARK,STATUS) VALUES (SEQ_SS_LOG.NEXTVAL,?,?,?,SYSDATE,?,?,?,?)",
                            u.getUser_id(), account, username, "上班打卡", request.getRemoteAddr(), "", 1);
                }
//                if("2".equals(type)){
//                    DBHelper.execute("insert  into TB_KQ_ATTENDANCE(ID,ACCOUNT,XBTIME,USERNAME,CREATE_TIME,UPDATE_TIME,JD,WD,MOBLIETIME,DZ)VALUES(?,?,SYSDATE,?,SYSDATE,SYSDATE,?,?,to_date(?,'yyyy-mm-dd hh24:mi:ss'),?)",
//                            id,account,username,jd,wd,mobile,dz);
//                }
            }
        }

        return "打卡成功";
    }

    @Override
    public List<Map<String, Object>> getDq(Map<String, String> param,UserEntity u) {
        if(u.getLoginname()==null){
           throw  new JSONException("没有该用户");
        }
        System.out.println(u.getLoginname());
        List<Map<String,Object>> list = DBHelper.queryForList("SELECT ID,ACCOUNT,\n" +
                "(select TO_CHAR(e.SBTIME, 'yyyy-MM-dd HH24:mi:ss') from TB_KQ_ATTENDANCE e where e.account = ? and to_char(e.create_time, 'yyyy-MM-dd') = TO_CHAR(SYSDATE, 'yyyy-MM-dd') and  checktype is null) as SBTIME,\n" +
                "(select TO_CHAR(e.XBTIME, 'yyyy-MM-dd HH24:mi:ss') from TB_KQ_ATTENDANCE e where e.account = ? and to_char(e.create_time, 'yyyy-MM-dd') = TO_CHAR(SYSDATE, 'yyyy-MM-dd') and checktype is null) as XBTIME,CHECKTYPE,USERNAME,JD,WD,DZ,\n" +
                "TO_CHAR(MOBLIETIME,'yyyy-MM-dd HH24:mi:ss')MOBLIETIME,TO_CHAR(CREATE_TIME,'yyyy-MM-dd HH24:mi:ss')CREATE_TIME,\n" +
                "TO_CHAR(UPDATE_TIME,'yyyy-MM-dd HH24:mi:ss')UPDATE_TIME,  (select TO_CHAR(sbtime, 'yyyy-MM-dd HH24:mi:ss')  from TB_KQ_ATTENDANCE where id in (select max(id)  from TB_KQ_ATTENDANCE where account = ? and checktype = '3' and to_char(create_time, 'yyyy-MM-dd') = TO_CHAR(SYSDATE, 'yyyy-MM-dd'))) as SPOTTIME  FROM TB_KQ_ATTENDANCE t" +
                "  where  id in (select max(id) from TB_KQ_ATTENDANCE where account = ? and to_char(create_time, 'yyyy-MM-dd') = TO_CHAR(SYSDATE, 'yyyy-MM-dd') )",u.getLoginname(),u.getLoginname(),u.getLoginname(),u.getLoginname());
        return list;
    }

    @Override
    public void save(Map<String, String> param) {
        String ss_id = param.get("ss_id");
        String avatar = param.get("avatar");
        String name = param.get("name");
        String native_place = param.get("native_place");
        String phone = param.get("phone");
        String rwny = param.get("rwny");
//        String status = param.get("ACC_STATUS") == null ? "0" : "1"; //这里挺有意思的，小朋友的代码这行是没有注释的，因为页面没有这个参数，导致用户只要修改过自己的数据，就会把自己禁用掉，>_< 头大
        UserEntity u = CustomRealm.GetLoginUser();
        // 修改
            DBHelper.execute("UPDATE TB_USER SET AVATAR=?,NATIVE_PLACE=?,PHONE=?,RWNY=?,UPDATETIME=SYSDATE WHERE ss_id=?",
                    avatar,native_place,phone,rwny,ss_id);
    }

    /**
     * 判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致
     *
     * @param dqsj 当前时间
     * @param kssj 开始时间
     * @param jssj 结束时间
     * @return
     */
    public static boolean isEffectiveDate(String dqsj, String kssj, String jssj) {
        String format = "HH:mm";
        Date nowTime = null;
        Date startTime = null;
        Date endTime = null;
        try {
            nowTime = new SimpleDateFormat(format).parse(dqsj);
            startTime = new SimpleDateFormat(format).parse(kssj);
            endTime = new SimpleDateFormat(format).parse(jssj);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }

    private static final double EARTH_RADIUS = 6371393;

    /**
     * 通过AB点经纬度获取距离
     * @param pointA A点(经，纬)
     * @param pointB B点(经，纬)
     * @return 距离(单位：米)
     */
    private static double getDistance(Point2D pointA, Point2D pointB) {
        // 经纬度（角度）转弧度。弧度用作参数，以调用Math.cos和Math.sin
        double radiansAX = Math.toRadians(pointA.getX()); // A经弧度
        double radiansAY = Math.toRadians(pointA.getY()); // A纬弧度
        double radiansBX = Math.toRadians(pointB.getX()); // B经弧度
        double radiansBY = Math.toRadians(pointB.getY()); // B纬弧度

        // 公式中“cosβ1cosβ2cos（α1-α2）+sinβ1sinβ2”的部分，得到∠AOB的cos值
        double cos = Math.cos(radiansAY) * Math.cos(radiansBY) * Math.cos(radiansAX - radiansBX)
                + Math.sin(radiansAY) * Math.sin(radiansBY);
//        System.out.println("cos = " + cos); // 值域[-1,1]
        double acos = Math.acos(cos); // 反余弦值
//        System.out.println("acos = " + acos); // 值域[0,π]
//        System.out.println("∠AOB = " + Math.toDegrees(acos)); // 球心角 值域[0,180]
        return EARTH_RADIUS * acos; // 最终结果
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
    public String spotDq(Map<String, String> param, UserEntity u, HttpServletRequest request) {
        //        String id= param.get("id");
        String jd=param.get("jd");//经度
        String wd=param.get("wd");//纬度
        String dz=param.get("dz");//纬度
        String account=u.getLoginname();//账号

        String username=u.getUsername();//用户id
        String rq =param.get("rq");//日期年月日
        String sj =param.get("sj");//时分秒

        String imel=param.get("deviceId");//设备编码
        String mobile=rq+" "+sj;//手机时间

        Map<String, Object> usermap = getUserInfo(u.getUser_id());
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String str = sf.format(date);//当前时间时分




        String id = DBHelper.queryForScalar("select SEQ_TB_DQ.nextval from dual ",String.class);

                    DBHelper.execute("insert  into TB_KQ_ATTENDANCE(ID,ACCOUNT,SBTIME,USERNAME,CREATE_TIME,UPDATE_TIME,JD,WD,MOBLIETIME,DZ,IMEL,CHECKTYPE)VALUES(?,?,SYSDATE,?,SYSDATE,SYSDATE,?,?,to_date(?,'yyyy-mm-dd hh24:mi:ss'),?,?,3)",
                            id,account,username,jd,wd,mobile,dz,imel);

        return "抽查打卡成功";
    }

}
