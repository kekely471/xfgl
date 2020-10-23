package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.data.ResultEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@RequestMapping(value = "/init")
@Controller
public class InitAction {
    Logger logger = LogManager.getLogger(InitAction.class.getName());

    @RequestMapping(value = "/friend")
    @ResponseBody
    @Transactional(value = "primaryTransactionManager")
    public Object initFriendsInfo() {

        ResultEntity r = new ResultEntity(1);

        try {
            //新增
            //获取所有用户
            List<Map<String, Object>> allUsers = DBHelper.queryForList(" select u.id from ss_user u where u.status <> '-1' ");
            for (Map<String, Object> loop : allUsers) {
                //当前用户id
                String userId = loop.get("id") == null ? "" : loop.get("id") + "";
                int count = DBHelper.queryForScalar(" select count(*) from im_friend_type where user_id=? and is_default='1' ", Integer.class, userId);
                //如果没有默认好友分组，添加默认分组
                if (count == 0) {
                    DBHelper.execute("insert into im_friend_type (id,type_name,user_id,create_time,is_default) values (seq_im_friend_type.nextval,'好友',?,sysdate,'1')", userId);
                }
                List<Map<String, Object>> allFriends = DBHelper.queryForList(" select u.id from ss_user u where u.id not in (select f.friend_id from im_friend f where f.user_id=?) and u.id <> ? and u.status <> '-1' ", userId, userId);

                //查询默认分组id
                int typeId = DBHelper.queryForScalar(" select id from im_friend_type where user_id=? and is_default='1' ", Integer.class, userId);
                for (Map<String, Object> friend : allFriends) {
                    //好友id
                    String friendId = friend.get("id") == null ? "" : friend.get("id") + "";
                    DBHelper.execute("insert into im_friend (id,user_id,friend_id,create_time,type_id) values (seq_im_friend.nextval,?,?,sysdate,?) ", userId, friendId, typeId);
                }

            }

            //删除好友
            List<Map<String, Object>> checkUsers = DBHelper.queryForList(" select distinct user_id from im_friend ");
            for (Map<String, Object> loop : checkUsers) {
                //用户id
                String userId = loop.get("user_id") == null ? "" : loop.get("user_id") + "";
                int count = DBHelper.queryForScalar(" select count(*) from ss_user u where u.status <> '-1' and u.id=? ", Integer.class, userId);
                //此用户不存在系统中
                if (count == 0) {

                    //删除用户以及其他用户的这个好友
                    DBHelper.execute(" delete from im_friend where user_id = ? or friend_id = ? ", userId, userId);
                }
            }

            //更新昵称默认昵称
            DBHelper.execute(" update ss_user set nick_name = username  where status <> -1 and nick_name is null ");

            r.setMsg("操作成功");
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
