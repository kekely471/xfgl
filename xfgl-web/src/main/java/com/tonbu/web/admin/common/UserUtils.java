package com.tonbu.web.admin.common;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;

import java.util.List;
import java.util.Map;

public class UserUtils {

    public static Map<String,Object> getUserByToken(String userId){
        UserEntity userEntity = CustomRealm.GetLoginUser(userId);
        List<Map<String, Object>> user = DBHelper.queryForList("select  id,phone,email,idcard,name,job," +
                "avatar,acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)jobName," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK)POLICERANK," +
                "to_char(CREATETIME,'YYYY-MM-DD HH24:MI:SS')CREATETIME,to_char(UPDATETIME,'YYYY-MM-DD HH24:MI:SS')UPDATETIME," +
                "DWBM,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'dwmc'  AND BD.VALUE = t.DWMC)DWMC,SJDWBM," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'sjdwmc'  AND BD.VALUE = t.SJDWMC)SJDWMC,RWNY,SS_ID,TQXJTS,NXJTS,TQXJTSSFJB,SFKYLDZS,NATIVE_PLACE    from   tb_user t where t.SS_ID=? ", userEntity.getUser_id());
        if (user != null && user.size()> 0){
            return user.get(0);
        }else {
            return null;
        }

    }
}
