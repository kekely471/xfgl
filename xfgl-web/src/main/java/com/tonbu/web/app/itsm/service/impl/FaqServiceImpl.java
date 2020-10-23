package com.tonbu.web.app.itsm.service.impl;

import com.tonbu.web.AppConstant;
import com.tonbu.web.app.itsm.service.FaqService;
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
public class FaqServiceImpl implements FaqService {

    /**
     * 列表
     */
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        String faq_type = param.get("faq_type");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT V.ID,V.TITLE,V.CREATE_ID,TO_CHAR(V.CREATE_TIME,'"+Constants.ORACLE_FORMATDATE_FULL +"') CREATETIME,V.CONTENT,V.FAQ_TYPE,V.KEYWORDS,V.UPDATE_ID,V.UPDATE_TIME,V.VIEW,V.STATUS  ");
        sql.append(" FROM FAQ V ");
        sql.append(" WHERE V.STATUS <> "+ AppConstant.STATUS_DEL);
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND ( V.TITLE LIKE ? OR V.CONTENT LIKE ? ) ");
            args.add("%" + key + "%");
            args.add("%" + key + "%");
        }
        if(StringUtils.isNotBlank(faq_type)){
            sql.append(" AND V.FAQ_TYPE=? ");
            args.add(faq_type);
        }
        //拼接排序
        String sord = param.get("sord");
        String sidx = param.get("sidx");
        if (StringUtils.isNotBlank(sidx)) {
            sql.insert(0,"SELECT * FROM (");
            sql.append(") T ORDER BY T." + sidx + " " + sord);
        }else{
            sql.append(" ORDER BY V.SORT ASC");
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
        String faq_type = param.get("faq_type");
        String title = param.get("title");
        String content = param.get("content");
        String keywords = param.get("keywords");
        String status = param.get("status") == null ? "0" : "1";
        String file_ids = param.get("file_ids");
        UserEntity u=CustomRealm.GetLoginUser();
        if (StringUtils.isBlank(id)) {
            // 新增
            DBHelper.execute("INSERT INTO FAQ(FAQ_TYPE,KEYWORDS,TITLE,CONTENT,STATUS,CREATOR,CREATE_TIME,UPDATE_ID,UPDATE_TIME,STATUS) VALUES(?,?,?,?,?,?,SYSDATE,?,SYSDATE.?)",
                    faq_type,keywords, title, content, status, u.getUser_id(),u.getUser_id(),status);
            id = DBHelper.queryForScalar("SELECT @@LAST_INSERT_ID", String.class);// 获取ID
        } else {
            // 修改
            DBHelper.execute("UPDATE FAQ SET FAQ_TYPE=?,KEYWORDS=?,TITLE=?,CONTENT=?,STATUS=?,EDITOR=?,EDIT_TIME=SYSDATE,UPDATE_ID=?,UPDATE_TIME=SYSDATE,STATUS=? WHERE ID=?",
                    faq_type, title, content, status, u.getUser_id(),u.getUser_id(),status, id);
            DBHelper.execute("DELETE FROM FAQ_FILE WHERE FAQ_ID = ? ", id);
        }
        if (StringUtils.isNotBlank(file_ids)) {
            String[] ids = file_ids.split(",");

            if (ids.length > 0) {
                for (String fileid : ids) {
                    DBHelper.execute("INSERT INTO FAQ_FILE (FAQ_ID ,BUSINESS_FILE_ID) VALUES(?,?)", id, fileid);
                }
            }
        }
    }

    @Override
    public void publish(Map<String, String> param) {

    }

    @Override
    public void unpublish(Map<String, String> param) {

    }

    /**
     * 删除
     */
    public void del(Map<String, String> param) {
        String ids=param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("UPDATE FAQ SET STATUS = ? WHERE ID = ?", AppConstant.STATUS_DEL, id_s[i]);
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
            DBHelper.execute("DELETE FROM FAQ_FILE WHERE CONTRACT_ID=?", id_s[i]);
            DBHelper.execute("DELETE FROM FAQ WHERE ID = ?", id_s[i]);
        }
    }




}
