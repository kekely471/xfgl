package com.tonbu.web.admin.common;

public class StrUtils {
    public static String trimToEmpty(final Object str) {
        return str == null ? "" : (str + "").trim();
    }

    public static int trimToNum(final Object str) {
        return ((str == null) || str.equals("")) ? 0 : Integer.parseInt((str + "").trim());
    }

    public static String[] insert(String[] arr, String str) {
        int size = arr.length;  //获取数组长度
        String[] tmp = new String[size + 1];  //新建临时字符串数组，在原来基础上长度加一
        for (int i = 0; i < size; i++){  //先遍历将原来的字符串数组数据添加到临时字符串数组
            tmp[i] = arr[i];
        }
        tmp[size] = str;  //在最后添加上需要追加的数据
        return tmp;  //返回拼接完成的字符串数组
    }


}
