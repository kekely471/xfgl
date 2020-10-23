package com.tonbu.web.admin.service.important.impl;

import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.support.helper.StringHelper;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.common.UserUtils;
import com.tonbu.web.admin.service.important.ImportantService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportantServiceImpl implements ImportantService {

    Logger logger = LogManager.getLogger(ImportantServiceImpl.class.getName());


    /**
     * 列表
     */
    public Page<?> findImportantPage(Map<String, String> param) {
        UserEntity userEntity = CustomRealm.GetLoginUser();
        String status = param.get("status"); //状态值
        //String userId = param.get("userId");
        //搜索人员
        String create_user = param.get("key_name");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append("select t.ID as id,t.CREATE_USER as userId ,a.USERNAME as userName ,t.IMPORTANT_TYPE as importantType,t.IMPORTANT_CONTENT as importantContent,t.AUDIT_USER as auditUserId,b.USERNAME as auditUserName,t.AUDIT_OPINION as auditOpinion, ")
            .append("t.REIGN_NUMBER as reignNumber,t.LEAVE_NUMBER as leaveNumber,t.GO_HOME_NUMBER as goHomeNumber,t.STATUS as status, ")
            .append(" TO_CHAR(t.CREATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  createTime, ")
            .append(" TO_CHAR(t.AUDIT_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  auditTime ,")
            .append(" zd.Name as typeName ," )
            .append(" case when t.STATUS=1 then '保存' when t.STATUS =2 then '待审核' when t.STATUS=3 then '已审核'")
            .append(" when t.STATUS= 4 THEN '已退回' else '已归档' end STATUSNAME ")
            .append(" from TB_IMPORTANT t ")
            .append(" left join SS_USER a on a.ID = t.CREATE_USER ")
            .append(" left join SS_USER b on b.ID = t.AUDIT_USER ")
            .append(" left join BUSINESS_DICT zd on zd.ID= t.IMPORTANT_TYPE ");
        //排除逻辑删除
        sql.append(" where 1=1 AND t.STATUS != " + Constants.ACCOUNT_DEL);
        sql.append(" and t.CREATE_USER = ?");
        args.add(userEntity.getUser_id());

        /*  //增加手机端的列表数据
        if (StringUtils.isNotBlank(userId)){
            args.add(userId);
        }else{
            args.add(userEntity.getUser_id());
        }*/
        if (StringUtils.isNotBlank(create_user)){
            sql.append(" and a.USERNAME like ?");
            args.add("%" + create_user + "%");
        }
        if (StringUtils.isNotBlank(status)) {
            sql.append(" and t.STATUS = ?");
            args.add(status);
           // args.add("%" + status + "%");
        }
        sql.append(" order by t.CREATE_TIME desc");
        Limit limit = new Limit(param);
        Page<?> result = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());

        return result;
    }

    /**
     * 保存或者更新要事登记
     * @param param
     * @return
     */
    @Override
    @Transactional(value = "primaryTransactionManager")
    public int saveOrUpdate(Map<String, String> param) {
        Integer create_user = null;
        String id = param.get("ID");
        Integer reuslt = null;
        //手机端保存保存数据
        String userId = param.get("userId");
        if (StringUtils.isNotBlank(userId)){
            Map<String ,Object> user  = UserUtils.getUserByToken(userId);
            create_user =Integer.parseInt(user.get("SS_ID").toString());

        }else{//PC端保存数据
            UserEntity userEntity = CustomRealm.GetLoginUser();
            //当前登录人ID
            String user_id = userEntity.getUser_id();
            create_user = Integer.parseInt(user_id);//提交人ID
        }

        Integer important_type = Integer.parseInt(param.get("IMPORTANT_TYPE"));//要事类型
        String important_content = param.get("IMPORTANT_CONTENT");//要事内容
        Integer reign_number = Integer.parseInt(param.get("REIGN_NUMBER"));//在位人数
        Integer leave_number = Integer.parseInt(param.get("LEAVE_NUMBER"));//请假人数
        Integer go_home_number = Integer.parseInt(param.get("GO_HOME_NUMBER"));//探亲回家人数
        String status = param.get("STATUS");
     //   Integer audit_user = Integer.parseInt(param.get("AUDIT_USER"));//审核人userid
        StringBuilder sql = new StringBuilder();

        //新增
        if (StringUtils.isBlank(id)){
            id = StringHelper.getRandomUUID();
            sql.append("insert into TB_IMPORTANT (id,create_user,important_type,important_content,reign_number,leave_number,go_home_number,create_time,status ) ");
            sql.append("values (");
            sql.append("?,?,?,?,?,?,?,SYSDATE,?");//保存状态
            sql.append(")");
            reuslt = DBHelper.execute(sql.toString(),id,create_user,important_type,important_content,reign_number,leave_number,go_home_number,status);

        }else{//修改
            sql.append("update TB_IMPORTANT set ");
            Map<String,Object> args = new HashMap<>();
            if (important_type != null){
                sql.append("important_type = :important_type ,");
                args.put("important_type",important_type);
            }
            if (StringUtils.isNotBlank(important_content)){
                sql.append("important_content = :important_content,");
                args.put("important_content",important_content);
            }
            if (reign_number !=null){
                sql.append("reign_number = :reign_number,");
                args.put("reign_number",reign_number);
            }
            if (leave_number != null ){
                sql.append("leave_number =:leave_number ,");
                args.put("leave_number",leave_number);
            }
            if (StringUtils.isNotBlank(status)){
                sql.append("STATUS =:STATUS ,");
                args.put("STATUS",status);
            }
            if (go_home_number != null){
                sql.append("go_home_number =:go_home_number ");
                args.put("go_home_number",go_home_number);
            }
            sql.append(" where id = :id");
            args.put("id",id);
            reuslt =  DBHelper.update(sql.toString(),args);
        }

        return reuslt;
    }

    @Override
    public void del(Map<String, String> param) {
        String ids = param.get("ids");
        String[] id_s = ids.split("\\,");
        StringBuffer querySql = new StringBuffer();
        querySql.append("select t.status  from TB_IMPORTANT t where t.ID = ?");
        for (int i = 0; i < id_s.length; i++) {
            Map<String,Object> result =DBHelper.queryForMap(querySql.toString(),id_s[i]);
            String status = result.get("STATUS").toString();
            if ("1".equals(status)){
                //只有保存状态下可以删除
                DBHelper.execute("update TB_IMPORTANT set STATUS = '-1' where ID = ?",id_s[i]);
            }else{
                throw new JSONException("已提交的要事登记不能删除");
            }
        }
    }

    /**
     * 根据职务查找人员
     * @param param
     * @return
     */
    @Override
    public List<Map<String, Object>> findAuditUsers(Map<String, String> param) {
        String zw = param.get("zw");

        StringBuffer sql = new StringBuffer("select t.ID,s.ID as USERID,t.NAME ")
                .append(" from TB_USER t left join SS_USER s on t.SS_ID = s.ID ")
                .append(" where 1= 1");
        if (StringUtils.isNotBlank(zw)){
            sql.append(" and t.job =?");
        }
        return DBHelper.queryForList(sql.toString(),zw);
    }

    @Override
    public Page<?> findImportantHanderPage(Map<String, String> param) {
        UserEntity userEntity = CustomRealm.GetLoginUser();
        String status = param.get("status"); //状态值
        //搜索人员
        String create_user = param.get("key_name");

        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append("select t.ID as id,t.CREATE_USER as userId ,a.USERNAME as userName ,t.IMPORTANT_TYPE as importantType,t.IMPORTANT_CONTENT as importantContent,t.AUDIT_USER as auditUserId,b.USERNAME as auditUserName,t.AUDIT_OPINION as auditOpinion, ")
                .append("t.REIGN_NUMBER as reignNumber,t.LEAVE_NUMBER as leaveNumber,t.GO_HOME_NUMBER as goHomeNumber,t.STATUS as status, ")
                .append(" TO_CHAR(t.CREATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  createTime, ")
                .append(" TO_CHAR(t.AUDIT_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  auditTime ,")
                .append(" zd.Name as typeName ,")
                .append(" case when t.STATUS=1 then '保存' when t.STATUS =2 then '待审核' when t.STATUS=3 then '已审核'")
                .append(" when t.STATUS= 4 THEN '已退回' else '已归档' end STATUSNAME ")
                .append(" from TB_IMPORTANT t ")
                .append(" left join SS_USER a on a.ID = t.CREATE_USER ")
                .append(" left join SS_USER b on b.ID = t.AUDIT_USER ")
                .append(" left join BUSINESS_DICT zd on zd.ID= t.IMPORTANT_TYPE ");
        //排除逻辑删除
        sql.append(" where 1=1 AND t.STATUS != " + Constants.ACCOUNT_DEL);
        sql.append(" and t.AUDIT_USER = ?");
        args.add(userEntity.getUser_id());

        if (StringUtils.isNotBlank(create_user)){
            sql.append(" and a.USERNAME like ?");
            args.add("%" + create_user + "%");
        }

        if (StringUtils.isNotBlank(status)) {
            if ("6".equals(status)){ //已审列表
                sql.append(" and (t.STATUS = ? or t.STATUS = ? or t.STATUS = ?) ");
                args.add(3);
                args.add(4);
                args.add(5);
            }else if ("3".equals(status)){//归档列表和待归档列表
                sql.append(" and (t.STATUS = ? or t.STATUS = ? ) ");
                args.add(3);
                args.add(5);
            }else{
                sql.append(" and (t.STATUS = ?  ) ");
                args.add(2);
            }

        }
        sql.append(" order by t.CREATE_TIME desc");
        if ("3".equals(status)){
            sql.append(" ,t.status asc ");
        }
        Limit limit = new Limit(param);
        Page<?> result = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());

        return result;
    }

    @Override
    @Transactional(value = "primaryTransactionManager")
    public int auditImportant(Map<String, String> param) {
        Integer audit_user = null;
        String id = param.get("ID");
        Integer result = null;
        //手机端保存保存数据
        String userId = param.get("userId");
        if (StringUtils.isNotBlank(userId)){
            Map<String ,Object> user  = UserUtils.getUserByToken(userId);
            audit_user =Integer.parseInt(user.get("SS_ID").toString());

        }else{//PC端保存数据
            UserEntity userEntity = CustomRealm.GetLoginUser();
            //当前登录人ID
            String user_id = userEntity.getUser_id();
            audit_user = Integer.parseInt(user_id);//提交人ID
        }
        String status = param.get("STATUS");
        String audit_opinion = param.get("AUDIT_OPINION");
        if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(status)){
            //保存审核记录
            //id = StringHelper.getRandomUUID();
            StringBuffer saveSql = new StringBuffer("insert into TB_AUDIT_IMPORTANT (")
                    .append(" ID,AUDIT_OPINION,AUDIT_DATE,AUDIT_USERID,TB_IMPORTANT_ID,AUDIT_STATUS")
                    .append(" ) values (")
                    .append(" ?,?,SYSDATE,?,?,?")
                    .append(" )");
            DBHelper.execute(saveSql.toString(),StringHelper.getRandomUUID(),audit_opinion, audit_user,id,status);

            //更新主状态
            StringBuffer updateSql = new StringBuffer("update TB_IMPORTANT set STATUS = ? where ID = ?");
            result = DBHelper.execute(updateSql.toString(),status,id);

        }else {
            throw new JSONException("审核异常");
        }
        return result;
    }

    @Override
    public Page<?> findImportantMobilePage(Map<String, String> param) {

        String userId = param.get("userId");
        String flag = param.get("flag");
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append("select t.ID as id,t.CREATE_USER as userId ,a.USERNAME as userName ,t.IMPORTANT_TYPE as importantType,t.IMPORTANT_CONTENT as importantContent,t.AUDIT_USER as auditUserId,b.USERNAME as auditUserName,t.AUDIT_OPINION as auditOpinion, ")
                .append("t.REIGN_NUMBER as reignNumber,t.LEAVE_NUMBER as leaveNumber,t.GO_HOME_NUMBER as goHomeNumber,t.STATUS as status, ")
                .append(" TO_CHAR(t.CREATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  createTime, ")
                .append(" TO_CHAR(t.AUDIT_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  auditTime ,")
                .append(" zd.Name as typeName ," )
                .append(" c.SJDWMC as SJDWMC ," )
                .append(" case when t.STATUS=1 then '保存' when t.STATUS =2 then '待审核' when t.STATUS=3 then '已审核'")
                .append(" when t.STATUS= 4 THEN '已退回' else '已归档' end STATUSNAME ")
                .append(" from TB_IMPORTANT t ")
                .append(" left join SS_USER a on a.ID = t.CREATE_USER ")
                .append(" left join SS_USER b on b.ID = t.AUDIT_USER ")
                .append(" left join TB_USER c on c.SS_ID = a.ID ")
                .append(" left join BUSINESS_DICT zd on zd.ID= t.IMPORTANT_TYPE ");
        //排除逻辑删除
        sql.append(" where 1=1 AND t.STATUS != " + Constants.ACCOUNT_DEL);
        if ("0".equals(flag)){//普通员工
            sql.append(" and t.CREATE_USER = ?");
            args.add(userId);
        }else {//职位领导
            sql.append(" and t.AUDIT_USER = ?");
            args.add(userId);
            sql.append(" and t.STATUS = ?");
            args.add(2);
        }
        sql.append(" order by t.CREATE_TIME desc");
        Limit limit = new Limit(param);
        Page<?> result = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());

        return result;
    }

    @Override
    public Integer findJobByUserID(Map<String, String> param) {
        Integer result= 0;
        String userId = param.get("userId");
        Map<String ,Object> user  = UserUtils.getUserByToken(userId);
        if (user != null  ){
            if(user.get("job") != null){
                String job = user.get("job").toString();
                //3站长、2指导员
                if("3".equals(job) || "7".equals(job)){
                    result = 1;//站长、教导员
                }else if ("0".equals(job)){
                    result = 2; //队务科
                }else{
                    result = 0;
                }
            }else {
                result = 0;
            }

        }else{
             result = null;
        }


        return result;
    }

    /**
     * 手机端要事列表
     * @param param
     * @return
     */
    @Override
    public Page<?> findImportantPageByStatus(Map<String, String> param) {

        String userId = param.get("userId");
        String status = param.get("status");
        StringBuffer sql = new StringBuffer();
        sql.append("select t.ID as id,t.CREATE_USER as userId ,a.USERNAME as userName ,t.IMPORTANT_TYPE as importantType,t.IMPORTANT_CONTENT as importantContent,t.AUDIT_USER as auditUserId,b.USERNAME as auditUserName,t.AUDIT_OPINION as auditOpinion, ")
                .append("t.REIGN_NUMBER as reignNumber,t.LEAVE_NUMBER as leaveNumber,t.GO_HOME_NUMBER as goHomeNumber,t.STATUS as status, ")
                .append(" TO_CHAR(t.CREATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  createTime, ")
                .append(" TO_CHAR(t.AUDIT_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  auditTime ,")
                .append(" zd.Name as typeName ," )
                .append(" c.SJDWMC as SJDWMC ," )
                .append(" case when t.STATUS=1 then '未提交' when t.STATUS =2 then '已提交' when t.STATUS=3 then '已审核'")
                .append(" when t.STATUS= 4 THEN '已退回' else '已归档' end STATUSNAME ")
                .append(" from TB_IMPORTANT t ")
                .append(" left join SS_USER a on a.ID = t.CREATE_USER ")
                .append(" left join SS_USER b on b.ID = t.AUDIT_USER ")
                .append(" left join TB_USER c on c.SS_ID = a.ID ")
                .append(" left join BUSINESS_DICT zd on zd.ID= t.IMPORTANT_TYPE ");
        //排除逻辑删除
        sql.append(" where 1=1  AND t.STATUS = ? AND t.CREATE_USER = ?  ");
        sql.append(" order by t.CREATE_TIME desc");

        Limit limit = new Limit(param);
        Page<?> result = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), status,userId);

        return result;
    }

    /**
     * 手机端归档列表
     * @param param
     * @return
     */
    @Override
    public Page<?> gDlist(Map<String, String> param) {
        String status = param.get("status");  //2提交（待归档） 5、归档
        StringBuffer sql = new StringBuffer();
        sql.append("select t.ID as id,t.CREATE_USER as userId ,a.USERNAME as userName ,t.IMPORTANT_TYPE as importantType,t.IMPORTANT_CONTENT as importantContent,t.AUDIT_USER as auditUserId,b.USERNAME as auditUserName,t.AUDIT_OPINION as auditOpinion, ")
                .append("t.REIGN_NUMBER as reignNumber,t.LEAVE_NUMBER as leaveNumber,t.GO_HOME_NUMBER as goHomeNumber,t.STATUS as status, ")
                .append(" TO_CHAR(t.CREATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  createTime, ")
                .append(" TO_CHAR(t.AUDIT_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  auditTime ,")
                .append(" zd.Name as typeName ," )
                .append(" c.SJDWMC as SJDWMC ," )
                .append(" case when t.STATUS=1 then '未提交' when t.STATUS =2 then '已提交' when t.STATUS=3 then '已审核'")
                .append(" when t.STATUS= 4 THEN '已退回' else '已归档' end STATUSNAME ")
                .append(" from TB_IMPORTANT t ")
                .append(" left join SS_USER a on a.ID = t.CREATE_USER ")
                .append(" left join SS_USER b on b.ID = t.AUDIT_USER ")
                .append(" left join TB_USER c on c.SS_ID = a.ID ")
                .append(" left join BUSINESS_DICT zd on zd.ID= t.IMPORTANT_TYPE ");

        sql.append(" where 1=1  AND t.STATUS = ?  ");
        sql.append(" order by t.CREATE_TIME desc");
        Limit limit = new Limit(param);
        Page<?> result = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), status);

        return result;
    }

    /**
     * 手机端根据id查询
     * @param param
     * @return
     */
    @Override
    public Map<String, Object> findImportantById(Map<String, String> param) {
        String id = param.get("id");
        StringBuffer sql = new StringBuffer();
        sql.append("select t.ID as id,t.CREATE_USER as userId ,a.USERNAME as userName ,t.IMPORTANT_TYPE as importantType,t.IMPORTANT_CONTENT as importantContent,t.AUDIT_USER as auditUserId,b.USERNAME as auditUserName,t.AUDIT_OPINION as auditOpinion, ")
                .append("t.REIGN_NUMBER as reignNumber,t.LEAVE_NUMBER as leaveNumber,t.GO_HOME_NUMBER as goHomeNumber,t.STATUS as status, ")
                .append(" TO_CHAR(t.CREATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  createTime, ")
                .append(" TO_CHAR(t.AUDIT_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  auditTime ,")
                .append(" zd.Name as typeName " )
                .append(" from TB_IMPORTANT t ")
                .append(" left join SS_USER a on a.ID = t.CREATE_USER ")
                .append(" left join SS_USER b on b.ID = t.AUDIT_USER ")
                .append(" left join BUSINESS_DICT zd on zd.ID= t.IMPORTANT_TYPE ");
        sql.append("where t.id =?");
        return DBHelper.queryForMap(sql.toString(),id);
    }

    /**
     * 手机端审核、归档
     * @param param
     * @return
     */
    @Override
    @Transactional(value = "primaryTransactionManager")
    public Integer  auditImportantById(Map<String, String> param) {
        List<Object> args = new ArrayList<Object>();

        Map<String,Object> user = UserUtils.getUserByToken(param.get("userId"));
        String auditUser = user.get("SS_ID").toString();
        Integer result = 0 ;
        String id = param.get("id");
        String status = param.get("status");
        String auditOpinion = param.get("audit_opinion");
        StringBuffer sql = new StringBuffer();
        sql.append("select * from  TB_IMPORTANT t where t.ID= ? ");
        Map<String,Object> map = DBHelper.queryForMap(sql.toString(),id);
        if (map != null){
            StringBuffer updateSql = new StringBuffer();
            updateSql.append("update TB_IMPORTANT set AUDIT_TIME = SYSDATE, STATUS = ? ");
            args.add(status);  //审核状态
            updateSql.append(",AUDIT_USER = ?");
            args.add(auditUser); //审核人
            if (StringUtils.isNotBlank(auditOpinion)){
                updateSql.append(" ,AUDIT_OPINION = ? ");
                args.add(auditOpinion); //审核意见
            }
            updateSql.append(" where ID = ?");
            args.add(id);
            result = DBHelper.update(updateSql.toString(),args.toArray());
        }
        return result;
    }

    /**
     * 改版pc端查看要事登记
     * @param param
     * @return
     */
    @Override
    public Page<?> findImportantPcPage(Map<String, String> param) {
        StringBuffer sql = new StringBuffer();
        List<Object> args = new ArrayList<Object>();
        String status = param.get("key_status");
        String createName = param.get("key_name");
        sql.append("select t.ID as id,t.CREATE_USER as userId ,a.USERNAME as userName ,t.IMPORTANT_TYPE as importantType,t.IMPORTANT_CONTENT as importantContent,t.AUDIT_USER as auditUserId,b.USERNAME as auditUserName,t.AUDIT_OPINION as auditOpinion, ")
                .append("t.REIGN_NUMBER as reignNumber,t.LEAVE_NUMBER as leaveNumber,t.GO_HOME_NUMBER as goHomeNumber,t.STATUS as status, ")
                .append(" TO_CHAR(t.CREATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  createTime, ")
                .append(" TO_CHAR(t.AUDIT_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  auditTime ,")
                .append(" zd.Name as typeName ," )
                .append(" case when t.STATUS=1 then '待提交' when t.STATUS =2 then '待归档' when t.STATUS=3 then '已审核'")
                .append(" when t.STATUS= 4 THEN '已退回' else '已归档' end STATUSNAME ")
                .append(" from TB_IMPORTANT t ")
                .append(" left join SS_USER a on a.ID = t.CREATE_USER ")
                .append(" left join SS_USER b on b.ID = t.AUDIT_USER ")
                .append(" left join BUSINESS_DICT zd on zd.ID= t.IMPORTANT_TYPE ");
        sql.append("where t.STATUS != " + Constants.ACCOUNT_DEL);
        if (StringUtils.isNotBlank(createName)){
            sql.append(" and a.USERNAME like ?");
            args.add("%" + createName + "%");
        }
        if (StringUtils.isNotBlank(status)){
            sql.append(" and t.STATUS = ?");
            args.add(status);
        }
        sql.append(" order by t.CREATE_TIME desc");
        Limit limit = new Limit(param);
        Page<?> result = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());

        return result;
    }


}
