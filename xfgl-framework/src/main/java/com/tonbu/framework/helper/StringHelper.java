package com.tonbu.framework.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {
    /**
     * 首字母变小写
     */
    public static String firstCharToLowerCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            char[] arr = str.toCharArray();
            arr[0] += ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    /**
     * 首字母变大写
     */
    public static String firstCharToUpperCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'a' && firstChar <= 'z') {
            char[] arr = str.toCharArray();
            arr[0] -= ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    /**
     * 字符串为 null 或者内部字符全部为 ' ' '\t' '\n' '\r' 这四类字符时返回 true
     */
    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        int len = str.length();
        if (len == 0) {
            return true;
        }
        for (int i = 0; i < len; i++) {
            switch (str.charAt(i)) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    // case '\b':
                    // case '\f':
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public static boolean notBlank(String str) {
        return !isBlank(str);
    }

    public static boolean notBlank(String... strings) {
        if (strings == null) {
            return false;
        }
        for (String str : strings) {
            if (isBlank(str)) {
                return false;
            }
        }
        return true;
    }

    public static boolean notNull(Object... paras) {
        if (paras == null) {
            return false;
        }
        for (Object obj : paras) {
            if (obj == null) {
                return false;
            }
            if(obj instanceof String && isBlank(obj.toString())){
                return false;
            }
        }
        return true;
    }

    public static String toCamelCase(String stringWithUnderline) {
        if (stringWithUnderline.indexOf('_') == -1) {
            return stringWithUnderline;
        }

        stringWithUnderline = stringWithUnderline.toLowerCase();
        char[] fromArray = stringWithUnderline.toCharArray();
        char[] toArray = new char[fromArray.length];
        int j = 0;
        for (int i=0; i<fromArray.length; i++) {
            if (fromArray[i] == '_') {
                // 当前字符为下划线时，将指针后移一位，将紧随下划线后面一个字符转成大写并存放
                i++;
                if (i < fromArray.length) {
                    toArray[j++] = Character.toUpperCase(fromArray[i]);
                }
            }
            else {
                toArray[j++] = fromArray[i];
            }
        }
        return new String(toArray, 0, j);
    }

    public static String join(String[] stringArray) {
        StringBuilder sb = new StringBuilder();
        for (String s : stringArray) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static String join(String[] stringArray, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<stringArray.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(stringArray[i]);
        }
        return sb.toString();
    }
/*
    public static boolean slowEquals(String a, String b) {
        byte[] aBytes = (a != null ? a.getBytes() : null);
        byte[] bBytes = (b != null ? b.getBytes() : null);
        return HashHelper.slowEquals(aBytes, bBytes);
    }*/
   /**
     * 左边补零；param转化成字符串，位数不够左边用零补足
     * @param param 被补零参数
     * @param reference 返回的字符串长度
     * */
    public static String firstZeroFill(int param,int reference){
        if((param/reference)>0){
            return param+"";
        }else{
            return "0"+param;
        }
    }

    public static String getRandomUUID() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    public static String[] toArray(String str,String regex) {
       if(isBlank(str)){
           return new String[]{};
       }
       return str.split(regex);
    }

    /**
     *身份证验证
     * @param idStr
     * @return
     */
    public static String identityCardVerification(String idStr){
        String[] wf = { "1", "0", "x", "9", "8", "7", "6", "5", "4", "3", "2" };
        String[] checkCode = { "7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7", "9", "10", "5", "8", "4", "2" };
        String iDCardNo = "";
        try {
            //判断号码的长度 15位或18位
            if (idStr.length() != 15 && idStr.length() != 18) {
                return "身份证号码长度应该为15位或18位";
            }
            if (idStr.length() == 18) {
                iDCardNo = idStr.substring(0, 17);
            } else if (idStr.length() == 15) {
                iDCardNo = idStr.substring(0, 6) + "19" + idStr.substring(6, 15);
            }
            if (isStrNum(iDCardNo) == false) {
                return "身份证15位号码都应为数字;18位号码除最后一位外,都应为数字";
            }
            //判断出生年月
            String strYear = iDCardNo.substring(6, 10);// 年份
            String strMonth = iDCardNo.substring(10, 12);// 月份
            String strDay = iDCardNo.substring(12, 14);// 月份
            if (isStrDate(strYear + "-" + strMonth + "-" + strDay) == false) {
                return "身份证生日无效";
            }
            GregorianCalendar gc = new GregorianCalendar();
            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
            if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150 || (gc.getTime().getTime() - s.parse(strYear + "-" + strMonth + "-" + strDay).getTime()) < 0) {
                return "身份证生日不在有效范围";
            }
            if (Integer.parseInt(strMonth) > 12 || Integer.parseInt(strMonth) == 0) {
                return "身份证月份无效";
            }
            if (Integer.parseInt(strDay) > 31 || Integer.parseInt(strDay) == 0) {
                return "身份证日期无效";
            }
            //判断地区码
            Hashtable h = GetAreaCode();
            if (h.get(iDCardNo.substring(0, 2)) == null) {
                return "身份证地区编码错误";
            }
            //判断最后一位
            int theLastOne = 0;
            for (int i = 0; i < 17; i++) {
                theLastOne = theLastOne + Integer.parseInt(String.valueOf(iDCardNo.charAt(i))) * Integer.parseInt(checkCode[i]);
            }
            int modValue = theLastOne % 11;
            String strVerifyCode = wf[modValue];
            iDCardNo = iDCardNo + strVerifyCode;
            if (idStr.length() == 18 &&!iDCardNo.equals(idStr)) {
                return "身份证无效，不是合法的身份证号码";
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 地区代码
     * @return Hashtable
     */
    private static Hashtable GetAreaCode() {
        Hashtable hashtable = new Hashtable();
        hashtable.put("11", "北京");
        hashtable.put("12", "天津");
        hashtable.put("13", "河北");
        hashtable.put("14", "山西");
        hashtable.put("15", "内蒙古");
        hashtable.put("21", "辽宁");
        hashtable.put("22", "吉林");
        hashtable.put("23", "黑龙江");
        hashtable.put("31", "上海");
        hashtable.put("32", "江苏");
        hashtable.put("33", "浙江");
        hashtable.put("34", "安徽");
        hashtable.put("35", "福建");
        hashtable.put("36", "江西");
        hashtable.put("37", "山东");
        hashtable.put("41", "河南");
        hashtable.put("42", "湖北");
        hashtable.put("43", "湖南");
        hashtable.put("44", "广东");
        hashtable.put("45", "广西");
        hashtable.put("46", "海南");
        hashtable.put("50", "重庆");
        hashtable.put("51", "四川");
        hashtable.put("52", "贵州");
        hashtable.put("53", "云南");
        hashtable.put("54", "西藏");
        hashtable.put("61", "陕西");
        hashtable.put("62", "甘肃");
        hashtable.put("63", "青海");
        hashtable.put("64", "宁夏");
        hashtable.put("65", "新疆");
        hashtable.put("71", "台湾");
        hashtable.put("81", "香港");
        hashtable.put("82", "澳门");
        hashtable.put("91", "国外");
        return hashtable;
    }
    /**
     * 判断字符串是否为数字
     * @param str
     * @return
     */
    private static boolean isStrNum(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (isNum.matches()) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 判断字符串是否为日期格式
     * @param strDate
     * @return
     */
    public static boolean isStrDate(String strDate) {
        Pattern pattern = Pattern.compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");
        Matcher m = pattern.matcher(strDate);
        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }


}
