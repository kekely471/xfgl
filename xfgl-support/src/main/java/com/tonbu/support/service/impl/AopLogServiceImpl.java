package com.tonbu.support.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.support.service.AopLogService;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class AopLogServiceImpl implements AopLogService {

    @Override
    public void save(Map<String, String> param) {
//        DBHelper.execute("INSERT INTO SS_LOG_OPERATE(ID, PARAM,URL ,USER_ID ,USER_NAME,USER_APP," +
//                        "RESPONSE_INFO,OPERATION_TYPE,USER_IP,LOCATION,MODEL_NAME ,CONTENT_BACK , ACCOUNT_ID ,OPERATION_TIME) " +
//                        " VALUES(SEQ_SS_LOG_OPERATE.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?,SYSDATE)",
//                param.get("param"),param.get("url"),param.get("userId"),param.get("userName"),param.get("userApp"),
//                param.get("responseInfo"),param.get("operateionType"),param.get("userIp"),param.get("location"),
//                param.get("modelName"),param.get("contentBack"),param.get("accountId"));
    }
}
