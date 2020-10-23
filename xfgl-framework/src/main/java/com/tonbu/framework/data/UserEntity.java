package com.tonbu.framework.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class UserEntity implements Serializable {

    //账户编号（accountId）
    private String id;
    //微信号
    private String wx_no;
    //微信企业账户
    private String wx_account;
    //用户编号（User_id）
    private String user_id;
    private String loginname;
    //账户密码
    private String password;
    //加密密码的盐
    private String salt;
    //账户状态
    private String status;
    //用户姓名
    private String username;
    // 部门
    private String deptname;
    // 部门编号
    private String dept_id;

    //办公电话
    private String officetel;
    //手机号码
    private String mobile;
    //电子邮件
    private String email;

    private String dwmc;
    //职务
    private String job;

    private String jobname;





    //角色列表
    private List<Map<String, Object>> roles;
    //权限列表
    private List<Map<String, Object>> menus;



}
