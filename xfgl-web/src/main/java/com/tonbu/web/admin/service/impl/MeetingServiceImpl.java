package com.tonbu.web.admin.service.impl;


import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;

import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.AppConstant;
import com.tonbu.web.admin.service.MeetingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MeetingServiceImpl implements MeetingService {

    /**
     * 会议列表
     */
    @Override
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        String deptid = param.get("pid");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();

        sql.append(" SELECT V.ID,V.TITLE,V.CONTENT,V.MEETING_TYPE,TO_CHAR(V.MEETING_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') MEETING_TIME,TO_CHAR(V.CREATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') CREATE_TIME,V.CREATE_ID,V.MEETING_ADDRESS,V.DEPT_ID,");
        sql.append(" ( SELECT USERNAME FROM SS_USER SU WHERE SU.ID = V.CREATE_ID ) AS HOST_USERNAME,(SELECT WM_CONCAT(TO_CHAR(MU.USER_ID)) FROM  MEETING_USER MU LEFT JOIN SS_USER SU ON SU.ID=MU.USER_ID WHERE MU.MEETING_ID = V.ID) AS JOIN_IDS ");
        sql.append(" FROM MEETING V ");
        sql.append(" WHERE 1=1");
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND V.TITLE LIKE ? ");
            args.add("%" + key + "%");
        }
        if (StringUtils.isNotBlank(deptid)) {
            sql.append(" AND( V.DEPT_ID = ? )");
            args.add(deptid);
        }
        sql.append(" ORDER BY V.CREATE_TIME DESC");
        //拼接排序
        String sord = param.get("sord");
        String sidx = param.get("sidx");
        if (StringUtils.isNotBlank(sidx)) {
            sql.insert(0, "SELECT * FROM (");
            sql.append(") T ORDER BY T." + sidx + " " + sord);
        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }

    /**
     * 我的会议列表
     */
    @Override
    public Page<?> loadMyList(Map<String, String> param) {
        String key = param.get("key_name");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        UserEntity u = CustomRealm.GetLoginUser();
        sql.append(" SELECT * FROM (");
        sql.append(" SELECT V.ID,V.TITLE,V.CONTENT,V.MEETING_TYPE,TO_CHAR(V.MEETING_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') MEETING_TIME,TO_CHAR(V.CREATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') CREATE_TIME,V.CREATE_ID,V.MEETING_ADDRESS,V.DEPT_ID,MU.STATUS READ_STATUS,");
        sql.append(" ( SELECT USERNAME FROM SS_USER SU WHERE SU.ID = V.CREATE_ID ) AS HOST_USERNAME, ROW_NUMBER ( ) OVER ( PARTITION BY MU.MEETING_ID ORDER BY V.CREATE_TIME ) AS CODE_ID ");
        sql.append(" FROM MEETING V ");
        sql.append(" LEFT JOIN MEETING_USER MU ON MU.MEETING_ID = V.ID LEFT JOIN SS_USER SU ON SU.ID = MU.USER_ID WHERE 1=1");
        if (StringUtils.isNotBlank(key)) {
            sql.append(" AND V.TITLE LIKE ? ");
            args.add("%" + key + "%");
        }
        sql.append(" AND (V.CREATE_ID = ? OR MU.USER_ID = ?) ");
        args.add(u.getUser_id());
        args.add(u.getUser_id());
        sql.append(" ) WHERE CODE_ID = 1");
        //拼接排序
        String sord = param.get("sord");
        String sidx = param.get("sidx");
        if (StringUtils.isNotBlank(sidx)) {
            sql.insert(0, "SELECT * FROM (");
            sql.append(") T ORDER BY T." + sidx + " " + sord);
        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }

    /**
     * 保存/修改通知
     */
    @Transactional(value = "primaryTransactionManager", rollbackFor = RuntimeException.class)
    @Override
    public void save(Map<String, String> param) throws ParseException{
            String id = param.get("id");
            String title = param.get("title");//会议标题
            String content = param.get("content");//会议内容
            //会议时间
            SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
            Timestamp meeting_time = new Timestamp(format.parse(param.get("meeting_time")).getTime());
            String meeting_address = param.get("meeting_address");//会议地点
            String meeting_type = param.get("meeting_type");//会议类型
            String create_id = param.get("create_id");//会议主持
            String join_id = param.get("join_ids");//参加人员
            String[] join_ids = join_id.split(",");
            UserEntity u = CustomRealm.GetLoginUser();
            if (StringUtils.isBlank(id)) {//新增
                DBHelper.execute("INSERT INTO MEETING(ID,TITLE,CONTENT,MEETING_TYPE,CREATE_TIME,CREATE_ID,MEETING_TIME,MEETING_ADDRESS,DEPT_ID) VALUES(SEQ_MEETING.NEXTVAL,?,?,?,SYSDATE,?,?,?,?)",
                        title, content, meeting_type, create_id, meeting_time, meeting_address, u.getDept_id());
                id = DBHelper.queryForScalar("SELECT SEQ_MEETING.CURRVAL FROM DUAL", Integer.class).toString();

            } else {// 修改
                DBHelper.execute("UPDATE MEETING SET TITLE=?,CONTENT=?,MEETING_TYPE=?,CREATE_TIME=SYSDATE,CREATE_ID=?,MEETING_TIME=?,MEETING_ADDRESS=? WHERE ID=?",
                        title, content, meeting_type, create_id, meeting_time, meeting_address, id);

                //删除关系表数据
                DBHelper.execute("DELETE FROM MEETING_USER WHERE MEETING_ID = ?", id);
            }
            //如果存在参加人员
            if (join_id.length() > 0) {
                for (String str : join_ids) {
                    DBHelper.execute("INSERT INTO MEETING_USER(MEETING_ID,USER_ID,STATUS) VALUES (?,?,?)", id, str, AppConstant.STATUS_DISABLED);
                }
            }
    }

    /**
     * 消息彻底删除
     */
    @Transactional(value = "primaryTransactionManager", rollbackFor = RuntimeException.class)
    @Override
    public void realDel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("DELETE FROM MEETING WHERE ID = ?", id_s[i]);
            DBHelper.execute("DELETE FROM MEETING_USER WHERE MEETING_ID = ?", id_s[i]);
        }
    }

    /**
     * 修改查看状态
     */
    @Transactional(value = "primaryTransactionManager", rollbackFor = RuntimeException.class)
    @Override
    public void update(Map<String, String> param) {
        String id = param.get("id");
        UserEntity u = CustomRealm.GetLoginUser();
        DBHelper.execute("UPDATE MEETING_USER SET STATUS = ? WHERE MEETING_ID = ? AND USER_ID = ?", AppConstant.STATUS_ENABLED, id, u.getUser_id());

    }
}
