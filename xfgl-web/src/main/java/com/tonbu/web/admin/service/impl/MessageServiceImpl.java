package com.tonbu.web.admin.service.impl;


import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.AppConstant;
import com.tonbu.web.admin.service.MessageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    /**
     * 通知列表
     */
    public Page<?> loadList(Map<String, String> param) {


        String key = param.get("key_name");
        String isMy = param.get("isMy");
        UserEntity u = CustomRealm.GetLoginUser();
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();

        sql.append(" SELECT V.ID,V.CONTENT,V.TITLE,V.MESSAGE_TYPE,TO_CHAR(V.SEND_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') SEND_TIME,V.SEND_ID,SU.USERNAME SEND_USERNAME,V.STATUS,V.PROCESS_ID,V.TABLE_ID");
        if(StringUtils.isNotBlank(isMy)){
            sql.append(",MU.STATUS READ_STATUS ");
        }
        sql.append(" FROM MESSAGE V ");
        if(StringUtils.isNotBlank(isMy)){
            sql.append(" LEFT JOIN MESSAGE_USER MU ON MU.MESSAGE_ID = V.ID LEFT JOIN SS_USER SU ON SU.ID = MU.USER_ID ");
        }else {
            sql.append(" LEFT JOIN SS_USER SU ON V.SEND_ID = SU.ID ");
        }
        sql.append(" WHERE  V.STATUS <> " + AppConstant.STATUS_DEL); // 已经删除的记录
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND V.CONTENT LIKE ? ");
            args.add("%" + key + "%");
        }
        if(StringUtils.isNotBlank(isMy)){
            sql.append(" AND MU.USER_ID = ?");
        }else {
            sql.append(" AND SEND_ID = ? ");
        }
            args.add(u.getUser_id());

        //拼接排序
        String sord = param.get("sord");
        String sidx = param.get("sidx");
        if (StringUtils.isNotBlank(sidx)) {
            sql.insert(0, "SELECT * FROM (");
            sql.append(") T ORDER BY T." + sidx + " " + sord);
        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(),args.toArray());
    }


    /**
     * 保存/修改通知
     */
    @Transactional(value = "primaryTransactionManager")
    public void save(Map<String, String> param) {
        String id = param.get("id");
        String title = param.get("title");//消息标题
        String content = param.get("content");//消息内容
        String message_type = param.get("message_type");//消息类型
        String status = param.get("status") == null ? "0" : "1";//状态
        String process_id = param.get("process_id");//业务编号
        String table_id = param.get("table_id");//业务表编号
        String receive_id = param.get("receive_id");//接收人
        String[] receive_ids = receive_id.split("\\+");
        UserEntity u = CustomRealm.GetLoginUser();
        if (StringUtils.isBlank(id)) {//新增
            DBHelper.execute("INSERT INTO MESSAGE(ID,TITLE,CONTENT,MESSAGE_TYPE,SEND_TIME,SEND_ID,STATUS,PROCESS_ID,TABLE_ID) VALUES(SEQ_MESSAGE.NEXTVAL,?,?,?,SYSDATE,?,?,?,?)",
                    title,content,message_type,u.getUser_id(),status,process_id,table_id);
            id = DBHelper.queryForScalar("SELECT SEQ_MESSAGE.CURRVAL FROM DUAL", Integer.class).toString();

        } else {// 修改
            DBHelper.execute("UPDATE MESSAGE SET TITLE=?,CONTENT=?,MESSAGE_TYPE=?,SEND_TIME=SYSDATE,SEND_ID=?,STATUS=?,PROCESS_ID=?,TABLE_ID=? WHERE ID=?",
                    title,content,message_type,u.getUser_id(),status,process_id,table_id,id);

            //删除关系表数据
            DBHelper.execute("DELETE FROM MESSAGE_USER WHERE MESSAGE_ID = ?",id);
        }
        //如果存在接收人
        if(receive_id.length() > 0){
            for(String str : receive_ids){
                DBHelper.execute("INSERT INTO MESSAGE_USER(MESSAGE_ID,USER_ID,STATUS) VALUES (?,?,?)",id,str, AppConstant.STATUS_DISABLED);
            }
        }
    }

    /**
     * 消息删除
     */
    @Transactional(value = "primaryTransactionManager")
    public void del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("UPDATE MESSAGE SET STATUS = ? WHERE ID = ?", AppConstant.STATUS_DEL, id_s[i]);
        }
    }

    /**
     * 消息彻底删除
     */
    @Transactional(value = "primaryTransactionManager")
    public void realDel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("DELETE FROM MESSAGE WHERE ID = ?", id_s[i]);
            DBHelper.execute("DELETE FROM MESSAGE_USER WHERE MESSAGE_ID = ?", id_s[i]);
        }
    }

    /**
     * 修改查看状态
     */
    public void update(String id) {
        UserEntity u = CustomRealm.GetLoginUser();
        DBHelper.execute("UPDATE MESSAGE_USER SET STATUS = ? WHERE MESSAGE_ID = ? AND USER_ID = ?", AppConstant.STATUS_ENABLED, id,u.getUser_id());

    }

    /**
     * 获取用户数据源
     *
     * @return
     */
    public List<Map<String, Object>> loadUserList() {
        return DBHelper.queryForList("SELECT T.ID,T.USERNAME NAME FROM ACCOUNT_USER AU LEFT JOIN SS_USER T ON AU.USER_ID = T.ID AND T.STATUS <> ? ", AppConstant.STATUS_DEL);
    }
}
