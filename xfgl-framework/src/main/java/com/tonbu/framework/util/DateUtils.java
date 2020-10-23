package com.tonbu.framework.util;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    protected static Logger logger=LogManager.getLogger(DateUtils.class.getName());

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public DateUtils() {
    }

    public static Date format(String date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);

        try {
            return df.parse(date);
        } catch (ParseException var4) {
            var4.printStackTrace();
            return null;
        }
    }

    public static String format(Date date, String format) {
        DateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

    public static int compareTo(String date1, String date2) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        try {
            c1.setTime(df.parse(date1));
            c2.setTime(df.parse(date2));
        } catch (ParseException var6) {
            System.err.println("格式不正确");
        }

        return c1.compareTo(c2);
    }

    public static int compareTo(Date date1, Date date2) {
        return date1.compareTo(date2);
    }

    public static String getNextDay(String date) {
        return getNextDay(date, "yyyy-MM-dd");
    }

    public static String getNextDay(String date, String format) {
        Calendar calendar = toCalendar(date, format);
        calendar.add(5, 1);
        return format(calendar.getTime(), format);
    }

    public static String getPriorDay(String date) {
        return getPriorDay(date, "yyyy-MM-dd");
    }

    public static String getPriorDay(String date, String format) {
        Calendar calendar = toCalendar(date, format);
        calendar.add(5, -1);
        return format(calendar.getTime(), format);
    }

    protected static Calendar toCalendar(String date, String format) {
        DateFormat df = new SimpleDateFormat(format);
        Calendar c1 = Calendar.getInstance();

        try {
            c1.setTime(df.parse(date));
        } catch (ParseException var5) {
            var5.printStackTrace();
        }

        return c1;
    }

    public static String formatLongToTimeStr(Long l) {
        int hour = 0;
        int minute = 0;
        int second = l.intValue() / 1000;
        if (second > 60) {
            minute = second / 60;
            second %= 60;
        }

        if (minute > 60) {
            hour = minute / 60;
            minute %= 60;
        }

        return hour + ":" + minute + ":" + second;
    }

    public static void main(String[] args) {


        String d=format(new Date(),"yyyy-MM-dd");
        String d2=format(new Date(),"yyyy-MM-dd HH:mm:ss");

        logger.error("123");

    }
}
