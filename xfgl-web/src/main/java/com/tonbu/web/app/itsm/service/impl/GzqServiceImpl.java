package com.tonbu.web.app.itsm.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.app.itsm.service.GzqService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
public class GzqServiceImpl  implements GzqService{

    @Override
    public Page<?> loadList(Map<String, String> param,UserEntity u) {
      List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("select t.id,\n" +
                "                       t.account,\n" +
                "                       t.content,\n" +
                "                       t.remark,t.name,\n" +
                "                      (select count(c.id) from  tb_comment c where c.businessid=t.id and c.data_status<>1)as good_num,\n" +
                "                       (select count(g.id) from tb_good g where g.businessid=t.id and g.good_status=1)as star_num,\n" +
                "                       t.publish_platform,\n" +
                "                       t.address,\n" +
                "                       to_char(t.create_time, 'yyyy-MM-dd HH24:mi:ss') create_time,\n" +
                "                       to_char(t.update_time, 'yyyy-MM-dd HH24:mi:ss') update_time,TU.AVATAR \n" +
                "                  from TB_GZQ t LEFT JOIN  TB_USER TU ON TU.SS_ID=T.SS_ID  order by t.create_time desc \n" +
                "                  ");

        Limit limit = new Limit(param);
        Page page = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
        List<Map<String,Object>> list = (List<Map<String,Object>>)page.getRows();

        for(int i=0;i<list.size();i++){
            Map<String,Object> map=list.get(i);
            List<Map<String,Object>>zlist=DBHelper.queryForList("select  TB_FILE_ID  from tb_gzq_files  where GZQID=?",map.get("ID").toString());
//            List<Map<String,Object>>z=DBHelper.queryForList("select  GLID  from  TB_GZQ where ID=?",map.get("ID").toString());

//              String avatar=DBHelper.queryForScalar("select AVATAR   FROM TB_USER  WHERE id=?",String.class,z.get(i).toString());
//              System.out.println(z.get(i).toString());
//              map.put("avatar",avatar);

//            List<Map<String,Object>>dzlist=DBHelper.queryForList("select  GOOD_TO_ACCOUNT as DZZID,GOOD_STATUS  from tb_good  where BUSINESSID=?",map.get("ID"));

            List<Map<String,Object>>nlist=DBHelper.queryForList("select c.id,\n" +
                    "       c.comment_conent  as nr,\n" +
                    "       c.COMMENT_TO_ACCOUNT as plzid,\n" +
                    "       c.comments_status as plzt,t.name\n" +
                    "  from TB_COMMENT c\n" +
                    "  left join tb_user t on t.ss_id=c.comment_account where c.businessid=? and c.DATA_STATUS <> 1 order by c.create_time desc\n",map.get("ID"));
            map.put("FILELIST",zlist);
            map.put("NRLIST",nlist);
//            map.put("DZLIST",dzlist);

        }

        return page;


    }

    @Override
    public Page<?> loadListxq(Map<String, String> param,UserEntity u) {
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("select t.id,\n" +
                "    t.account,\n" +
                "    t.content,t.name,\n" +
                "    t.remark,\n" +
                "    (select count(c.id) from  tb_comment c where c.businessid=t.id and c.data_status<>1)as good_num,\n" +
                "    (select count(g.id) from tb_good g where g.businessid=t.id and g.good_status=1)as star_num,\n" +
                "   t.publish_platform,\n" +
                "   t.address,\n" +
                "  to_char(t.create_time, 'yyyy-MM-dd HH24:mi:ss') create_time,\n" +
                "   to_char(t.update_time, 'yyyy-MM-dd HH24:mi:ss') update_time,TU.AVATAR \n" +
                "  from TB_GZQ t  LEFT JOIN  TB_USER TU ON TU.SS_ID=T.SS_ID  \n" +
                "    where t.account=? ");
        args.add( u.getMobile());
        sql.append("order by t.create_time desc");
        Limit limit = new Limit(param);
        Page page = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
        List<Map<String,Object>> list = (List<Map<String,Object>>)page.getRows();

        for(int i=0;i<list.size();i++){
            Map<String,Object> map=list.get(i);
            List<Map<String,Object>>zlist=DBHelper.queryForList("select  TB_FILE_ID  from tb_gzq_files  where GZQID=?",map.get("ID").toString());
//            String avatar=DBHelper.queryForScalar("select AVATAR   FROM TB_USER  WHERE ss_id=?",String.class,param.get("ss_id"));
            List<Map<String,Object>>nlist=DBHelper.queryForList("select c.id,\n" +
                    "       c.comment_conent  as nr,\n" +
                    "       c.COMMENT_TO_ACCOUNT as plzid,\n" +
                    "       c.comments_status as plzt,t.name\n" +
                    "  from TB_COMMENT c\n" +
                    "  left join tb_user t on t.ss_id=c.comment_account where c.businessid=? and c.DATA_STATUS <> 1 order by c.create_time desc",map.get("ID"));
            map.put("FILELIST",zlist);
            map.put("NRLIST",nlist);

        }

        return page;

    }

    @Override
    public void save(Map<String, String> param, UserEntity u) {
        String id=DBHelper.queryForScalar("SELECT SEQ_GZQ.NEXTVAL from  DUAL",String.class);
        String  content=param.get("content");
        String publish_platform=param.get("publish_platform");
        String address=param.get("address");
        String name=param.get("userName");
        String  ss_id=param.get("ss_id");
        String tb_file_id=param.get("tb_file_id");

        if(null==tb_file_id||"".equals(tb_file_id)){
            tb_file_id="";
            DBHelper.execute("INSERT INTO TB_GZQ(ID,ACCOUNT,NAME,CONTENT,REMARK,STAR_NUM,GOOD_NUM,PUBLISH_PLATFORM,ADDRESS,CREATE_TIME,UPDATE_TIME,SS_ID)" +
                            "VALUES(?,?,?,?,?,?,?,?,?,SYSDATE,SYSDATE,?)",
                    id,u.getLoginname(),name,content,"","","",publish_platform,address,ss_id);
        }else {
            String []file_id=tb_file_id.split(",");
            DBHelper.execute("INSERT INTO TB_GZQ(ID,ACCOUNT,NAME,CONTENT,REMARK,STAR_NUM,GOOD_NUM,PUBLISH_PLATFORM,ADDRESS,CREATE_TIME,UPDATE_TIME,SS_ID)VALUES(?,?,?,?,?,?,?,?,?,SYSDATE,SYSDATE,?)",
                    id,u.getLoginname(),name,content,"","","",publish_platform,address,ss_id);
            for(int i=0;i<file_id.length;i++){
                String fileid=DBHelper.queryForScalar("SELECT SEQ_GZQ_FILES.NEXTVAL FROM DUAL",String.class);
                 String fileId=file_id[i];
                DBHelper.execute("INSERT INTO TB_GZQ_FILES(ID,GZQID,TB_FILE_ID,SMALL_TB_FILE_ID,FILETYPE,CREATE_TIME,UPDATE_TIME)VALUES(?,?,?,?,1,SYSDATE,SYSDATE)",
                        fileid,id,fileId,"");
            }
        }
    }

    @Override
    public void delete(Map<String, String> param, UserEntity u) {
        String id = param.get("id");
        if(id!=null){
                DBHelper.execute("delete  from TB_GZQ  WHERE ID = ?", id);
        }
    }

    @Override
    public  Map<String, Object> get(Map<String, String> param,UserEntity u) {
        String id=param.get("id");
        String plid=param.get("plid");
       Map<String,Object>map=DBHelper.queryForMap("select  t.id,t.account,t.name,t.content,t.remark,\n" +
               "                t.star_num,t.good_num,t.publish_platform,t.address,t.ss_id,\n" +
               "    to_char(t.create_time,'YYYY-MM-DD HH24:MI:SS')create_time, to_char(t.update_time,'YYYY-MM-DD HH24:MI:SS')update_time,TU.AVATAR \n" +
               "                from TB_GZQ t  LEFT JOIN  TB_USER TU ON TU.SS_ID=T.SS_ID\n" +
               "                where  t.id=?",id);

        List<Map<String,Object>>nlist=DBHelper.queryForList("select c.id,\n" +
                "       c.comment_conent  as nr,\n" +
                "       c.COMMENT_TO_ACCOUNT as plzid,\n" +
                "       c.comments_status as plzt,t.name\n" +
                "  from TB_COMMENT c\n" +
                "  left join tb_user t on t.ss_id=c.comment_account where c.businessid=? and c.DATA_STATUS <> 1  order  by c.create_time desc\n",id);
        List<Map<String,Object>>dzlist=DBHelper.queryForList("select  GOOD_TO_ACCOUNT as DZZID,GOOD_STATUS  from tb_good  where BUSINESSID=?",id);
        List<Map<String,Object>>zlist=DBHelper.queryForList("select  TB_FILE_ID  from tb_gzq_files  where GZQID=?",id);
        if(zlist!=null){
            map.put("FILELIST",zlist);
        }
        if (nlist!=null){
            map.put("PLLIST",nlist);
        }
        if(dzlist!=null){
            map.put("DZLIST",dzlist);
        }

        return map;
    }

    @Override
    public void savepl(Map<String, String> param, UserEntity u) {
        String xxid=param.get("gzqid");
//         String  ss_id=DBHelper.queryForScalar("select (select s.id from ss_user s where s.username=t.account )as id  from TB_GZQ t where t.id=? ",String.class,xxid);
        String ss_id=param.get("ss_id");
        String plzt=param.get("plzt");
        String nr=param.get("nr");
        String id=DBHelper.queryForScalar("select SEQ_TB_COMMENT.NEXTVAL from  DUAL",String.class);

        DBHelper.execute("insert into TB_COMMENT(ID,BUSINESSID,BUSINESSTYPE,COMMENT_CONENT,COMMENT_ACCOUNT,COMMENT_TO_ACCOUNT,COMMENTS_STATUS,DATA_STATUS,CREATE_TIME,UPDATE_TIME)" +
                     "VALUES(?,?,?,?,?,?,?,0,SYSDATE,SYSDATE)",id,xxid,2,nr,u.getUser_id(),ss_id,plzt);



    }

    @Override
    public void delpl(Map<String, String> param, UserEntity u) {
        String id=param.get("plid");
        if(id!=null){
            DBHelper.execute("update  TB_COMMENT  set  DATA_STATUS= 1  WHERE id = ?", id);
        }
    }

    @Override
    public void savedz(Map<String, String> param, UserEntity u) {
        String xxid=param.get("gzqid");
        String dzid=param.get("dzid");
        String dzzt=param.get("dzzt");
        String xlid=DBHelper.queryForScalar("SELECT SEQ_GOOD.NEXTVAL FROM DUAL",String.class);
        String id=DBHelper.queryForScalar("select id from TB_GOOD where BUSINESSID=? and GOOD_ACCOUNT=?",String.class,xxid,dzid);
        if(id!=null){
            DBHelper.execute("delete  TB_GOOD where BUSINESSID=? and GOOD_TO_ACCOUNT=?",xxid,dzid);
        }else {
            DBHelper.execute("INSERT INTO TB_GOOD(ID,BUSINESSID,BUSINESSTYPE,GOOD_ACCOUNT,GOOD_TO_ACCOUNT,GOOD_STATUS,CREATE_TIME,UPDATE_TIME)VALUES(?,?,?,?,?,?,SYSDATE,SYSDATE)",xlid,xxid,2,u.getUser_id(),dzid,dzzt);
        }

    }


}
