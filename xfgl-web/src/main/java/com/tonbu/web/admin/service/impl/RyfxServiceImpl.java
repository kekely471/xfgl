package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.web.admin.service.RyfxService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RyfxServiceImpl implements RyfxService{

    @Override
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        String dw=param.get("dwmc");
//        String type = param.get("type");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("select t.ID,\n" +
                "       t.SS_ID,\n" +
                "       t.NAME,\n" +
                "       t.SEX,\n" +
                "       t.NL,\n" +
                "       t.MZ,\n" +
                "       t.SG,\n" +
                "       t.JBZL,\n" +
                "       t.GRCZS,\n" +
                "       t.JSZT,\n" +
                "       t.STZT,\n" +
                "       t.SHGN,\n" +
                "       t.GRZXQK,\n" +
                "       t.CZWT,\n" +
                "       t.DCFX,\n" +
                "       to_char(t.CREATE_TIME, 'YYYY-MM-DD HH24:MI:SS') CREATE_TIME,\n" +
                "       to_char(t.UPDATE_TIME, 'YYYY-MM-DD HH24:MI:SS') UPDATE_TIME,\n" +
                "       t.CZR,\n" +
                "       t.SFTJ,\n" +
                "       (SELECT NAME\n" +
                "          FROM BUSINESS_DICT BD\n" +
                "         WHERE BD.TYPE_CODE = 'zw'\n" +
                "           AND BD.VALUE = t.ZJ)ZJ_NAME,\n" +
                "       （SELECT DEPTNAME NAME\n" +
                "  FROM SS_DEPT s\n" +
                " WHERE s.id = t.DWMC\n" +
                "   AND s.STATUS <> '-1')DWMC_NAME, t.STATUS ,t.RYSTATUS\n");
        sql.append("  from TB_RYFX t  where 1=1 ");
        if (StringUtils.isNotBlank(dw)) {
            sql.append(" AND t.dwmc = ? ");
            args.add("" + dw + "");
        }
        if(StringUtils.isNotBlank(key)){
            sql.append(" and t.NAME like ? ");
            args.add("%" + key + "%");
        }
        sql.append(" ORDER BY t.ID desc");


        Limit limit = new Limit(param);
        Page<?> p = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
        return p;
    }
}
