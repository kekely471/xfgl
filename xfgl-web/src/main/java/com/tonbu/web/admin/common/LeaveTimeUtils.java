package com.tonbu.web.admin.common;

import com.tonbu.framework.dao.DBHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @program: xfgl
 * @author: keke
 * @create: 2020-07-13 10:02
 **/

public class LeaveTimeUtils {


    public static String trimToEmpty(final Object str) {
        return str == null ? "" : (str + "").trim();
    }

    public static int trimToNum(final Object str) {
        return ((str == null) || str.equals("")) ? 0 : Integer.parseInt((str + "").trim());
    }



    //天数计算（开始时间-结束时间）
    public static int getDayDiffer(Date startDate, Date endDate) throws ParseException {
        int days = 0;
        //判断是否跨年
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat hourFormat = new SimpleDateFormat("HHmm");
        String startYear = yearFormat.format(startDate);
        String endYear = yearFormat.format(endDate);
        String endhour = hourFormat.format(endDate);
        if (startYear.equals(endYear)) {
            /*  使用Calendar跨年的情况会出现问题    */
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            int startDay = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTime(endDate);
            int endDay = calendar.get(Calendar.DAY_OF_YEAR);
            if (trimToEmpty(startDay).equals(trimToEmpty(endDay))) {
                //变更，计算同一天情况下，结束时间是否超过18点，超过1，不超0，不是同一天1
                days = 1;
            } else {
                days = endDay - startDay + 1;
            }
        } else {
            /*  跨年不会出现问题，需要注意不满24小时情况（2016-03-18 11:59:59 和 2016-03-19 00:00:01的话差值为 0）  */
            //  只格式化日期，消除不满24小时影响
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            long startDateTime = dateFormat.parse(dateFormat.format(startDate)).getTime();
            long endDateTime = dateFormat.parse(dateFormat.format(endDate)).getTime();
            days = (int) ((endDateTime - startDateTime) / (1000 * 3600 * 24));
        }

        return days;
    }


    //计算同一天情况下，结束时间是否超过18点，超过1，不超0，不是同一天1
    public static int getDayDifferForActiviti(Date startDate, Date endDate) throws ParseException {
        int days = 1;
        //判断是否跨年
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat hourFormat = new SimpleDateFormat("HHmm");
        String startYear = yearFormat.format(startDate);
        String endYear = yearFormat.format(endDate);
        String endhour = hourFormat.format(endDate);
        //战备状态
        String zbzt =  DBHelper.queryForScalar("SELECT STATUS FROM TB_ZBZT WHERE ID=1",String.class);
        ;//3正常 2二级战备 1一级战备
        if (zbzt.equals("2")) {//战备状态为2时，请假均走复杂流程
            return 1;
        }
        if (startYear.equals(endYear)) {
            /*  使用Calendar跨年的情况会出现问题    */
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            int startDay = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTime(endDate);
            int endDay = calendar.get(Calendar.DAY_OF_YEAR);
            if (trimToEmpty(startDay).equals(trimToEmpty(endDay))) {
                if (trimToNum(endhour) > 1800) {
                    days = 1;
                } else {
                    days = 0;
                }
            } else {
                days = 1;
            }
        } else {
            days = 1;
        }

        return days;
    }


    //计算当年中，排除掉节假日的请假天数
    public static int getDaysByHoliday(Date startDate, Date endDate, int allDays) throws ParseException {


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        Date startDate1 = dateFormat.parse(dateFormat.format(startDate));
        Date endDate1 = dateFormat.parse(dateFormat.format(endDate));

        Calendar tempStart = Calendar.getInstance();

        tempStart.setTime(startDate1);

        Calendar tempEnd = Calendar.getInstance();
        tempEnd.setTime(endDate1);
        while (tempStart.before(tempEnd) || tempStart.equals(tempEnd)) {
            String count = DBHelper.queryForScalar("select count(1) from business_dict t where t.type_code='jjr' and value=? and status=1 ", String.class, dateFormat.format(tempStart.getTime()));
            if ("1".equals(count)) {
                allDays--;
            }
            tempStart.add(Calendar.DAY_OF_YEAR, 1);

        }


        return allDays;
    }
}
