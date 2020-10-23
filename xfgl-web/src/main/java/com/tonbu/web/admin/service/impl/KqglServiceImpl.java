package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.web.admin.service.KqglService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
public class KqglServiceImpl implements KqglService{
    @Override
    public Page<?> loadList(Map<String, String> param) {
//        String key = param.get("key_name");
//        String sj=param.get("sj");
            String dw=param.get("dwmc");
        String name=param.get("key_name");
        String rq=param.get("test6");

        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append( "select t.ID,\n" +
                "   t.ACCOUNT,\n" +
                "   to_char(t.SBTIME, 'YYYY-MM-DD HH24:MI:SS')as SBTIME,\n" +
                "   t.CHECKTYPE,\n" +
                "   t.USERNAME,\n" +
                "   t.JD,\n" +
                "   t.WD,\n" +
                "   to_char(t.create_time, 'YYYY-MM-DD HH24:MI:SS')as CREATETIME,\n" +
                "   to_char(t.create_time, 'YYYY-MM-DD')as KQRQ,\n" +
                "   to_char(t.update_time, 'YYYY-MM-DD HH24:MI:SS')as UPDATETIME,\n" +
                "   t.MOBLIETIME,\n" +
                "  t.DZ,u.DWMC AS DW,\n" +
                "  to_char(t.XBTIME, 'YYYY-MM-DD HH24:MI:SS') XBTIME,\n" +
                "  (select DEPTNAME NAME  FROM SS_DEPT s WHERE  s.id=u.dwmc)as DWMC_NAME\n" +
                "  ,t.IMEL  from TB_KQ_ATTENDANCE t left  join  tb_user u  on  u.name=t.username where 1=1  "); // 已经删除的记录
        if (StringUtils.isNotBlank(dw)) {
            sql.append(" AND U.DWMC = ? ");
            args.add("" + dw + "");
        }
        if (StringUtils.isNotBlank(name)) {
            sql.append(" AND t.USERNAME LIKE  ? ");
            args.add("%" + name + "%");
        }

        if(StringUtils.isBlank(rq)){
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD ");
            String kssj = sdf.format(new Date());

        }else {
            String ks[]=rq.split(" - ");
            sql.append("AND to_char(t.SBTIME, 'YYYY-MM-DD ') between ? and  ?");
            args.add(ks[0]);
            args.add(ks[1]);
        }
        sql.append("order by t.SBTIME  desc");
        Limit limit = new Limit(param);
        Page<?> p = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
        return p;
    }
}
