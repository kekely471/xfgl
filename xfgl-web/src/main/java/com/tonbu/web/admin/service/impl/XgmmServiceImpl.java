package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.web.admin.service.XgmmService;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
public class XgmmServiceImpl  implements XgmmService{

    @Override
    public void save(Map<String, String> param) {
        String ss_id = param.get("ss_id");
        String phone =param.get("phone");
        String sd=DBHelper.queryForScalar("SELECT PHONE FROM  TB_USER WHERE SS_ID=?",String.class,ss_id);
        String account_id=DBHelper.queryForScalar("select ID from  ACCOUNT  WHERE LOGINNAME=?",String.class,sd);
        DBHelper.execute("UPDATE ACCOUNT SET LOGINNAME=?,UPDATE_TIME=SYSDATE WHERE ID=? ",phone,account_id);
        DBHelper.execute("UPDATE TB_USER SET ACCOUNT=?,PHONE=?,UPDATETIME=SYSDATE  WHERE  SS_ID=?",phone,phone,ss_id);

        String uid=DBHelper.queryForScalar("select ID from  SS_USER  WHERE  mobile=?",String.class,sd);

        DBHelper.execute("UPDATE SS_USER  SET MOBILE=?,UPDATE_TIME=SYSDATE  WHERE ID=?",phone,uid);
    }
}
