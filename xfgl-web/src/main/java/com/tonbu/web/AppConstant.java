package com.tonbu.web;

/**
 * 业务常量
 *
 * @author jinlei
 *
 */
public class AppConstant {

    //Status字段的转义，全部统一
    public static final String STATUS_ENABLED = "1";//可用
    public static final String STATUS_DISABLED = "0";//已禁用(未发布)
    public static final String STATUS_DEL = "-1";//删除



    //业务Status字段的转义，全部统一
    public static final String PROCESS_STATUS_ENABLE = "1";//可用
    public static final String PROCESS_STATUS_DOSOMETHING = "2";//流程挂起，执行子流程
    public static final String PROCESS_STATUS_DISABLE = "0";//已禁用
    public static final String PROCESS_STATUS_DEL = "-1";//删除
    public static final String PROCESS_STATUS_FINISH = "3";//已结束



    //ACE_TREE
    public static final String NODE_FOLDER="folder";
    public static final String NODE_ITEM="item";


}
