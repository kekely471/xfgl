package com.tonbu.web.admin.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.KqpzService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
public class KqpzServiceImpl implements KqpzService{

    @Override
    public Page<?> loadList(Map<String, String> param) {
        String key = param.get("key_name");
        String type = param.get("type");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("select t.id,t.DZ,t.JD,t.WD,t.FW,t.SBSJ,T.XBSJ,（SELECT DEPTNAME NAME FROM SS_DEPT s WHERE  s.id= t.bm AND s.STATUS <> '-1')DWMC,T.BM,t.STATUS  ");
        sql.append(" from TB_KQPZ t where 1=1 ");
        if(StringUtils.isNotBlank(key)){
            sql.append(" and t.dz like ? ");
            args.add("%" + key + "%");
        }
        sql.append(" ORDER BY t.ID desc");
        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
    }

    @Override
    public void save(Map<String, String> param) {
        String id = param.get("id");
        String dz = param.get("dz");
        String fw = param.get("fw");
        String jd = param.get("jd");
        String wd = param.get("wd");
        String bm = param.get("dwmc");
        String sbsj = param.get("sbsj");
        String xbsj = param.get("xbsj");
        String status = param.get("status") == null ? "0" : "1";
        UserEntity u = CustomRealm.GetLoginUser();
        if (StringUtils.isBlank(id)) {// 新增
            DBHelper.execute("INSERT INTO TB_KQPZ(ID,DZ,FW,JD,WD,STATUS,BM,SBSJ,XBSJ) " +
                            "VALUES(SEQ_TB_KQPZ.NEXTVAL,?,?,?,?,?,?,?,?)",
                 dz,fw,jd,wd,status,bm,sbsj,xbsj);
        } else {// 修改
            DBHelper.execute("UPDATE TB_KQPZ SET DZ=?,FW=?,JD=?,WD=?,STATUS=?,BM=?,SBSJ=?,XBSJ=? WHERE ID=?",
                    dz,fw,jd,wd,status,bm,sbsj,xbsj,id);
        }
    }


    @Override
    public void realDel(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute(" delete from tb_kqpz where id=?",id_s[i]);
        }
    }
}
