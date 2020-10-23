package com.tonbu.web;

import com.alibaba.fastjson.JSONObject;
import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelperEx;
import com.tonbu.framework.util.JsonUtils;
import com.tonbu.support.util.CryptUtils;
import com.tonbu.support.util.EhCacheUtils;
import io.netty.handler.codec.http.HttpUtil;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.activiti.engine.task.Task;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.net.www.http.HttpClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URL;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WebApplicationTests {

    @Autowired
    RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    ProcessEngine processEngine;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    IdentityService identityservice;
    @Autowired
    HistoryService historyService;

    @Test
    public void deployment() {
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("leave.bpmn")  //添加bpmn资源
                .addClasspathResource("leave.png")
                .name("请假流程")
                .deploy();

        //4.输出部署的一些信息
        System.out.println(deployment.getName());
        System.out.println(deployment.getId());
    }


    @Test
    public void runProcesses() {
/*        ProcessInstance pi = runtimeService.startProcessInstanceByKey("lllll");
        System.out.println("流程定义：" + pi);*/
        String businesskey= UUID.randomUUID()+"";//使用leaveapply表的主键作为businesskey,连接业务数据和流程数据
        identityservice.setAuthenticatedUserId("罗杨");
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("applyUserId", "罗杨");
        variables.put("ZdAudit", "张三");
        variables.put("bz", 1);
        variables.put("days", 0);
        variables.put("audit", 1);
        ProcessInstance instance=runtimeService.startProcessInstanceByKey("leave",businesskey,variables);
        Task task = taskService.createTaskQuery().processInstanceId(instance.getId()).singleResult();
        taskService.complete(task.getId(), variables);
        System.out.println(businesskey);
        String instanceid=instance.getId();

    }

    @Test
    public void getpagedepttask() {
        List<Task> tasks=taskService.createTaskQuery().taskCandidateOrAssigned("张三").listPage(0, 20);
//		tasks=taskservice.createTaskQuery().taskCandidateOrAssigned("luoyang").listPage(firstrow,rowcount);
        for(Task task:tasks){
            String instanceid=task.getProcessInstanceId();
            ProcessInstance ins=runtimeService.createProcessInstanceQuery().processInstanceId(instanceid).singleResult();
            String businesskey=ins.getBusinessKey();
            System.out.println("businesskey:"+businesskey);
    }

    }



    @Test
    public void deptcomplete() {
        List<Task> tasks=taskService.createTaskQuery().taskCandidateOrAssigned("张三").listPage(0, 20);

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("days", 2);
        variables.put("ZdAudit", "ligen");
//        variables.put("BzAudit", "jinlei");
//        taskService.claim( tasks.get(0).getId(), "罗杨");
        taskService.complete(tasks.get(0).getId(), variables);

    }

    @Test
    public void deptcomplete2() {
        List<Task> tasks=taskService.createTaskQuery().taskCandidateOrAssigned("ligen").listPage(0, 20);

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("audit", 1);
        variables.put("DdAudit", "zhugong");
        taskService.complete(tasks.get(0).getId(), variables);

    }

    @Test
    public void deptcomplete3() {
        List<Task> tasks=taskService.createTaskQuery().taskCandidateOrAssigned("zhugong").listPage(0, 20);

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("audit", 1);
        variables.put("JwkAudit", "hezong");
        taskService.complete(tasks.get(0).getId(), variables);

    }

    @Test
    public void deptcomplete4() {
        List<Task> tasks=taskService.createTaskQuery().taskCandidateOrAssigned("hezong").listPage(0, 20);

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("audit", 1);
        variables.put("CmcAudit", "hezong");
        taskService.complete(tasks.get(0).getId(), variables);

    }

    @Test
    public void deptcomplete5() {
        List<Task> tasks=taskService.createTaskQuery().taskCandidateOrAssigned("hezong").listPage(0, 20);

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("audit", 1);
        taskService.complete(tasks.get(0).getId(), variables);

    }








    @Test
    public void deletecomplete() {
        int[] businessid = {465,
                467,
                470,
                471,
                472,
                473,
                474,
                475,
                476,
                451,
                450,
                453,
                456,
                457,
                458,
                460,
                466,
                481,
                452,
                455,
                461,
                462,
                464,
                468,
                469







        };
        for (int id:businessid){
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().
                processInstanceBusinessKey(id+"").
                singleResult();
        if(hpi==null){
            continue;
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

    }

    @Test
    public void deleteProcessInstance() {
        String[] businessid = {"106","108","110","119","122","130","200","216","235","236"};
        for (String id:businessid) {

            //该流程实例未结束的
//            runtimeService.suspendProcessInstanceById(id);//挂起流程
//            runtimeService.deleteProcessInstance(id, "");
            historyService.deleteHistoricProcessInstance(id);//(顺序不能换)

        }

    }

    @Test
    public void historicactivityinstance() {

        List<HistoricActivityInstance> his = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId("157501").orderByHistoricActivityInstanceStartTime().asc().list();
        for (HistoricActivityInstance historicActivityInstance:his){
            historicActivityInstance.getActivityId();
            System.out.println(historicActivityInstance.getStartTime());
        }

    }

    @Test
    public void history() {

        HistoricProcessInstanceQuery process = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey("leave").startedBy("").finished();
        int total = (int) process.count();

        List<HistoricProcessInstance> info = process.listPage(0, 10);

        System.out.println(info);
    }

    @Test
    public void delete() {
        List<Task> tasks=taskService.createTaskQuery().taskCandidateOrAssigned("罗杨").listPage(0, 20);
        ;
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("ZdAudit", "ligen");
//        taskService.claim( tasks.get(0).getId(), "");
        taskService.complete(tasks.get(0).getId(), variables);

    }



    @Test
    public void contextLoads() {
        //List<Map<String,Object>> l=DBHelperEx.queryForList("select * from MOCEXM","thirdlyJdbcTemplate");
        //测试blob字段
        String npwd = CryptUtils.md5(Constants.INIT_PASSWORD, "m259p3b386835b1nhf3i429rqwmf6abm");
        System.out.println(npwd.equals("db70dee81946ce14ae50067192ab4dc1"));
    }


    @Test
    public void testStartProcess() {
        System.out.println("Start.........");
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("myProcess", "1");
        System.out.println("流程启动成功，流程id:"+pi.getId());
    }

    @Test
    public void findTasksByUserId() {
        String userId ="dulingjiang";
        List<Task> resultTask = taskService.createTaskQuery().processDefinitionKey("myProcess").taskCandidateOrAssigned(userId).list();
        System.out.println("任务列表："+resultTask);
    }
    public static final String URL = "https://tool.bitefu.net/jiari/";


    @Test
    public  void Get() {

        String date = "";
        int dateint = 20200100;
        for(int i=0;i<30;i++){
            ++dateint;
            date+=dateint+",";
        }


        Map<String, String> params = new HashMap<String, String>();
        params.put("d", date);
        HttpClientResult result = null;
        try {
            result = HttpClientUtils.doGet("https://tool.bitefu.net/jiari/", params);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(result);
    }








    @Test
    public void deletebyprocessid() {
        int[] businessid = {497501,
                495030,
                495042,
                472501,
                475013,
                497614,
                475043,
                475056,
                495014








        };
        for (int id:businessid){


            String processInstanceId = id+""; //流程实例ID

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

    }


}

