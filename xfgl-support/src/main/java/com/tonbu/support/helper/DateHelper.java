package com.tonbu.support.helper;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateHelper {
    public final static String yyyyMMddHHmmss = "yyyyMMddHHmmss";
    public final static String yyyyMMdd = "yyyyMMdd";
    public final static String yyyyMMddHHmmssSSS = "yyyyMMddHHmmssSSS";

    public final static String YYYY = "yyyy";
    public final static String MM = "MM";
    public final static String DD = "dd";
    public final static String YYYY_MM_DD = "yyyy-MM-dd";
    public final static String YYYY_MM = "yyyy-MM";
    public final static String HH_MM_SS = "HH:mm:ss";
    public final static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public final static String formatStr_yyyyMMddHHmmssS = "yyyy-MM-dd HH:mm:ss.S";
    public final static String formatStr_yyyyMMddHHmmss = "yyyy-MM-dd HH:mm:ss";
    public final static String formatStr_yyyyMMddHHmmss1 = "yyyyMMddHHmmss";
    public final static String formatStr_yyyyMMddHHmm = "yyyy-MM-dd HH:mm";
    public final static String formatStr_yyyyMMddHH = "yyyy-MM-dd HH";
    public final static String formatStr_yyyyMMdd = "yyyy-MM-dd";
    public final static String formatStr_yyyymmddhhmmss_oracle="";
    public final static String[] formatStr = {formatStr_yyyyMMddHHmmss, formatStr_yyyyMMddHHmm, formatStr_yyyyMMddHH, formatStr_yyyyMMdd};





    /** 锁对象 */
    private static final Object lockObj = new Object();
    /** 存放不同的日期模板格式的SimpleDateFormat的Map */
    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();

    /**
     * 返回一个ThreadLocal的SimpleDateFormat,每个线程只会new一次SimpleDateFormat
     * @param pattern
     * @return
     */
    private static SimpleDateFormat getSdf(final String pattern) {
        ThreadLocal<SimpleDateFormat> tl = sdfMap.get(pattern);
        // 此处的双重判断和同步是为了防止sdfMap这个单例被多次put重复的SimpleDateFormat
        if (tl == null) {
            synchronized (lockObj) {
                tl = sdfMap.get(pattern);
                if (tl == null) {
                    // 这里是关键,使用ThreadLocal<SimpleDateFormat>替代原来直接new SimpleDateFormat
                    tl = new ThreadLocal<SimpleDateFormat>() {
                        @Override
                        protected SimpleDateFormat initialValue() {
                            return new SimpleDateFormat(pattern);
                        }
                    };
                    sdfMap.put(pattern, tl);
                }
            }
        }
        return tl.get();
    }




    /**
     * 日期格式化－将<code>Date</code>类型的日期格式化为<code>String</code>型
     *
     * @param date
     *            待格式化的日期
     * @param pattern
     *            时间样式
     * @return 一个被格式化了的<code>String</code>日期
     */
    public static String format(Date date, String pattern) {
        if (date == null)
            return "";
        else
            return getFormatter(pattern).format(date);
    }

    /**
     * 默认把日期格式化成yyyy-mm-dd格式
     *
     * @param date
     * @return
     */
    public static String format(Date date) {
        if (date == null)
            return "";
        else
            return getFormatter(YYYY_MM_DD).format(date);
    }

    /**
     * 把字符串日期默认转换为yyyy-mm-dd格式的Data对象
     *
     * @param strDate
     * @return
     */
    public static Date format(String strDate) {
        Date d = null;
        if (strDate == "")
            return null;
        else
            try {
                d = getFormatter(YYYY_MM_DD).parse(strDate);
            } catch (ParseException pex) {
                return null;
            }
        return d;
    }

    /**
     * 把字符串日期转换为f指定格式的Data对象
     *
     * @param strDate
     *            ,f
     * @return
     */
    public static Date format(String strDate, String f) {
        Date d = null;
        if (strDate == "")
            return null;
        else
            try {
                d = getFormatter(f).parse(strDate);
            } catch (ParseException pex) {
                return null;
            }
        return d;
    }

    /**
     * 日期解析－将<code>String</code>类型的日期解析为<code>Date</code>型
     *
     * @param strDate
     *            待格式化的日期
     * @param pattern
     *            日期样式
     * @exception ParseException
     *                如果所给的字符串不能被解析成一个日期
     * @return 一个被格式化了的<code>Date</code>日期
     */
    public static Date parse(String strDate, String pattern) throws ParseException {
        try {
            return getFormatter(pattern).parse(strDate);
        } catch (ParseException pe) {
            throw new ParseException("Method parse in Class DateUtil err: parse strDate fail.", pe.getErrorOffset());
        }
    }

    /**
     * 获取当前日期
     *
     * @return 一个包含年月日的<code>Date</code>型日期
     */
    public static synchronized Date getCurrDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }


    /**
     * 获取当前日期
     *
     * @return 一个包含年月日的<code>Date</code>型日期
     */
    public static  String getCurrDateFormat() {
        return format(new Date(), formatStr_yyyyMMddHHmmss);
    }


    /**
     * 获取当前日期
     *
     * @return 一个包含年月日的<code>Date</code>型日期
     */
    public static Timestamp getCurrDateForOracle() {
        return  new java.sql.Timestamp(new java.util.Date().getTime());
    }

    /**
     *
     *
     * @return 一个包含年月日的<code>Date</code>型日期
     */
    public static  Timestamp getDateForOracle(Date date) {
        return  new java.sql.Timestamp(date.getTime());
    }

    /**
     * 获取当前日期
     *
     * @return 一个包含年月日的<code>String</code>型日期，但不包含时分秒。yyyy-mm-dd
     */
    public static String getCurrDateStr() {
        return format(getCurrDate(), YYYY_MM_DD);
    }

    /**
     * 获取当前时间
     *
     * @return 一个包含年月日时分秒的<code>String</code>型日期。hh:mm:ss
     */
    public static String getCurrTimeStr() {
        return format(getCurrDate(), HH_MM_SS);
    }

    /**
     * 获取当前完整时间,样式: yyyy－MM－dd hh:mm:ss
     *
     * @return 一个包含年月日时分秒的<code>String</code>型日期。yyyy-MM-dd hh:mm:ss
     */
    public static String getCurrDateTimeStr() {
        return format(getCurrDate(), YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 获取当前年分 样式：yyyy
     *
     * @return 当前年分
     */
    public static String getYear() {
        return format(getCurrDate(), YYYY);
    }

    /**
     * 获取当前月分 样式：MM
     *
     * @return 当前月分
     */
    public static String getMonth() {
        return format(getCurrDate(), MM);
    }

    /**
     * 获取当前日期号 样式：dd
     *
     * @return 当前日期号
     */
    public static String getDay() {
        return format(getCurrDate(), DD);
    }

    /**
     * 按给定日期样式判断给定字符串是否为合法日期数据
     *
     * @param strDate
     *            要判断的日期
     * @param pattern
     *            日期样式
     * @return true 如果是，否则返回false
     */
    public static boolean isDate(String strDate, String pattern) {
        try {
            parse(strDate, pattern);
            return true;
        } catch (ParseException pe) {
            return false;
        }
    }

    /**
     * 判断给定字符串是否为特定格式日期（包括：年月日yyyy-MM-dd）数据
     *
     * @param strDate
     *            要判断的日期
     * @return true 如果是，否则返回false
     */
    // public static boolean isDate(String strDate) {
    // try {
    // parse(strDate, YYYY_MM_DD);
    // return true;
    // }
    // catch (ParseException pe) {
    // return false;
    // }
    // }

    /**
     * 判断给定字符串是否为特定格式年份（格式：yyyy）数据
     *
     * @param strDate
     *            要判断的日期
     * @return true 如果是，否则返回false
     */
    public static boolean isYYYY(String strDate) {
        try {
            parse(strDate, YYYY);
            return true;
        } catch (ParseException pe) {
            return false;
        }
    }

    public static boolean isYYYY_MM(String strDate) {
        try {
            parse(strDate, YYYY_MM);
            return true;
        } catch (ParseException pe) {
            return false;
        }
    }

    /**
     * 判断给定字符串是否为特定格式的年月日（格式：yyyy-MM-dd）数据
     *
     * @param strDate
     *            要判断的日期
     * @return true 如果是，否则返回false
     */
    public static boolean isYYYY_MM_DD(String strDate) {
        try {
            parse(strDate, YYYY_MM_DD);
            return true;
        } catch (ParseException pe) {
            return false;
        }
    }

    /**
     * 判断给定字符串是否为特定格式年月日时分秒（格式：yyyy-MM-dd HH:mm:ss）数据
     *
     * @param strDate
     *            要判断的日期
     * @return true 如果是，否则返回false
     */
    public static boolean isYYYY_MM_DD_HH_MM_SS(String strDate) {
        try {
            parse(strDate, YYYY_MM_DD_HH_MM_SS);
            return true;
        } catch (ParseException pe) {
            return false;
        }
    }

    /**
     * 判断给定字符串是否为特定格式时分秒（格式：HH:mm:ss）数据
     *
     * @param strDate
     *            要判断的日期
     * @return true 如果是，否则返回false
     */
    public static boolean isHH_MM_SS(String strDate) {
        try {
            parse(strDate, HH_MM_SS);
            return true;
        } catch (ParseException pe) {
            return false;
        }
    }

    /**
     * 判断给定字符串是否为特定格式时间（包括：时分秒hh:mm:ss）数据
     *
     * @param strTime
     *            要判断的时间
     * @return true 如果是，否则返回false
     */
    // public static boolean isTime(String strTime) {
    // try {
    // parse(strTime, HH_MM_SS);
    // return true;
    // }
    // catch (ParseException pe) {
    // return false;
    // }
    // }

    /**
     * 判断给定字符串是否为特定格式日期时间（包括：年月日时分秒 yyyy-MM-dd hh:mm:ss）数据
     *
     * @param strDateTime
     *            要判断的日期时间
     * @return true 如果是，否则返回false
     */
    // public static boolean isDateTime(String strDateTime) {
    // try {
    // parse(strDateTime, YYYY_MM_DD_HH_MM_SS);
    // return true;
    // }
    // catch (ParseException pe) {
    // return false;
    // }
    // }

    /**
     * 获取一个简单的日期格式化对象
     *
     * @return 一个简单的日期格式化对象
     */
    private static SimpleDateFormat getFormatter(String parttern) {
        return new SimpleDateFormat(parttern);
    }

    /**
     * 获取给定日前的后intevalDay天的日期
     *
     * @param refenceDate
     *            给定日期（格式为：yyyy-MM-dd）
     * @param intevalDays
     *            间隔天数
     * @return 计算后的日期
     */
    public static String getNextDate(String refenceDate, int intevalDays) {
        try {
            return getNextDate(parse(refenceDate, YYYY_MM_DD), intevalDays);
        } catch (Exception ee) {
            return "";
        }
    }

    /**
     * 获取给定日前的后intevalDay天的日期
     *
     * @param refenceDate
     *            Date 给定日期
     * @param intevalDays
     *            int 间隔天数
     * @return String 计算后的日期
     */
    public static String getNextDate(Date refenceDate, int intevalDays) {
        try {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(refenceDate);
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + intevalDays);
            return format(calendar.getTime(), YYYY_MM_DD);
        } catch (Exception ee) {
            return "";
        }
    }

    public static long getIntevalDays(String startDate, String endDate) {
        try {
            return getIntevalDays(parse(startDate, YYYY_MM_DD), parse(endDate, YYYY_MM_DD));
        } catch (Exception ee) {
            return 0l;
        }
    }

    public static long getIntevalDays(Date startDate, Date endDate) {
        try {
            java.util.Calendar startCalendar = java.util.Calendar.getInstance();
            java.util.Calendar endCalendar = java.util.Calendar.getInstance();

            startCalendar.setTime(startDate);
            endCalendar.setTime(endDate);
            long diff = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();

            return (diff / (1000 * 60 * 60 * 24));
        } catch (Exception ee) {
            return 0l;
        }
    }

    /**
     * 求当前日期和指定字符串日期的相差天数
     *
     * @param startDate
     * @return
     */
    public static long getTodayIntevalDays(String startDate) {
        try {
            // 当前时间
            Date currentDate = new Date();

            // 指定日期
            SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date theDate = myFormatter.parse(startDate);

            // 两个时间之间的天数
            long days = (currentDate.getTime() - theDate.getTime()) / (24 * 60 * 60 * 1000);

            return days;
        } catch (Exception ee) {
            return 0l;
        }
    }

    public static Date parseToDate(String dateTimeStr) {
        if (dateTimeStr == null)
            return null;
        Date d = null;
        int formatStrLength = formatStr.length;
        for (int i = 0; i < formatStrLength; i++) {
            d = parseToDate2(dateTimeStr, formatStr[i]);
            if (d != null) {
                break;
            }
        }
        return d;
    }

    private static Date parseToDate2(String dateTimeStr, String formatString) {
        Date d = null;
        SimpleDateFormat sdf = new SimpleDateFormat(formatString);
        try {
            d = sdf.parse(dateTimeStr);
        } catch (ParseException pe) {

        }
        return d;
    }

    public static String dateTimeToString(Date datetime) {
        // dateTime=dateTime.substring(0,4)+dateTime.substring(5,7)+dateTime.substring(8,10)+dateTime.substring(11,13)+dateTime.substring(14,16)+dat
        // return dateTime;

        java.util.GregorianCalendar calendar = new java.util.GregorianCalendar();
        calendar.setTime(datetime);
        String dateTime = calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1 > 9 ? "" : "0") + (calendar.get(Calendar.MONTH) + 1) + ""
                + (calendar.get(Calendar.DATE) > 9 ? "" : "0") + calendar.get(Calendar.DATE) + "" + (calendar.get(Calendar.HOUR_OF_DAY) > 9 ? "" : "0")
                + calendar.get(Calendar.HOUR_OF_DAY) + "" + (calendar.get(Calendar.MINUTE) > 9 ? "" : "0") + calendar.get(Calendar.MINUTE) + ""
                + (calendar.get(Calendar.SECOND) > 9 ? "" : "0") + calendar.get(Calendar.SECOND);
        return dateTime;
    }

    /**
     * 由年、月份，获得当前月的最后一天
     *
     * @param year
     *            month 月份 01 02 11 12
     * @return
     * @throws ParseException
     */
    public static String getLastDayOfMonth(String year, String month) throws ParseException {
        String LastDay = "";
        Calendar cal = Calendar.getInstance();
        Date date_;
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(year + "-" + month + "-14");
        cal.setTime(date);
        int value = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, value);
        date_ = cal.getTime();
        LastDay = new SimpleDateFormat("yyyy-MM-dd").format(date_);
        return LastDay;
    }

    public static String getTimeForOracle(String format){
        return "to_date('"+format(new Date(), format)+"','yyyy-mm-dd hh24:mi:ss')";
    }

    public static String getTimeForOracle(){
        return "to_date('"+format(new Date(), formatStr_yyyyMMddHHmmss)+"','yyyy-mm-dd hh24:mi:ss')";
    }

    /**
     * 把字符串日期转换为Timestamp
     * @param strDate
     * @return
     */
    public static Timestamp formatForOracle(String strDate) {
        return new java.sql.Timestamp(format(strDate)
                .getTime());
    }


    public static boolean isValidDate(Object str) {
        boolean convertSuccess = true;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            format.setLenient(false);
            format.parse(str.toString());
        } catch (Exception e) {
            convertSuccess = false;
        }
        return convertSuccess;
    }

    public static boolean isValidDate(Object str,String formatStr) {
        boolean convertSuccess = true;
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        try {
            format.setLenient(false);
            format.parse(str.toString());
        } catch (Exception e) {
            convertSuccess = false;
        }
        return convertSuccess;
    }

    /**
     * 获取当前日期(兼容mysql与oracle日期类型)
     *
     * @return 一个包含年月日的<code>Date</code>型日期
     */
    public static  Date getDateForAll() {
        return  new java.sql.Timestamp(new java.util.Date().getTime());
    }

    /**
     * 获取当前日期(兼容mysql与oracle日期类型)加time毫秒
     * @param time 增加或减少的毫秒数
     * @return 一个包含年月日的<code>Date</code>型日期
     */
    public static  Date getDateForAll(Long time) {
        return  new java.sql.Timestamp(new java.util.Date().getTime()+time);
    }

    /**
     * debug
     *//*
    public static void main(String[] args) {
        try {
            System.out.println("当前日期：" + DateUtil.getCurrDateStr());
            System.out.println("日期格式化：" + DateUtil.format(new Date(), DateUtil.formatStr_yyyyMMddHHmmss1));
            System.out.println("短日期：" + DateUtil.format(new Date()));
            System.out.println("长日期：" + DateUtil.getCurrDateTimeStr());
            System.out.println("日：" + DateUtil.getDay());
            System.out.println("月：" + DateUtil.getMonth());
            System.out.println("年：" + DateUtil.getYear());
            System.out.println("月未最后一天：" + DateUtil.getLastDayOfMonth("2010", "08"));
            System.out.println("相差几天：" + DateUtil.getIntevalDays("2010-08-01", "2010-08-21"));

            System.out.println("当前日期后的几天：" + DateUtil.getNextDate("2010-08-01", -3));
            System.out.println("与今天相差几天：" + DateUtil.getTodayIntevalDays("2010-08-01"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
    /**
     * oracle类型的自定义format
     * @author zhi.zhang, 2015-12-22
     * @param str
     * @param format
     * @return
     */
    public static Timestamp formatForOracle(String str, String format){
        try {
            return new java.sql.Timestamp(format(str,format).getTime());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 加减月
     * @param date
     * @param minute
     *
     * */
    public static Date addMinute(Date date,int minute){
        Calendar nowTime = Calendar.getInstance();
        nowTime.setTime(date);
        nowTime.add(Calendar.MINUTE, minute);
        return nowTime.getTime();
    }

    /**
     * 加减分钟
     * @param date
     * @param day
     *
     * */
    public static Date addDay(Date date,int day){
        Calendar nowTime = Calendar.getInstance();
        nowTime.setTime(date);
        nowTime.add(Calendar.DAY_OF_MONTH, day);
        return nowTime.getTime();
    }
}