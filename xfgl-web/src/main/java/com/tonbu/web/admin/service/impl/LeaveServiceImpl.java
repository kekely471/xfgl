package com.tonbu.web.admin.service.impl;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.notification.IosAlert;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.common.HttpUtil;
import com.tonbu.web.admin.common.LeaveTimeUtils;
import com.tonbu.web.admin.pojo.LeaveApply;
import com.tonbu.web.admin.pojo.RunningProcess;
import com.tonbu.web.admin.service.LeaveService;
import com.tonbu.web.app.activiti.JPushUtils;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tonbu.framework.dao.DBHelper.queryForMap;

@Service
public class LeaveServiceImpl implements LeaveService {
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    ProcessEngine processEngine;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    IdentityService identityService;
    @Autowired
    HistoryService historyService;


    @Value("${jg.appKey}")
    private   String appKey;
    @Value("${jg.masterSecret}")
    private   String masterSecret;



    Logger logger = LogManager.getLogger(LeaveServiceImpl.class.getName());


    @Override
    public List<Map<String, Object>> zwlxqList(Map<String, String> param) {
        String dwmc = param.get("id");
        StringBuilder sql = new StringBuilder();
        sql.append("select distinct t.id as BUSINESSID, tu.name,dept.DEPTNAME,case when t.LEAVE_TYPE is not null then '不在位' when t.LEAVE_TYPE is null then '在位' end SFZW,t.LEAVE_TYPE from tb_user tu " +
                " LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC " +
                " left join TB_LEAVEAPPLY t on t.user_id=tu.ss_id " +
                " and to_char(t.start_time,'yyyy-MM-dd')<to_char(sysdate,'yyyy-MM-dd') " +
                " and to_char(t.end_time,'yyyy-MM-dd')>to_char(sysdate,'yyyy-MM-dd') " +
                " and t.COMPLETE=2 "+
                " where tu.dwmc=? " +
                " ORDER BY t.LEAVE_TYPE");
        return DBHelper.queryForList(sql.toString(),dwmc);

    }

    @Override
    public Page<?> loadList(Map<String, String> param) {
        String name = param.get("name");
        String dwmc = param.get("dwmc");
        String leavetype = param.get("leavetype");
        String rq = param.get("datetime");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("select t.id," +
                " t.id as business_id, " +
                " t.process_instance_id as process_id, " +
                " t.user_id , " +
                " u.name, " +
                " to_char(t.start_time, 'yyyy/MM/dd HH24:mi:ss') AS start_time, " +
                " to_char(t.end_time, 'yyyy/MM/dd HH24:mi:ss') AS end_time, " +
                " d.name as leave_type, " +
                " t.reason, " +
                " to_char(t.apply_time, 'yyyy/MM/dd HH24:mi:ss') AS apply_time, " +
                " t.leave_space, " +
                " t.vacation, " +
                " t.complete, " +
                " t.leave_days, " +
                " t.leave_days_true, " +
                " t.leave_days_reward, " +
                " t.report, " +
                " dept.deptname, " +
                "  nvl(to_char(t.report_time, 'yyyy/MM/dd HH24:mi:ss') ,'未销假') AS report_time " +
                " from TB_LEAVEAPPLY t  " +
                " left join tb_user u on u.ss_id = t.user_id  " +
                " left join business_DICT d on  d.value=t.leave_type and d.TYPE_CODE = 'qjlx' AND d.status = 1  " +
                " LEFT JOIN SS_DEPT dept  ON dept.id = u.DWMC " +
                " where  1=1  " );


        if(StringUtils.isNotBlank(name)){
            sql.append(" and u.name like  ? ");
            args.add("%" + name + "%");
        }
        if(StringUtils.isNotBlank(dwmc)){
            sql.append(" and  u.dwmc= ? ");
            args.add("" + dwmc + "");        }

        if(!StringUtils.isBlank(rq)){
            String ks[]=rq.split(" - ");
            sql.append(" and to_char(t.start_time,'yyyy-MM-dd')<? and to_char(t.end_time,'yyyy-MM-dd')>?  ");
            args.add(ks[1]);
            args.add(ks[0]);
        }
        if(StringUtils.isNotBlank(leavetype)){
            sql.append(" and  t.leave_type= ? ");
            args.add("" + leavetype + "");        }


        sql.append(" order by  dept.deptname,t.complete,d.value,t.start_time desc ");

        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());

    }


    @Override
    public Page<?> loadListForApp(Map<String, String> param) {
        String name = param.get("name");
        String dwmc = param.get("dwmc");
        String leavetype = param.get("leavetype");
        String endtime = param.get("endtime");
        String starttime = param.get("starttime");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("select t.id," +
                " t.id as business_id, " +
                " t.process_instance_id as process_id, " +
                " t.user_id , " +
                " u.name, " +
                " to_char(t.start_time, 'yyyy/MM/dd HH24:mi:ss') AS start_time, " +
                " to_char(t.end_time, 'yyyy/MM/dd HH24:mi:ss') AS end_time, " +
                " d.name as leave_type, " +
                " t.reason, " +
                " to_char(t.apply_time, 'yyyy/MM/dd HH24:mi:ss') AS apply_time, " +
                " t.leave_space, " +
                " t.vacation, " +
                " t.complete, " +
                " t.leave_days, " +
                " t.leave_days_true, " +
                " t.leave_days_reward, " +
                " t.report, " +
                " dept.deptname, " +
                "  nvl(to_char(t.report_time, 'yyyy/MM/dd HH24:mi:ss') ,'未销假') AS report_time " +
                " from TB_LEAVEAPPLY t  " +
                " left join tb_user u on u.ss_id = t.user_id  " +
                " left join business_DICT d on  d.value=t.leave_type and d.TYPE_CODE = 'qjlx' AND d.status = 1  " +
                " LEFT JOIN SS_DEPT dept  ON dept.id = u.DWMC " +
                " where  1=1  " );


        if(StringUtils.isNotBlank(name)){
            sql.append(" and u.name like  ? ");
            args.add("%" + name + "%");
        }
        if(StringUtils.isNotBlank(dwmc)){
            sql.append(" and  u.dwmc= ? ");
            args.add("" + dwmc + "");        }

        if(!StringUtils.isBlank(starttime)){
            sql.append(" and to_char(t.start_time,'yyyy-MM-dd')<? and to_char(t.end_time,'yyyy-MM-dd')>?  ");
            args.add(endtime);
            args.add(starttime);
        }
        if(StringUtils.isNotBlank(leavetype)){
            sql.append(" and  t.leave_type= ? ");
            args.add("" + leavetype + "");        }


        sql.append(" order by  dept.deptname,t.complete,d.value,t.start_time desc ");

        Limit limit = new Limit(param);
        return DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());

    }

    @Override
    public void del(Map<String, String> param){
        String ids = param.get("ids");
        String[] id_s = ids.split("\\+");
        for (int i = 0; i < id_s.length; i++) {
            DBHelper.execute("delete  from TB_LEAVEAPPLY   WHERE ID=?",id_s[i]);

            //请假信息
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().
                    processInstanceBusinessKey(id_s[i]).
                    singleResult();

            if(hpi==null){

            }

            String processInstanceId = hpi.getId(); //流程实例ID

            System.out.println("删除流程实例processInstanceId:"+processInstanceId);
            //　　判断该流程实例是否结束，未结束和结束两者删除表的信息是不一样的。

            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
                    .singleResult();

            if (pi == null) {
                //该流程实例已经完成了
                historyService.deleteHistoricProcessInstance(processInstanceId);
            } else {

                //该流程实例未结束的
                runtimeService.suspendProcessInstanceById(processInstanceId);//挂起流程
                runtimeService.deleteProcessInstance(processInstanceId, "");
                historyService.deleteHistoricProcessInstance(processInstanceId);//(顺序不能换)
            }

        }

    }

    @Override
    public List<Map<String, Object>> leaveType() {
        return DBHelper.queryForList(" SELECT value,name FROM business_DICT t WHERE TYPE_CODE = 'qjlx' AND t.status = 1  order by sort ");
    }

    @Override
    public List<Map<String, Object>> business_dict(String type) {
        return DBHelper.queryForList(" SELECT value,name FROM business_DICT t WHERE TYPE_CODE = ? AND t.status = 1  order by sort ",type);
    }

    @Override
    public List<Map<String, Object>> selectLeaveHistory(UserEntity userEntity , String leaveType){
        return DBHelper.queryForList(" select t.id,t.process_instance_id,t.user_id,t.leave_type,t.start_time,t.end_time,t.reason,t.apply_time,t.reality_start_time,t.reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,t.leave_days_true,nvl(t.leave_days_reward,0) as leave_days_reward " +
                " from TB_LEAVEAPPLY t where t.user_id=? and t.leave_type=? and to_char(t.apply_time,'yyyy')=to_char(sysdate,'yyyy') and complete=2",userEntity.getUser_id(),leaveType);

    }

    @Override
    public List<Map<String, Object>> selectLeaveHistoryForleaveTpyeOne(UserEntity userEntity , String leaveType){//请假类型为 1 "事假" 时，仅需要计算超过一天的数据
        return DBHelper.queryForList(" select t.id,t.process_instance_id,t.user_id,t.leave_type,t.start_time,t.end_time,t.reason,t.apply_time,t.reality_start_time,t.reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,t.leave_days_true,nvl(t.leave_days_reward,0) as leave_days_reward " +
                " from TB_LEAVEAPPLY t where t.user_id=? and t.leave_type=? and to_char(t.apply_time,'yyyy')=to_char(sysdate,'yyyy') and complete=2 and t.leave_days_true<>1",userEntity.getUser_id(),leaveType);

    }
    @Override
    public String  startWorkflow(LeaveApply apply, Map<String, Object> variables) {

        String instanceid="";
        String ins="";
        apply.setApply_time(new Date().toString());
        //退回后修改录入
        if (!"0".equals(trimToEmpty(variables.get("returnStr")))) {

            DBHelper.execute("delete  from  TB_LEAVEAPPLY   WHERE ID=?",apply.getId());
            //删除流程实例
            deletecomplete(trimToEmpty(apply.getId()));
            //生成业务表主键
            String id = DBHelper.queryForScalar("select SEQ_LEAVE_ID.nextval from dual ",String.class);
            apply.setId(Integer.parseInt(id));
            //保存
            DBHelper.execute("insert  into TB_LEAVEAPPLY(ID,PROCESS_INSTANCE_ID,USER_ID,START_TIME,END_TIME,SPR," +
                            "LEAVE_TYPE,REASON,APPLY_TIME,LEAVE_SPACE,LEAVE_DAYS,COMPLETE,LEAVE_DAYS_TRUE)VALUES(" +
                            " ?,'',?,to_date(?,'yyyy-mm-dd hh24:mi:ss'),to_date(?,'yyyy-mm-dd hh24:mi:ss'),?" +
                            " ,?,?,sysdate,?,?,?,?)",
                    apply.getId(),apply.getUser_id(),apply.getStart_time(),apply.getEnd_time(),apply.getSpr().get(0),
                    apply.getLeave_type(),apply.getReason(),apply.getLeave_space(),apply.getLeave_days(),apply.getComplete(),apply.getLeave_days_true());
            String businesskey=id;//使用leaveapply表的主键作为businesskey,连接业务数据和流程数据
            identityService.setAuthenticatedUserId(apply.getUser_id());
            ProcessInstance instance=runtimeService.startProcessInstanceByKey("leave",businesskey,variables);
            System.out.println(businesskey);
            instanceid=instance.getId();
            apply.setProcess_instance_id(instanceid);
            //修改业务表中的流程主键
            DBHelper.execute("UPDATE TB_LEAVEAPPLY SET PROCESS_INSTANCE_ID=? WHERE ID=?",instanceid,id);
            //送审
            if("1".equals(apply.getComplete())){
                Task task = taskService.createTaskQuery().processInstanceId(instanceid).singleResult();
                taskService.complete(task.getId(), variables);
                //极光 审批
                messageOfsp(apply.getSpr(),apply.getUser_id(),apply.getStart_time(),apply.getEnd_time());



            }
            return instanceid;
        }




        //第一次
        if ("0".equals(trimToEmpty(apply.getId()))){
            //生成业务表主键
            String id = DBHelper.queryForScalar("select SEQ_LEAVE_ID.nextval from dual ",String.class);
            apply.setId(Integer.parseInt(id));
            //保存
            DBHelper.execute("insert  into TB_LEAVEAPPLY(ID,PROCESS_INSTANCE_ID,USER_ID,START_TIME,END_TIME,SPR," +
                            "LEAVE_TYPE,REASON,APPLY_TIME,LEAVE_SPACE,LEAVE_DAYS,COMPLETE,LEAVE_DAYS_TRUE)VALUES(" +
                            " ?,'',?,to_date(?,'yyyy-mm-dd hh24:mi:ss'),to_date(?,'yyyy-mm-dd hh24:mi:ss'),?" +
                            " ,?,?,sysdate,?,?,?,?)",
                    apply.getId(),apply.getUser_id(),apply.getStart_time(),apply.getEnd_time(),apply.getSpr().get(0),
                    apply.getLeave_type(),apply.getReason(),apply.getLeave_space(),apply.getLeave_days(),apply.getComplete(),apply.getLeave_days_true());
            String businesskey=id;//使用leaveapply表的主键作为businesskey,连接业务数据和流程数据
            identityService.setAuthenticatedUserId(apply.getUser_id());
            ProcessInstance instance=runtimeService.startProcessInstanceByKey("leave",businesskey,variables);
            System.out.println(businesskey);
            instanceid=instance.getId();
            apply.setProcess_instance_id(instanceid);
            //修改业务表中的流程主键
            DBHelper.execute("UPDATE TB_LEAVEAPPLY SET PROCESS_INSTANCE_ID=? WHERE ID=?",instanceid,id);
            //送审
            if("1".equals(apply.getComplete())){
                Task task = taskService.createTaskQuery().processInstanceId(instanceid).singleResult();
                taskService.complete(task.getId(), variables);
                //极光 审批
                messageOfsp(apply.getSpr(),apply.getUser_id(),apply.getStart_time(),apply.getEnd_time());

            }
        }else{
            //送审
            if("1".equals(apply.getComplete())) {
                //保存后送审

                Map<String, Object> leavemap = queryForMap(" select t.id,t.process_instance_id,t.user_id,t.leave_type,t.start_time,t.end_time,t.reason,t.apply_time,t.reality_start_time,t.reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days " +
                        " from TB_LEAVEAPPLY t where t.id=? and to_char(t.apply_time,'yyyy')=to_char(sysdate,'yyyy')",apply.getId());

                apply.setProcess_instance_id(trimToEmpty(leavemap.get("process_instance_id")));
                Task task = taskService.createTaskQuery().processInstanceId(apply.getProcess_instance_id()).singleResult();
                taskService.complete(task.getId(), variables);
                //极光 审批
                messageOfsp(apply.getSpr(),apply.getUser_id(),apply.getStart_time(),apply.getEnd_time());

            }
            //修改业务表中的流程主键
            DBHelper.execute("UPDATE TB_LEAVEAPPLY SET   START_TIME=to_date(?,'yyyy-mm-dd hh24:mi:ss')  , END_TIME=to_date(?,'yyyy-mm-dd hh24:mi:ss') , SPR=? , LEAVE_TYPE=?  , REASON=?  , APPLY_TIME=sysdate  , LEAVE_SPACE=?  , LEAVE_DAYS=?  , COMPLETE=? , LEAVE_DAYS_TRUE=? " +
                            "WHERE ID=?"
                    , apply.getStart_time(), apply.getEnd_time(), apply.getSpr().get(0), apply.getLeave_type(), apply.getReason(), apply.getLeave_space(), apply.getLeave_days(), apply.getComplete(),apply.getLeave_days_true(),
                    apply.getId());
        }




        return instanceid;
    }


    public void deletecomplete(String businessid) {


        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().
                processInstanceBusinessKey(businessid).
                singleResult();
        if(hpi==null){
            return;
        }

        String processInstanceId = hpi.getId(); //流程实例ID

        System.out.println("processInstanceId"+processInstanceId);
        //　　判断该流程实例是否结束，未结束和结束两者删除表的信息是不一样的。
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
                .singleResult();

        if (pi == null) {
            //该流程实例已经完成了
            historyService.deleteHistoricProcessInstance(processInstanceId);
        } else {

            //该流程实例未结束的
            runtimeService.suspendProcessInstanceById(processInstanceId);//挂起流程
            runtimeService.deleteProcessInstance(processInstanceId, "");
            historyService.deleteHistoricProcessInstance(processInstanceId);//(顺序不能换)
        }


    }
    @Override
    public List<Map<String,Object>>  getPageDeptTask(String userid,int firstrow,int rowcount){
        List<Map<String,Object>> results = new ArrayList<>();


        List<Task> tasks=taskService.createTaskQuery().taskCandidateOrAssigned(userid).listPage(firstrow, rowcount);
        for(Task task:tasks){
            String instanceid=task.getProcessInstanceId();
            ProcessInstance ins=runtimeService.createProcessInstanceQuery().processInstanceId(instanceid).singleResult();
            String businesskey=ins.getBusinessKey();

            Map<String,Object>  a = queryForMap("select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE ,tu.NATIVE_PLACE,tu.ss_id " +
                    " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC  where t.id=? and d.type_code='qjlx' and to_char(t.apply_time,'yyyy')=to_char(sysdate,'yyyy')  ",businesskey);


            if (!trimToEmpty(a.get("ss_id")).equals(userid)){
                results.add(a);
            }

        }


        return results;
    }
    @Override
    public List<Map<String,Object>>  getTaskForApp(String userid){
        List<Map<String,Object>> results = new ArrayList<>();
        List<Task> tasks=taskService.createTaskQuery().taskCandidateOrAssigned(userid).list();
        for(Task task:tasks){
            String instanceid=task.getProcessInstanceId();
            ProcessInstance ins=runtimeService.createProcessInstanceQuery().processInstanceId(instanceid).singleResult();
            String businesskey=ins.getBusinessKey();

            Map<String,Object>  a = queryForMap("select t.id, tu.name,d.name as leave_type_name,t.reason,t.leave_days,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE, tu.NATIVE_PLACE,tu.ss_id  " +
                    " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC  where t.id=? and d.type_code='qjlx' and to_char(t.apply_time,'yyyy')=to_char(sysdate,'yyyy')  ",businesskey);
            if (!trimToEmpty(a.get("ss_id")).equals(userid)){
                results.add(a);
            }
        }
        return results;
    }

    @Override
    public int  getPageDeptTaskCount(String userid){

        List<Map<String,Object>> results = new ArrayList<>();


        List<Task> tasks=taskService.createTaskQuery().taskCandidateOrAssigned(userid).list();
        for(Task task:tasks){
            String instanceid=task.getProcessInstanceId();
            ProcessInstance ins=runtimeService.createProcessInstanceQuery().processInstanceId(instanceid).singleResult();
            String businesskey=ins.getBusinessKey();

            Map<String,Object>  a = queryForMap("select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE ,tu.NATIVE_PLACE,tu.ss_id " +
                    " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC  where t.id=? and d.type_code='qjlx' and to_char(t.apply_time,'yyyy')=to_char(sysdate,'yyyy')  ",businesskey);


            if (!trimToEmpty(a.get("ss_id")).equals(userid)){
                results.add(a);
            }

        }




        return results.size();
    }


    @Override
    public Map<String,Object>  getTaskByBussinessId(String businesskey){

        return    queryForMap(" select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,t.leave_days_true,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,tu.ss_id ,tu.NATIVE_PLACE,tu.rwny,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = tu.POLICERANK) as POLICERANK " +
                " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC  where t.id=? and d.type_code='qjlx'  ",businesskey);

    }
    @Override
    public Map<String,Object>  getTaskByProcessInstanceId(String processInstanceId){

        return    queryForMap(" select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,tu.ss_id ,tu.NATIVE_PLACE,tu.rwny,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = tu.POLICERANK) as POLICERANK " +
                " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC  where t.process_instance_id=? and d.type_code='qjlx'  ",processInstanceId);

    }
    @Override
    public Page<?> getPageLeave(String userid,int firstrow,int rowcount) {
        String sql = "select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,tu.ss_id ,tu.NATIVE_PLACE,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE  " +
                " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC where t.user_id=? and d.type_code='qjlx' and complete<>0 " ;

        return DBHelper.queryForPage(sql, firstrow, rowcount,userid);


    }
    @Override
    public Page<?> getPageReport(String userid,int firstrow,int rowcount) {
        String sql = "select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,tu.ss_id ,tu.NATIVE_PLACE,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,t.REPORT  " +
                " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC where t.user_id=? and d.type_code='qjlx' and complete=2  " ;

        return DBHelper.queryForPage(sql, firstrow, rowcount,userid);


    }
    @Override
    public Page<?> getPageLeaveForH5(String userid,String dwmc,int firstrow,int rowcount,String starttime,String endtime) {

        String args = "";
        StringBuilder sql = new StringBuilder();
        sql.append(" select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,t.leave_days_true,t.leave_days-t.leave_days_true as legal_days,tu.ss_id ,tu.NATIVE_PLACE,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE ,nvl(t.leave_days_reward,0) as leave_days_reward   ");
        sql.append(" from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC where ");
        if (StringUtils.isNotBlank(userid)) {
            sql.append(" t.user_id=? and ");
            sql.append("  d.type_code='qjlx'  and to_char(t.start_time,'yyyy-MM-dd')<=? and to_char(t.end_time,'yyyy-MM-dd')>=?  and complete=2  ");
            args=userid;
            return  DBHelper.queryForPage(sql.toString(), firstrow, rowcount,args,endtime,starttime);
        }
        if (StringUtils.isNotBlank(dwmc)) {
            sql.append(" tu.dwmc=? and ");
            sql.append("  d.type_code='qjlx'  and to_char(t.start_time,'yyyy-MM-dd')<=? and to_char(t.end_time,'yyyy-MM-dd')>=?  and complete=2  ");
            args=dwmc;
            return DBHelper.queryForPage(sql.toString(), firstrow, rowcount,args,endtime,starttime);
        }
        sql.append("  d.type_code='qjlx'  and to_char(t.start_time,'yyyy-MM-dd')<=? and to_char(t.end_time,'yyyy-MM-dd')>=?  and complete=2  ");

        return DBHelper.queryForPage(sql.toString(), firstrow, rowcount,endtime,starttime);

    }

    public Page<?> getPageLeaveForCount_bak(String dwmc,Map<String, Object> usermap,int firstrow,int rowcount,String starttime,String endtime) {

        StringBuilder sql = new StringBuilder();

        sql.append(" select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,t.leave_days_true,t.leave_days-t.leave_days_true as legal_days,tu.ss_id ,tu.NATIVE_PLACE,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,nvl(t.leave_days_reward,0) as leave_days_reward,t.report    ");
        sql.append(" from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC where ");
        if (!"".equals(dwmc)){
            sql.append(" tu.dwmc = ? and ");
            sql.append("  d.type_code='qjlx'  and to_char(t.start_time,'yyyy-MM-dd')<=? and to_char(t.end_time,'yyyy-MM-dd')>=?   ");
            return DBHelper.queryForPage(sql.toString(), firstrow, rowcount,dwmc,endtime,starttime);
        }else{
            sql.append(" tu.dwmc in  ");
            //根据实际权级展示对应部职级数据
            if((trimToEmpty(usermap.get("JOB")).indexOf("站长")>-1)||(trimToEmpty(usermap.get("JOB")).indexOf("指导员")>-1)){
                sql.append("(select t.id  from ss_dept t where t.isunit=1 and t.id=?)");
                sql.append(" and d.type_code='qjlx'  and to_char(t.start_time,'yyyy-MM-dd')<=? and to_char(t.end_time,'yyyy-MM-dd')>=?    ");
                return DBHelper.queryForPage(sql.toString(), firstrow, rowcount,trimToEmpty(usermap.get("DWMC_TRUE")),endtime,starttime);
            }else if((trimToEmpty(usermap.get("JOB")).indexOf("大队长")>-1)||(trimToEmpty(usermap.get("JOB")).indexOf("教导员")>-1)){
                sql.append("(select t.id from ss_dept t where t.isunit=1 and t.parentid=?)");
                sql.append(" and d.type_code='qjlx'  and to_char(t.start_time,'yyyy-MM-dd')<=? and to_char(t.end_time,'yyyy-MM-dd')>=?    ");
                return DBHelper.queryForPage(sql.toString(), firstrow, rowcount,trimToEmpty(usermap.get("DWMC_TRUE")),endtime,starttime);
            }else if((trimToEmpty(usermap.get("JOB")).indexOf("队务科")>-1)||(trimToEmpty(usermap.get("JOB")).indexOf("政治部主任")>-1)){
                sql.append("(select t.id from ss_dept t where t.isunit=1 )" );
                sql.append(" and d.type_code='qjlx'  and to_char(t.start_time,'yyyy-MM-dd')<=? and to_char(t.end_time,'yyyy-MM-dd')>=?    ");
                return DBHelper.queryForPage(sql.toString(), firstrow, rowcount,endtime,starttime);
            }

        }
        return null;

    }


    @Override
    public Page<?> getPageLeaveForCount(String dwmc,Map<String, Object> usermap,int firstrow,int rowcount) {
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append( "select  t.id,t.phone,t.email,t.idcard,t.name,t.avatar, nvl(t.tqxjts,0)as tqxjts, nvl(t.nxjts,0)as nxjts," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'sf'  AND BD.VALUE = t.TQXJTSSFJB)tqxjtssfjb," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'sf'  AND BD.VALUE = t.SFKYLDZS)sfkyldzs,t.acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)job,  nvl((SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK) ,'未填写') POLICERANK," +
                "to_char(CREATETIME,'YYYY-MM-DD HH24:MI:SS')CREATETIME,to_char(UPDATETIME,'YYYY-MM-DD HH24:MI:SS')UPDATETIME,RWNY,SS_ID,d.DEPTNAME as DWMC,DWMC as DWMC_TRUE" +
                "    from   tb_user  t  LEFT JOIN SS_DEPT D  ON D.id = t.DWMC WHERE 1=1");
        if(StringUtils.isNotBlank(dwmc)){
            sql.append(" and  t.dwmc= ?   order by  UPDATETIME desc ");
            args.add("" + dwmc + "");
        }else{
            sql.append(" and t.dwmc in  ");
            //根据实际权级展示对应部职级数据
            if((trimToEmpty(usermap.get("JOB")).indexOf("站长")>-1)||(trimToEmpty(usermap.get("JOB")).indexOf("指导员")>-1)){
                sql.append("(select s.id  from ss_dept s where s.isunit=1 and s.id=?)  order by  UPDATETIME desc ");
                return DBHelper.queryForPage(sql.toString(), firstrow, rowcount,trimToEmpty(usermap.get("DWMC_TRUE")));
            }else if((trimToEmpty(usermap.get("JOB")).indexOf("大队长")>-1)||(trimToEmpty(usermap.get("JOB")).indexOf("教导员")>-1)){
                sql.append("(select s.id from ss_dept s where s.isunit=1 and s.parentid=?)  order by  UPDATETIME desc ");
                return DBHelper.queryForPage(sql.toString(), firstrow, rowcount,trimToEmpty(usermap.get("DWMC_TRUE")));
            }else if((trimToEmpty(usermap.get("JOB")).indexOf("队务科")>-1)||(trimToEmpty(usermap.get("JOB")).indexOf("政治部主任")>-1)){
                sql.append("(select s.id from ss_dept s where s.isunit=1 )  order by  UPDATETIME desc "  );
                return DBHelper.queryForPage(sql.toString(), firstrow, rowcount);
            }
        }

        Page<?> p = DBHelper.queryForPage(sql.toString(), firstrow, rowcount, args.toArray());
        return p;
    }

    @Override
    public Page<?> getpageleaveHistoryforH5(String ss_id,int firstrow,int rowcount) {


        StringBuilder sql = new StringBuilder();
        sql.append(" select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,t.leave_days_true,t.leave_days-t.leave_days_true as legal_days,tu.ss_id ,tu.NATIVE_PLACE,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,nvl(t.leave_days_reward,0) as leave_days_reward    ");
        sql.append(" from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC where ");
        sql.append(" d.type_code='qjlx'  and  t.user_id= ? and complete=2  ");
        return DBHelper.queryForPage(sql.toString(), firstrow, rowcount,ss_id);


    }
    @Override
    public String getpageZWLforH5(String dwmc) {

        StringBuilder sql = new StringBuilder();
        sql.append("select count(1) as rum from  (select distinct  tu.name");
        sql.append(" from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC where ");
        sql.append("  tu.dwmc =? ");
        sql.append(" and d.type_code='qjlx'  and to_char(t.start_time,'yyyy-MM-dd')<to_char(sysdate,'yyyy-MM-dd') and to_char(t.end_time,'yyyy-MM-dd')>to_char(sysdate,'yyyy-MM-dd')  and complete=2 ) ");

        return DBHelper.queryForScalar(sql.toString(),String.class,dwmc);

    }

    @Override
    public void updateReport(String businesskey) throws ParseException {


        Map<String,Object> leavemapold = DBHelper.queryForMap("select * from TB_LEAVEAPPLY where id=?",businesskey);

        String START_TIME = leavemapold.get("START_TIME")+"";
        String END_TIME = leavemapold.get("END_TIME")+"";


        //请假天数
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");//注意月份是MM
        Date dateSTART_TIME = simpleDateFormat.parse(START_TIME);
        Date dateEND_TIME = simpleDateFormat.parse(END_TIME);
        Date sysdate = new Date();
        Date dateTRUE_TIME = simpleDateFormat.parse(simpleDateFormat.format(sysdate));

        //销假日期比请假结束时间靠前，则重新计算有效假期
        //实际天数（排除节假日） - 奖励假期天数 （警务科赋值，上一个步骤，最后在查询接口处会做对比0的处理） = 实际天数 （因为上一个步骤给了奖励假期天数，实际天数可能出现小于0）

        if (dateEND_TIME.after(dateTRUE_TIME)){
            //天数计算
            int LEAVE_DAYS = LeaveTimeUtils.getDayDiffer(dateSTART_TIME, dateTRUE_TIME);
            //实际请假天数
            int LEAVE_DAYS_TRUE = LeaveTimeUtils.getDaysByHoliday(dateSTART_TIME, dateTRUE_TIME, LEAVE_DAYS);

            if (LEAVE_DAYS_TRUE<0){
                LEAVE_DAYS_TRUE=0;
            }

            //同时修改请假天数
            DBHelper.execute("UPDATE TB_LEAVEAPPLY SET LEAVE_DAYS_TRUE=?,report=? , report_time=sysdate WHERE ID=?",LEAVE_DAYS_TRUE,1,businesskey);

        }else{
            DBHelper.execute("UPDATE TB_LEAVEAPPLY SET report=? , report_time=sysdate WHERE ID=?",1,businesskey);

        }



        //请假信息
        Map<String,Object> leavemap = getTaskByBussinessId(businesskey);

        Map<String, Object> usermap = getUserInfo(trimToEmpty(leavemap.get("user_id")));


        List<HistoricActivityInstance> his = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(trimToEmpty(leavemap.get("process_instance_id"))).orderByHistoricActivityInstanceStartTime().asc().list();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (HistoricActivityInstance history:his){
            if ("userTask".equals(history.getActivityType())){
                Map<String,Object> map = new HashMap<>();
                Map<String,Object> selectmap=null;
                if (history.getActivityId().indexOf("usertask1")>-1){
                    //不做操作
                    continue;
                }else{
                    //审批人
                    String body =usermap.get("dwmc")+"，"+usermap.get("name")+"的"+leavemap.get("leave_type_name")+"("+leavemap.get("start_time")+"--"+leavemap.get("end_time")+") 共计"+leavemap.get("leave_days")+"天，已休假结束！";
                    messageOfReport(history.getAssignee(),trimToEmpty(leavemap.get("user_id")),body);

                }

            }
        }

    }


    //排除掉保存的
    @Override
    public String getPageLeaveCount(String userid) {
        String sql = "select count(1) as count" +
                " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type where t.user_id=? and d.type_code='qjlx' and complete<>0  " ;

        //complete<>0
        return DBHelper.queryForScalar(sql, String.class,userid);
    }
    @Override
    public Page<?> getSaveLeave(String userid, int firstrow, int rowcount) {
        String sql = "select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,tu.ss_id ,tu.NATIVE_PLACE " +
                " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC where t.user_id=? and d.type_code='qjlx' and complete=0 " ;

        return DBHelper.queryForPage(sql, firstrow, rowcount,userid);


    }
    //排除掉保存的
    @Override
    public String getSaveLeaveCount(String userid) {
        String sql = "select count(1) as count" +
                " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type where t.user_id=? and d.type_code='qjlx' and complete=0  " ;

        //complete<>0
        return DBHelper.queryForScalar(sql, String.class,userid);
    }


    @Override
    public void updateStatus(String businessid, String stuts){
        DBHelper.execute("UPDATE TB_LEAVEAPPLY SET complete=? WHERE ID=?",stuts,businessid);

    }

    @Override
    public void updateReward(String businessid, String leave_days_reward){
        DBHelper.execute("UPDATE TB_LEAVEAPPLY SET leave_days_reward=? WHERE ID=?",leave_days_reward,businessid);

    }

    @Override
    public void deleteTaskByBussinessId(String businessid){
        DBHelper.execute("delete from TB_LEAVEAPPLY  WHERE ID=?",businessid);

    }


    //审批并指定下一级审批人（附加批注）（通过）
    @Override
    public String leaveSp (String process_instance_id, Map<String,Object> variables, String pz, String userid){

        List<Task> tasklist = taskService.createTaskQuery().processInstanceId(process_instance_id).taskAssignee(userid).list();
        tasklist.forEach(task -> {
                Authentication.setAuthenticatedUserId(userid); // 添加批注时候的审核人，通常应该从session获取
                taskService.addComment(task.getId(),process_instance_id,pz+"&&"+variables.get("audit"));//添加批注
                taskService.complete(task.getId(), variables);
                List comments= taskService.getProcessInstanceComments(process_instance_id);
        });


        return process_instance_id;
    }

    @Override
    public List<Map<String,String>>  getHistoryByBussinessId(String instanceid){
        List<Map<String,String>> results = new ArrayList<>();

        List<HistoricActivityInstance> his = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceid).orderByHistoricActivityInstanceStartTime().asc().list();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date();
        String time = ft.format(date);
        for (HistoricActivityInstance history:his){
            if ("userTask".equals(history.getActivityType())){
                Map<String,String> map = new HashMap<>();
                Map<String,Object> selectmap=null;
                if (history.getActivityId().indexOf("usertask1")>-1){
                    //申请人
//                    selectmap = getSprByAssigneeInit(history.getAssignee());
                    continue;
                }else{
                    //审批人
                    selectmap = getSprByAssigneeInit(history.getAssignee());
                    map.put("name",selectmap.get("NAME")+"");
                    map.put("sprId",history.getAssignee());
                    map.put("activitiName",history.getActivityName().split("审核")[0]);

                }
                map.put("startTime",ft.format(history.getStartTime()));
                if (!"".equals(trimToEmpty(history.getEndTime()))){
                    map.put("endTime",ft.format(history.getEndTime()));
                }else{
                    map.put("startTime","");
                    map.put("endTime","未审批");
                    map.put("comment","");
                    map.put("audit","未审批");
                    results.add(map);
                    continue;
                }


                List<Comment>  list = taskService.getProcessInstanceComments(instanceid);
                for(Comment co:list){
                    map.put("comment","");
                    if (trimToEmpty(co.getUserId()).equals(selectmap.get("SS_ID"))){

                        String[] comment =   co.getFullMessage().split("&&");
                        map.put("comment",comment[0]);
                        if ("0".equals(comment[1])){
                            map.put("audit","不同意");
                        }else{
                            map.put("audit","同意");
                        }
                        map.put("comment",comment[0]);

                        break;
                    }
                }
                results.add(map);
            }
        }

        return results;
    }


    public Map<String,Object>  getSprByAssigneeInit(String assignee){

        return     queryForMap("select  t.id,t.phone,t.email,t.idcard,t.name," +
                "t.avatar,t.acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)job," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK) as POLICERANK_NAME,POLICERANK, " +
                "to_char(CREATETIME,'yyyy-mm-dd hh24:mi')CREATETIME,to_char(UPDATETIME,'yyyy-mm-dd hh24:mi')UPDATETIME," +
                "RWNY,SS_ID,SFKYLDZS,TQXJTSSFJB,NXJTS,TQXJTS,NATIVE_PLACE,d.DEPTNAME as DWMC,DWMC as DWMC_TRUE     " +
                "  from   tb_user t  LEFT JOIN SS_DEPT D  ON D.id = t.DWMC   where t.SS_ID=? ", assignee);

    }

    public Map<String,Object>  getSprByAssignee(String assignee){

        return     queryForMap("select  t.id,t.phone,t.email,t.idcard,name," +
                "t.avatar,t.acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)job," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK) as POLICERANK_NAME,POLICERANK, " +
                "to_char(CREATETIME,'yyyy-mm-dd hh24:mi')CREATETIME,to_char(UPDATETIME,'yyyy-mm-dd hh24:mi')UPDATETIME," +
                "RWNY,SS_ID,SFKYLDZS,TQXJTSSFJB,NXJTS,TQXJTS,NATIVE_PLACE,d.DEPTNAME as DWMC,DWMC as DWMC_TRUE      " +
                "  from   tb_user t  LEFT JOIN SS_DEPT D  ON D.id = t.DWMC  where t.id=? ", assignee);

    }


    public Map<String, Object>  getUserInfo(String userId) {
        //用户信息
        Map<String, Object> usermap = queryForMap("select  t.id,t.phone,t.email,t.idcard,t.name," +
                "t.avatar,t.acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)job,job as job_true," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK) as POLICERANK_NAME,POLICERANK, " +
                "to_char(CREATETIME,'yyyy-mm-dd hh24:mi')CREATETIME,to_char(UPDATETIME,'yyyy-mm-dd hh24:mi')UPDATETIME," +
                "RWNY,SS_ID,SFKYLDZS,TQXJTSSFJB,NXJTS,TQXJTS,NATIVE_PLACE,d.DEPTNAME as DWMC,DWMC as DWMC_TRUE    " +
                "  from   tb_user t  LEFT JOIN SS_DEPT D  ON D.id = t.DWMC  where t.SS_ID=? ", userId);
        return usermap;
    }


    public static String trimToEmpty(final Object str) {
        return str == null ? "" : (str+"").trim();
    }

    //极光推送 下级 审批人
    @Override
    public  void messageOfsp(List<String> sprId, String leaveUserSsid, String start_time, String end_time)  {

        sprId.forEach(item->{
            Map<String,Object> sprmap = getUserInfo(item);
            String leaveUserName = DBHelper.queryForScalar("select name from tb_user where ss_id=?",String.class,leaveUserSsid);
            String title ="您有一条请假消息！";
            String body="  "+leaveUserName+"的请假消息，请假开始于"+start_time+",结束于"+end_time+"，需要您及时处理";
/*          messageForIos(trimToEmpty(sprId),title,body);
            messageForAndroid(trimToEmpty(sprId),title,body);*/
            HttpUtil.sendSms(trimToEmpty(sprmap.get("phone")),title+" "+body);
            saveMessage(title,body,trimToEmpty(item),leaveUserSsid);
        });
    }
    //退回 请假人
    @Override
    public  void messageOfth (String leaveUserSsid)  {
        Map<String,Object> map = getUserInfo(leaveUserSsid);
/*        String title ="您有一条审批消息！";
        String body="您的请假申请被退回，需要您及时处理";
        messageForIos(leaveUserSsid,title,body);
        messageForAndroid(leaveUserSsid,title,body);
        saveMessage(title,body,leaveUserSsid,"ROOT");*/
        HttpUtil.sendSms(trimToEmpty(map.get("phone")),"您有一条审批消息!您的请假申请被退回，需要您及时处理。");
        saveMessage("您有一条审批消息","您的请假申请被退回，需要您及时处理。",trimToEmpty(leaveUserSsid),leaveUserSsid);
    }
    //极光推送 请假通过 请假人
    @Override
    public  void messageOfPass (String leaveUserSsid)  {
/*        String title ="您有一条审批消息！";
        String body="您的请假申请已通过，请注意查看";
        messageForIos(leaveUserSsid,title,body);
        messageForAndroid(leaveUserSsid,title,body);
        saveMessage(title,body,leaveUserSsid,"ROOT");*/
    }

    //极光推送 请假通过 请假人
    public  void messageOfReport (String leaveUserSsid,String leaveUserName,String body)  {
/*        String title ="您有一条销假消息！";
        messageForIos(leaveUserSsid,title,body);
        messageForAndroid(leaveUserSsid,title,body);
        saveMessage(title,body,leaveUserSsid,leaveUserName);*/
    }
    //短信 同级 审批人
    @Override
    public  void messageForEvery(List<Map<String, Object>> list, String dwmc, String name, String sprId)  {

        String title ="您有一条请假通知！";
        for (Map<String, Object> map:list){
            if (!sprId.equals(map.get("ID"))){
                String body=dwmc+"的"+name+"的请假消息，通知，不需要处理";
                HttpUtil.sendSms(trimToEmpty(map.get("phone")),title+" "+body);
                System.out.println(trimToEmpty(map.get("phone"))+""+body);
            }
        }
    }
    //短信所有审批人和同级人
    public  void messageForEveryRefuse(List<Map<String, Object>> list,String dwmc,String name,String pz,String sprId)  {
        if (StringUtils.isBlank(pz)){
            pz = "未知";
        }
        String title ="您有一条请假通知！";
        for (Map<String, Object> map:list){
            if (!sprId.equals(map.get("ID"))){
                String body=dwmc+"的"+name+"的请假被退回，原因：“"+pz+"”，此消息为通知，不需要处理";
                HttpUtil.sendSms(trimToEmpty(map.get("phone")),title+" "+body);
                System.out.println(trimToEmpty(map.get("phone"))+""+body);
            }
        }
    }

    public void saveMessage(String title,String body,String leaveUserSsid,String leaveUserName){
        String id = DBHelper.queryForScalar("select SEQ_MESSAGE_ID.nextval from dual ",String.class);
        DBHelper.execute("insert  into TB_MESSAGE(id,title,body,accepter,publisher,createtime)VALUES(" +
                " ?,?,?,?,?,sysdate)",id,title,body,leaveUserSsid,leaveUserName);

    }


    //极光推送，调用Ios
    public  void messageForIos(String ssId,String title,String body)  {
        Map<String,String> pushMap = new HashMap<>();
        pushMap.put("sound","default");
        pushMap.put("badge","1");

        IosAlert alert = IosAlert.newBuilder()
                .setTitleAndBody(title, "", body)
                .setActionLocKey("PLAY")
                .build();

        try{
            //ios （带自定义消息体）
            Message message = JPushUtils.buildMessage(title,body);
            JPushUtils.senPush(JPushUtils.createPushPayloadAllForIOS(title, alert,message,true,pushMap,ssId));
//            JPushUtils.senPush(JPushUtils.createPushPayloadAllForIOS("test3", alert,message,true,pushMap,"635"));
        } catch (Exception e) {
            //不作操作
        } finally{
            //todo
        }

    }

    //极光推送，调用Android
    public  void messageForAndroid(String ssId,String title,String body)  {

        JPushClient jpushClient = new JPushClient(masterSecret, appKey);

        try{
            //安卓（带自定义消息体）

//            Map<String,String> pushMap = new HashMap<>();
//            pushMap.put("sound","default");
//            pushMap.put("badge","1");
//            Message message = JPushUtils.buildMessage("msgtitle！","messagecontent");
//            JPushUtils.senPush(JPushUtils.createPushPayloadAll(title, body,message,true,pushMap,ssId));

            jpushClient.sendAndroidNotificationWithAlias(title,body,new HashMap<>(),ssId);

        } catch (APIConnectionException e) {
            //不作操作
        } catch (APIRequestException e) {
            //不作操作
        }finally{
            jpushClient.close();
        }

    }

    @Override
    public Page<?> getMessage(String userid, int firstrow, int rowcount) {
        String sql = "select t.ID,t.TITLE,t.BODY,(select u.name from TB_USER u where u.ss_id=t.ACCEPTER) as ACCEPTER,CASE t.PUBLISHER WHEN 'ROOT' THEN '系统' ELSE (select u.name from TB_USER u where u.ss_id = t.PUBLISHER)  END AS PUBLISHER,to_char(t.CREATETIME,'yyyy-mm-dd hh24:mi') as CREATETIME"+
                " from TB_MESSAGE t  where t.ACCEPTER=? order by CREATETIME desc " ;

        return DBHelper.queryForPage(sql, firstrow, rowcount,userid);
    }
    //审批人查询
    List<Map<String, Object>> sjlist(String job1, String job2, String dwmc, boolean ifsj) {
        //是否查询上级
        if (ifsj) {
            dwmc = DBHelper.queryForScalar("select dd.parentid from SS_DEPT dd where dd.id=?", String.class, dwmc);
        }
        return DBHelper.queryForList("select  t.ss_id as id,t.phone,t.email,t.idcard,t.name," +
                "t.avatar,t.acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)job," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK) as POLICERANK_NAME,POLICERANK, " +
                "to_char(CREATETIME,'yyyy-mm-dd hh24:mi')CREATETIME,to_char(UPDATETIME,'yyyy-mm-dd hh24:mi')UPDATETIME," +
                "RWNY,SS_ID,SFKYLDZS,NATIVE_PLACE,d.DEPTNAME as DWMC,DWMC as DWMC_TRUE     from   tb_user t   LEFT JOIN SS_DEPT D  ON D.id = t.DWMC   where t.job in (?,?) and dwmc=? and acc_status=1", job1, job2, dwmc);

    }


    @Override
    public  void sendAllSpr(String processid, String pz, String sprId)  {
        List<Map<String, Object>> sjlist = new ArrayList<>();
        List<Map<String,String>> splist = getHistoryByBussinessId(processid);
        for (Map<String,String> processmap:
                splist) {
            Map<String, Object> usermap = getUserInfo(processmap.get("sprId"));

         /*   if ((trimToEmpty(usermap.get("JOB"))).indexOf("班长") > -1) {
                //审批人审批阶段(班长选择)

                sjlist = sjlist("4", "5", trimToEmpty(usermap.get("DWMC_TRUE")), false);

            } else*/
            if (((trimToEmpty(usermap.get("JOB"))).indexOf("站长") > -1)||((trimToEmpty(usermap.get("JOB"))).indexOf("指导员") > -1)) {
                //一天以上不需要选择下一级的审批人

                sjlist = sjlist("3", "7", trimToEmpty(usermap.get("DWMC_TRUE")), false);

            } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("大队长") > -1) || ((trimToEmpty(usermap.get("JOB"))).indexOf("教导员") > -1)) {
                //查询大队长和教导员 的上级 队务科科长

                sjlist = sjlist("1", "2", trimToEmpty(usermap.get("DWMC_TRUE")), false);

            } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("队务科") > -1)) {
                //查询队务科长 的上级 政治部主任

                sjlist = sjlist("0", "未知", trimToEmpty(usermap.get("DWMC_TRUE")), false);


            } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("政治部") > -1)) {
                //结束流程

                sjlist = sjlist("x", "未知", trimToEmpty(usermap.get("DWMC_TRUE")), false);

            }
            //
            Map<String,Object> map = getTaskByProcessInstanceId(processid);

            messageForEveryRefuse(sjlist,map.get("DWMC")+"",map.get("NAME")+"",pz,sprId);
        }

    }



    //销假提醒，回程提醒 消息
    @Override
    @Scheduled(cron = "${leave.message.xiaojia}")
    public void leaveMessageTask(){
        System.out.println("***************销假提醒开始***************");
        String xiaojiasql = "select t.id,t.process_instance_id,t.user_id,tu.name,tu.phone,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,tu.ss_id ,tu.NATIVE_PLACE,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,t.REPORT  " +
                " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC where  d.type_code='qjlx' and complete=2  and report is null and end_time<sysdate" ;
        List<Map<String,Object>> xiaojialist = DBHelper.queryForList(xiaojiasql);

        for(Map<String,Object> map:xiaojialist){
            HttpUtil.sendSms(trimToEmpty(map.get("phone")),"您的请假还未销假，请及时销假！");
            logger.info(trimToEmpty(map.get("phone"))+"您的请假还未销假，请及时销假！");
        }


        System.out.println("***************回程提醒开始***************");
        String huichengsql = "select t.id,t.process_instance_id,t.user_id,tu.name,tu.phone,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,tu.ss_id ,tu.NATIVE_PLACE,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,t.REPORT  " +
                " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC where  d.type_code='qjlx' and complete=2  and report is null and end_time<sysdate and to_number(to_char(t.end_time,'yyyyMMDD'))=to_number(to_char(sysdate,'yyyyMMDD'))+3" ;
        List<Map<String,Object>> huichenglist = DBHelper.queryForList(huichengsql);

        for(Map<String,Object> map:huichenglist){
            HttpUtil.sendSms(trimToEmpty(map.get("phone")),"您的假期即将结束，请及时回程！");
            logger.info(trimToEmpty(map.get("phone"))+"您的假期即将结束，请及时回程！");
        }


    }

    @Override
    public Page<?> loadxfyxjtjList(Map<String, String> param) {
        String key = param.get("key_name");
        String dwmc = param.get("dwmc");
        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append( "select  t.id,t.phone,t.email,t.idcard,t.name,t.avatar,t.tqxjts,t.nxjts," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'sf'  AND BD.VALUE = t.TQXJTSSFJB)tqxjtssfjb," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'sf'  AND BD.VALUE = t.SFKYLDZS)sfkyldzs,t.acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)job, (SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK)POLICERANK," +
                "to_char(CREATETIME,'YYYY-MM-DD HH24:MI:SS')CREATETIME,to_char(UPDATETIME,'YYYY-MM-DD HH24:MI:SS')UPDATETIME,RWNY,SS_ID,d.DEPTNAME as DWMC,DWMC as DWMC_TRUE" +
                "    from   tb_user  t  LEFT JOIN SS_DEPT D  ON D.id = t.DWMC WHERE 1=1"); // 已经删除的记录
        if(StringUtils.isNotBlank(key)){
            sql.append(" and NAME like ? ");
            args.add("%"+key+"%");
        }
        if(StringUtils.isNotBlank(dwmc)){
            sql.append(" and  t.dwmc= ? ");
            args.add("" + dwmc + "");        }
        sql.append(" order by  UPDATETIME desc ");
        Limit limit = new Limit(param);
        Page<?> p = DBHelper.queryForPage(sql.toString(), limit.getStart(), limit.getSize(), args.toArray());
        return p;
    }

    @Override
    public Map<String,Object> countLeave(String userId){

        //用户信息
        Map<String, Object> usermap = getUserInfo(userId);
        int allcount = 0;
        if ("1".equals(trimToEmpty(trimToNum(usermap.get("TQXJTSSFJB"))))) {
            allcount = (trimToNum(usermap.get("NXJTS")) + trimToNum(usermap.get("TQXJTS"))) / 2;
        } else {
            allcount = trimToNum(usermap.get("NXJTS")) + trimToNum(usermap.get("TQXJTS"));
        }
        //剩余
        int surplus = 0;


        BigDecimal b1 = new BigDecimal("0");
        if (usermap.size() > 1) {

            UserEntity  userEntity =  new UserEntity() ;
            userEntity.setUser_id(userId);
            //探亲假
            List<Map<String, Object>> leavehistory2 = selectLeaveHistory(userEntity, "2");
            if (leavehistory2.size() != 0) {
                for (Map<String, Object> leave : leavehistory2) {
                    BigDecimal b3 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                    b1 = b1.add(b3);
                }
            }


            //年假
//                if ((trimToEmpty(usermap.get("POLICERANK_NAME"))).indexOf("消防长")>-1) {
            List<Map<String, Object>> leavehistory3 = selectLeaveHistory(userEntity, "3");
            if (leavehistory3.size() != 0) {
                for (Map<String, Object> leave : leavehistory3) {
                    BigDecimal b3 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                    b1 = b1.add(b3);
                }
            }
//                }


            //事假，一天以上
            List<Map<String, Object>> leavehistory1 = selectLeaveHistory(userEntity, "1");
            //计算总天数
            if (leavehistory1.size() != 0) {
                BigDecimal b2 = new BigDecimal("1");
                for (Map<String, Object> leave : leavehistory1) {


                    BigDecimal b3 = new BigDecimal(trimToNum(leave.get("leave_days_true")));
                    //大于一天,计算入内(减去一天)
                    if (b3.compareTo(b2) > 0) {
                        BigDecimal b4 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                        if (b4.intValue() < 1) {
                            b4 = new BigDecimal("1");
                        }
                        b1 = b1.add(b4).subtract(b2);
                    }
                }
            }


            //计算还剩多少假期（探亲，年假）
            surplus = allcount - b1.intValue();
            if (surplus < 0) {
                surplus = 0;
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("leavehistorycount", "已休" + b1.intValue() + "天探亲年假，剩余" + surplus + "天");
        map.put("username", trimToEmpty(usermap.get("name")));
        map.put("already", b1.intValue());
        map.put("notyet", surplus);
        map.put("count", b1.intValue()+surplus);
        map.put("countpercent", percent(b1.intValue(), b1.intValue()+surplus));

        return map;
    }

    private String percent(float a, float b) {


        if (a==0){
            return "0%";
        }
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);

        return numberFormat.format(a / b * 100)+"%";

    }


    @Override
    public Page<?> loadxfydwtjList(Map<String, String> param) {

        UserEntity userEntity = CustomRealm.GetLoginUser();

        try {

            String dwmc = trimToEmpty(param.get("dwmc"));
            List<Map<String, Object>> dwList = new ArrayList<>();
            //dwmc 为null，全查
            if (StringUtils.isNotBlank(dwmc)) {
                dwList = DBHelper.queryForList("select t.id,t.deptname,deptno from ss_dept t where t.isunit=1 and t.id=?", dwmc);
            } else {
                Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());
                if(!usermap.get("name").equals("系统管理员")){
                    //根据实际权级展示对应部职级数据
                    if ((trimToEmpty(usermap.get("JOB")).indexOf("站长") > -1) || (trimToEmpty(usermap.get("JOB")).indexOf("指导员") > -1)) {
                        dwList = DBHelper.queryForList("select t.id,t.deptname,deptno from ss_dept t where t.isunit=1 and t.id=?", trimToEmpty(usermap.get("DWMC_TRUE")));
                    } else if ((trimToEmpty(usermap.get("JOB")).indexOf("大队长") > -1) || (trimToEmpty(usermap.get("JOB")).indexOf("教导员") > -1)) {
                        dwList = DBHelper.queryForList("select t.id,t.deptname,deptno from ss_dept t where t.isunit=1 and t.parentid=?", trimToEmpty(usermap.get("DWMC_TRUE")));
                    } else if ((trimToEmpty(usermap.get("JOB")).indexOf("队务科") > -1) || (trimToEmpty(usermap.get("JOB")).indexOf("政治部主任") > -1)) {
                        dwList = DBHelper.queryForList("select t.id,t.deptname,deptno from ss_dept t where t.isunit=1 ");
                    }
                }else{
                    dwList = DBHelper.queryForList("select t.id,t.deptname,deptno from ss_dept t where t.isunit=1 ");
                }
            }

            for (int i = 0; i < dwList.size(); i++) {
                //总人数
                String dwcount = DBHelper.queryForScalar("select count(1) from tb_user t  where t.dwmc=?", String.class, dwList.get(i).get("ID"));
                //请假人数
                String bzwcount = getpageZWLforH5(trimToEmpty(dwList.get(i).get("ID")));
                dwList.get(i).put("dwcount", Integer.valueOf(dwcount));
                dwList.get(i).put("bzwcount", Integer.valueOf(bzwcount));
                dwList.get(i).put("zwcount", (Integer.valueOf(dwcount) - Integer.valueOf(bzwcount)) + "");
                BigDecimal dwcountDecimal = new BigDecimal(dwcount);
                BigDecimal zwcountDecimal = new BigDecimal(Integer.valueOf(dwcount) - Integer.valueOf(bzwcount));
                BigDecimal zwl = zwcountDecimal.divide(dwcountDecimal, 2, BigDecimal.ROUND_HALF_UP);
                dwList.get(i).put("zwl", zwl.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());

                String  alreadyMap =   DBHelper.queryForScalar("SELECT count(1) from ( select distinct u.name from tb_leaveapply t left join tb_user u on u.ss_id = t.user_id left join ss_dept s on s.id=u.dwmc where dwmc=? and t.leave_days_true>=2 )",String.class,dwList.get(i).get("ID"));
                dwList.get(i).put("ywccount",Integer.valueOf(alreadyMap));
                //使用运算
                dwList.get(i).put("wwccount",Integer.valueOf(dwcount) -Integer.valueOf(alreadyMap));
                dwList.get(i).put("count",percent(Integer.valueOf(alreadyMap),Integer.valueOf(dwcount)));

            }



            return  new Page( 0,  dwList.size(),  dwList.size(), dwList);
        }catch (Exception e){
            System.out.println(e);
        }
        return new Page<>();

    }


    public static int trimToNum(final Object str) {
        return ((str == null) || str.equals("")) ? 0 : Integer.parseInt((str + "").trim());
    }

}
