package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.web.admin.service.GzqglService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GzqglServieImpl implements GzqglService {
    @Override
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from TB_GZQ where 1=1 ");
        sql.append("  ");
        sql.append("  ");
        if(StringUtils.isNotBlank(key)){
            sql.append(" and  NAME  like ? ");
            args.add("%" + key + "%");
        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }

    @Override
    public void del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute(" delete from TB_GZQ where id=?",id_s[i]);
            DBHelper.execute(" delete from TB_COMMENT where businessid=?",id_s[i]);
        }
    }

    @Override
    public Page<?> loadPlList(Map<String, String> param) {
        String key = param.get("key_name");
        String id = param.get("id");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select t.id,case t.comments_status when '1' then '匿名' else u.name end name,t.comment_conent, ");
        sql.append(" t.create_time,t.update_time from TB_COMMENT T ");
        sql.append("  ");
        sql.append(" left join TB_USER U on t.COMMENT_TO_ACCOUNT=u.ss_id ");
        sql.append("  ");
        sql.append(" where t.data_status=0 AND T.businessid=? ");
        args.add(id);
//        if(StringUtils.isNotBlank(key)){
//            sql.append(" and  t.COMMENT_CONENT  like ? ");
//            args.add("%" + key + "%");
//        }
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }

    @Override
    public void pldel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute(" delete from TB_COMMENT where id=?",id_s[i]);
        }
    }
}
