package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.aop.AopLog;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.support.util.CryptUtils;
import com.tonbu.web.admin.action.AccountAction;
import com.tonbu.web.admin.service.AccService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class AccServiceImpl implements AccService {

    Logger logger = LogManager.getLogger(AccountAction.class.getName());

    /**
     * 　修改密码
     *
     * @return ResultEntity
     */
    @AopLog(module = "账号管理",method="密码修改")
    public ResultEntity updatePassword(String oldpassword, String password, String password2) {
        ResultEntity r = new ResultEntity(1);
        try {
            UserEntity u = CustomRealm.GetLoginUser();
            String slt = u.getSalt();
            String account_id = u.getId();
            String oldpwd = CryptUtils.md5(oldpassword, slt);
            if (!StringUtils.equals(u.getPassword(), oldpwd)) {

                r.setCode(-1);
                r.setMsg("旧密码不正确！");
                return r;

            }
            if (!StringUtils.equals(password, password2)) {
                r.setCode(-1);
                r.setMsg("两次输入的新密码不一致！");
                return r;
            }
            String npwd = CryptUtils.md5(password, slt);
            DBHelper.update("UPDATE ACCOUNT SET PASSWORD=? WHERE ID=? ", npwd, account_id);
            r.setMsg("密码修改成功！");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }
}
