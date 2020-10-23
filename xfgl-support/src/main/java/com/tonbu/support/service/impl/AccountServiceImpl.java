package com.tonbu.support.service.impl;

import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.support.service.AccountService;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.support.util.CryptUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("accountService")
public class AccountServiceImpl implements AccountService {


    Logger logger = LogManager.getLogger(AccountServiceImpl.class.getName());

    public AccountServiceImpl() {
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param loginname
     */
    @Override
    public UserEntity findByloginname(String loginname) {
        UserEntity u = DBHelper.queryForBean("SELECT A.LOGINNAME,A.PASSWORD,A.SALT,A.STATUS,A.WX_NO,A.WX_ACCOUNT," +
                "SU.*,A.ID,SU.ID USER_ID,SU.USERNAME,SD.DEPTNAME,SD.UNITNO,SU.OFFICETEL,SU.MOBILE,SU.EMAIL,SD.DEPTNO,t.DWMC,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)JOBNAME,t.JOB " +
                "FROM ACCOUNT A " +
                "LEFT JOIN ACCOUNT_USER AU ON AU.ACCOUNT_ID=A.ID " +
                "LEFT JOIN SS_USER SU ON SU.ID=AU.USER_ID " +
                "LEFT JOIN SS_DEPT SD ON SD.ID=SU.DEPT_ID " +
                "left join tb_user t on t.ss_id=SU.ID  WHERE A.LOGINNAME=? AND A.STATUS=? AND  t.acc_status=1 ", UserEntity.class, loginname, Constants.ACCOUNT_ENABLED);
        if (u == null) {
            return null;
        }
		//获取角色role
        List<Map<String,Object>> l_r = DBHelper.queryForList("SELECT SR.ID,SR.ROLENAME,SR.ROLE_TYPE,SR.REMARK,SR.STATUS,SR.JSBS " +
                "FROM SS_ROLE SR " +
                "LEFT JOIN ACCOUNT_ROLE AR ON AR.ROLE_ID=SR.ID " +
                "WHERE AR.ACCOUNT_ID=? AND SR.STATUS=?",  u.getId(), Constants.ACCOUNT_ENABLED);
        u.setRoles(l_r);
        String sql_menu = "SELECT DISTINCT SM.ID,SM.MENUNAME,SM.MENUTYPE,SM.PARENTID,PSM.MENUNAME PMENUNAME,SM.SYMBOL,SM.ICON,SM.URL,SM.SORT,SM.STATUS " +
                "FROM SS_MENU SM " +
                "LEFT JOIN SS_ROLE_MENU SRM ON SRM.MENU_ID=SM.ID " +
                "LEFT JOIN SS_ROLE SR ON SR.ID=SRM.ROLE_ID " +
                "LEFT JOIN ACCOUNT_ROLE AR ON AR.ROLE_ID=SR.ID " +
                "LEFT JOIN SS_MENU PSM ON PSM.ID=SM.PARENTID ";
        if(!StringUtils.equals("root", loginname)) {
            sql_menu = sql_menu + "WHERE SM.ID<>'00' AND AR.ACCOUNT_ID =" + u.getId() + " AND SM.STATUS=" + "1"+" AND SM.ID NOT LIKE '06%'";
        } else {
            sql_menu = sql_menu + "WHERE SM.ID<>'00' ";
        }

        sql_menu +=" ORDER BY SM.SORT ASC ";
        List<Map<String, Object>> l_m = DBHelper.queryForList(sql_menu);

        u.setMenus(l_m);
        return u;
    }

    @Override
    public  UserEntity findById(String userId) {
        String sql = "SELECT A.LOGINNAME,A.STATUS,A.WX_NO,A.WX_ACCOUNT,A.PASSWORD,A.SALT," +
                "   SU.DEPT_ID,SU.STATUS,SU.SORT ,SU.REMARK ,SU.CREATE_ID,SU.CREATE_TIME,SU.UPDATE_ID,SU.UPDATE_TIME,SU.SEX ," +
                "  SU.POLICE_NO,SU.USER_TYPE,A.ID,SU.ID USER_ID,SU.USERNAME,SD.DEPTNAME,SU.OFFICETEL,SU.MOBILE,SU.EMAIL " +
                "  FROM ACCOUNT A " +
                " INNER JOIN ACCOUNT_USER AU ON AU.ACCOUNT_ID=A.ID " +
                " INNER JOIN SS_USER SU ON SU.ID=AU.USER_ID " +
                " INNER JOIN SS_DEPT SD ON SD.ID=SU.DEPT_ID" +
                "   WHERE su.id = ?  AND A.STATUS=?  ";
        UserEntity u = DBHelper.queryForBean(sql, UserEntity.class, userId, Constants.ACCOUNT_ENABLED);
        return u;
    }


    /**
     * 列表
     */
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT V.ID,V.LOGINNAME,V.WX_NO,V.WX_ACCOUNT,V.STATUS,U.USERNAME UPDATER, TO_CHAR(V.UPDATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') UPDATETIME , ");
        sql.append("TO_CHAR(V.LAST_LOGIN_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') LASTLOGINTIME ,");
        sql.append("(SELECT WM_CONCAT(TO_CHAR(R.ROLENAME)) FROM  ACCOUNT_ROLE AR LEFT JOIN SS_ROLE R ON R.ID=AR.ROLE_ID WHERE AR.ACCOUNT_ID=V.ID) AS ROLENAME, ");
        sql.append("(SELECT WM_CONCAT(TO_CHAR(UU.USERNAME)) FROM  ACCOUNT_USER AU LEFT JOIN SS_USER UU ON UU.ID=AU.USER_ID WHERE AU.ACCOUNT_ID=V.ID) AS RELUSER ");
        sql.append("FROM ACCOUNT V ");
        sql.append("LEFT JOIN SS_USER U ON U.ID=V.UPDATE_ID ");
        sql.append("WHERE 1=1 AND V.STATUS <> " + Constants.ACCOUNT_DEL);
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND (V.WX_ACCOUNT LIKE ? OR V.LOGINNAME LIKE ? ) ");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
        }
        //拼接排序
        String sord = param.get("sord");
        String sidx = param.get("sidx");
        if (StringUtils.isNotBlank(sidx)) {
            sql.insert(0, "SELECT * FROM (");
            sql.append(") T ORDER BY T." + sidx + " " + sord);
        } else {
            sql.append(" ORDER BY V.LAST_LOGIN_TIME DESC");
        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }

    /**
     * 修改/保存
     */
    @Transactional(value = "primaryTransactionManager")
    public void save(Map<String, String> param) {
        String id = param.get("id");
        String loginname = param.get("loginname");
        String password = param.get("password");
        String password2 = param.get("password2");
        String wx_account = param.get("wx_account");
        String remark = param.get("remark");
        String role = param.get("role");
        String status = param.get("status") == null ? "0" : "1";
        if (StringUtils.isBlank(status)) {
            status = "1";
        }
        String user_id = param.get("user_id");
        String salt = CryptUtils.getRandomSalt(32);
        String pwd = CryptUtils.md5(password,salt);
        UserEntity u = CustomRealm.GetLoginUser();
        if (StringUtils.isBlank(id)) {// 新增
            // loginname 唯一性验证
            Map<String, Object> m = DBHelper.queryForMap("SELECT 1 FROM ACCOUNT V WHERE V.LOGINNAME=? ", loginname);
            if (m != null) {
                throw new RuntimeException("该登录名称已经存在，请重新输入！");
            }
            if (!StringUtils.equals(password2, password)) {
                throw new RuntimeException("输入的两次密码不相同，请重新输入！");
            }
            DBHelper.execute("INSERT INTO ACCOUNT(ID,LOGINNAME,WX_ACCOUNT,PASSWORD,SALT,STATUS,REMARK,CREATE_ID,CREATE_TIME,UPDATE_ID,UPDATE_TIME) " +
                            "VALUES(SEQ_ACCOUNT.NEXTVAL,?,?,?,?,?,?,?,SYSDATE,?,SYSDATE)",
                    loginname, wx_account, pwd, salt, status, remark, u.getUser_id(), u.getUser_id());
            id = DBHelper.queryForScalar("SELECT SEQ_ACCOUNT.CURRVAL FROM DUAL", Integer.class).toString();

            //如果还传入了user_id 则需要在绑定一下用户
            if (StringUtils.isNotBlank(user_id)) {

                Map<String, Object> m3 = DBHelper.queryForMap("SELECT 1 FROM ACCOUNT_USER V WHERE V.USER_ID=?", user_id);
                if (m3 != null) {
                    throw new RuntimeException("该用户已经绑定登录账号，如需更换，请先解绑！");
                }
                Map<String, Object> m2 = DBHelper.queryForMap("SELECT 1 FROM ACCOUNT_USER V WHERE V.ACCOUNT_ID=?", id);
                if (m2 != null) {
                    throw new RuntimeException("该登录账号已经绑定用户，如需更换，请先解绑！");
                }
                DBHelper.execute("INSERT INTO ACCOUNT_USER (ACCOUNT_ID,USER_ID)VALUES(?,?)", id, user_id);
            }
        } else {// 修改
            DBHelper.execute("UPDATE ACCOUNT SET LOGINNAME=?,WX_ACCOUNT=?,STATUS=?,REMARK=?,UPDATE_ID=?,UPDATE_TIME=SYSDATE WHERE ID=?",
                    loginname, wx_account, status, remark, u.getUser_id(), id);
        }
        //如果还传入了roleid 则需要在绑定一下角色
        if (StringUtils.isNotBlank(role)) {
            DBHelper.execute("DELETE FROM ACCOUNT_ROLE WHERE ACCOUNT_ID=?", id);
            String[] id_s = role.split("\\,");
            for (int i = 0; i < id_s.length; i++) {
                DBHelper.execute("INSERT INTO ACCOUNT_ROLE (ACCOUNT_ID,ROLE_ID)VALUES(?,?)", id, id_s[i]);
            }
        }


    }


    /**
     * 删除
     */
    public void del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {

            if (!StringUtils.equals("1", id_s[i])) { //ROOT不能删除
                // 绑定用户的账号不能删除，必须先解绑
                String ss_id = DBHelper.queryForScalar("select ss_id FROM TB_USER where  id =?",String.class,id_s[i]);
                String account_id = DBHelper.queryForScalar("select ACCOUNT_ID FROM ACCOUNT_USER where  USER_ID =?",String.class,ss_id);
                DBHelper.execute("DELETE FROM ACCOUNT_USER WHERE account_id = ?", account_id);
                DBHelper.execute("UPDATE ACCOUNT SET STATUS=? WHERE ID = ? ", Constants.ACCOUNT_DEL, id_s[i]);
            } else {
                throw new RuntimeException("ROOT账号不能删除");

            }

        }
    }

    /**
     * 删除
     */
    @Transactional(value = "primaryTransactionManager")
    public void realDel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            if (!StringUtils.equals("1", id_s[i])) { //ROOT不能删除
                // 绑定用户的账号不能删除，必须先解绑
                DBHelper.execute("DELETE FROM ACCOUNT WHERE ID = ? ", id_s[i]);
                DBHelper.execute("DELETE FROM ACCOUNT_USER WHERE ACCOUNT_ID = ? ", id_s[i]);
                DBHelper.execute("DELETE FROM ACCOUNT_ROLE WHERE ACCOUNT_ID = ? ", id_s[i]);
            } else {
                throw new RuntimeException("ROOT账号不能删除");
            }

        }
    }


    /**
     * 获取可绑定的账号
     *
     * @param
     * @return
     */
    public List<Map<String, Object>> getBind() {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT A.ID,A.LOGINNAME FROM ACCOUNT A ");
        sql.append(" LEFT JOIN ACCOUNT_USER AU ON AU.USER_ID=A.ID ");
        sql.append(" WHERE AU.ACCOUNT_ID IS NULL ");
        return DBHelper.queryForList(sql.toString());
    }

    /**
     * 获取可绑定的用户
     * STATUS = 1 否则会绑定禁用的用户
     * @param
     * @return
     */
    public List<Map<String, Object>> getBindUser() {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SU.ID,SU.USERNAME FROM SS_USER SU ");
        sql.append(" LEFT JOIN ACCOUNT_USER AU ON AU.USER_ID=SU.ID ");
        sql.append(" WHERE AU.USER_ID IS NULL AND SU.STATUS = 1");
        return DBHelper.queryForList(sql.toString());
    }

    /**
     * 绑定账号
     *
     * @param
     * @return
     */
    public void bind(Map<String, String> param) {
        String account_id = param.get("account_id");
        String user_id = param.get("user_id");
        //先判断当前账号是否有绑定
        Map<String, Object> m3 = DBHelper.queryForMap("SELECT 1 FROM ACCOUNT_USER V WHERE V.ACCOUNT_ID=? ", account_id);
        if (m3 != null) {
            throw new RuntimeException("该账号已经绑定用户，如需更换，请先解绑！");
        }
        Map<String, Object> m2 = DBHelper.queryForMap("SELECT 1 FROM ACCOUNT_USER V WHERE V.USER_ID=?", user_id);
        if (m2 != null) {
            throw new RuntimeException("该用户已经绑定账号，如需更换，请先解绑！");
        } else {
            DBHelper.execute("INSERT INTO ACCOUNT_USER (ACCOUNT_ID,USER_ID)VALUES(?,?)", account_id, user_id);
        }
    }

    /**
     * 解绑账号
     *
     * @param
     * @return
     */
    public void delBind(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {

            if (!StringUtils.equals("1", id_s[i])) { //根目录不能删除
                DBHelper.execute("DELETE FROM ACCOUNT_USER WHERE ACCOUNT_ID=?", id_s[i]);
            } else {
                throw new RuntimeException("ROOT账号不能解绑");
            }
        }
    }

    @Override
    @Transactional(value = "primaryTransactionManager")
    public void saveAccountDevice( UserEntity reEntity ,Map<String, String> param){
        String deviceId = param.get("deviceId");
        String registrationID = param.get("registrationID");
        if(StringUtils.isBlank(deviceId)){
            throw new JSONException("登陆设备ID参数不能为空");
        }
        Map<String,Object> map = DBHelper.queryForMap("select t.account_id,t.deviceid from ACCOUNT_DEVICE t where t.account_id = ?",reEntity.getId());
        if(map == null){
            DBHelper.execute("insert into account_device(account_id,deviceid,registrationid) values(?,?,?) ",reEntity.getId(),deviceId,registrationID);
        }else if(!deviceId.equals(map.get("DEVICEID").toString())){
            throw new JSONException("请使用绑定的设备登陆，若要更换，请联系管理员解绑");
        }
    }
}
