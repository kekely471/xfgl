package com.tonbu.web.app.itsm.service.impl;

import com.tonbu.web.AppConstant;
import com.tonbu.web.app.itsm.service.ContractService;
import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.framework.data.Limit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ContractServiceImpl implements ContractService {



    /**
     * 列表(左边GRID)
     */
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        String pid = param.get("pid");

        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT V.ID,V.CONTRACTNAME,V.STATUS,V.SIGN_USER,TO_CHAR(V.SIGN_TIME,'"+Constants.ORACLE_FORMATDATE_FULL +"') SIGNTIME,V.REMARK,V.UPDATE_TIME,V.UPDATE_ID,V.CREATE_TIME,V.CREATE_ID," +
                "V.CONTRACTNO,V.CONTRACTDESCR,V.CONTRACTAMOUNT,V.CONTRACT_SDATE,V.CONTRACT_EDATE,V.DEPT_ID,V.BESIGN_USER,BD.DEPTNAME AS DEPT_NAME, " +
                "TO_CHAR(V.CONTRACT_SDATE,'"+Constants.ORACLE_FORMATDATE_SHORT +"')||~||TO_CHAR(V.CONTRACT_EDATE,'"+Constants.ORACLE_FORMATDATE_SHORT +"') CONTRACT_DATE ");
        sql.append(" FROM BUSINESS_CONTRACT V ");
        sql.append(" LEFT JOIN SS_DEPT BD ON BD.ID=V.DEPT_ID ");
        sql.append(" WHERE V.STATUS <> " + AppConstant.STATUS_DEL);
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND ( V.CONTRACTNAME LIKE ? OR V.CONTRACTNO LIKE ? ) ");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
        }
        if (StringUtils.isNotBlank(pid)) {
            sql.append(" AND V.DEPT_ID = ? ");
            args.add(pid);
        }
        //拼接排序
        String sord = param.get("sord");
        String sidx = param.get("sidx");
        if (StringUtils.isNotBlank(sidx)) {
            sql.insert(0, "SELECT * FROM (");
            sql.append(") T ORDER BY T." + sidx + " " + sord);
        } else {
            sql.append(" ORDER BY V.SIGNTIME DESC");
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
        String dept_id = param.get("dept_id");
        String contractname = param.get("contractname");
        String contractno = param.get("contractno");
        String contract_sdate = param.get("contract_sdate");
        String contract_edate = param.get("contract_edate");
        String besign_user = param.get("besign_user");
        String sign_user = param.get("sign_user");
        String sign_time = param.get("sign_time");
        String contractdescr = param.get("contractdescr");
        String remark = param.get("remark");
        String status = param.get("status") == null ? "0" : "1";
        String file_ids = param.get("file_ids");
        UserEntity u = CustomRealm.GetLoginUser();
        if (StringUtils.isBlank(id)) {// 新增
            DBHelper.execute("INSERT INTO BUSINESS_CONTRACT(CONTRACTNAME,DEPT_ID,CONTRACTNO,CONTRACT_SDATE,CONTRACT_EDATE,BESIGN_USER,SIGN_USER," +
                            "SIGN_TIME,CONTRACTDESCR,STATUS,REMARK,CREATE_ID,CREATE_TIME,UPDATE_ID,UPDATE_TIME) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,SYSDATE,?,SYSDATE)",
                    contractname, dept_id, contractno, contract_sdate, contract_edate, besign_user, sign_user, sign_time, contractdescr, status, remark, u.getUser_id(), u.getUser_id());
            id=DBHelper.queryForScalar("SELECT LAST_INSERT_ID()",Integer.class).toString();
        } else {// 修改
            DBHelper.execute("UPDATE BUSINESS_CONTRACT SET CONTRACTNAME=?,DEPT_ID=?,CONTRACTNO=?,CONTRACT_SDATE=?,CONTRACT_EDATE=?,BESIGN_USER=?," +
                            "SIGN_USER=?,SIGN_TIME=?,CONTRACTDESCR=?,STATUS=?,REMARK=?,UPDATE_ID=?,UPDATE_TIME=SYSDATE WHERE ID=?",
                    contractname, dept_id, contractno, contract_sdate, contract_edate, besign_user, sign_user, sign_time, contractdescr, status, remark, u.getUser_id(), id);
        }
        String[] ids = file_ids.split(",");
        DBHelper.execute("DELETE FROM  BUSINESS_CONTRACT_FILE WHERE CONTRACT_ID=?", id);
        if (ids.length > 0) {
            for (String fileid : ids) {
                if(StringUtils.isNotBlank(file_ids)) {
                    DBHelper.execute("INSERT INTO BUSINESS_CONTRACT_FILE ( CONTRACT_ID,BUSINESS_FILE_ID) VALUES (?,?)", id, fileid);
                }
            }
        }
    }

    /**
     * 删除
     */
    public void del(Map<String, String> param) {
        String ids=param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("UPDATE BUSINESS_CONTRACT SET STATUS = ? WHERE ID = ?", AppConstant.STATUS_DEL, id_s[i]);
        }
    }

    /**
     * 删除
     */
    @Transactional(value = "primaryTransactionManager")
    public void realDel(Map<String, String> param) {
        String ids=param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("DELETE FROM BUSINESS_CONTRACT_FILE WHERE CONTRACT_ID=?", id_s[i]);
            DBHelper.execute("DELETE FROM BUSINESS_CONTRACT WHERE ID = ?", id_s[i]);
        }
    }

}
