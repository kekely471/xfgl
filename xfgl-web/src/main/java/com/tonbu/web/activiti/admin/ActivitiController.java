package com.tonbu.web.activiti.admin;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: xfgl
 * @author: keke
 * @create: 2019-12-19 08:49
 **/
@RequestMapping(value = "/acti")
@Controller
public class ActivitiController {

    @Autowired
    ProcessEngine processEngine;
    @Autowired
    RepositoryService  repositoryService;
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    IdentityService identityservice;
    @ResponseBody
    @RequestMapping("/deployment")
    public void deployment() {
    Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("leave.bpmn")  //添加bpmn资源
                .addClasspathResource("leave.png")
                .name("请假申请单流程")
                .deploy();

        //4.输出部署的一些信息
        System.out.println(deployment.getName());
        System.out.println(deployment.getId());
    }

    @ResponseBody
    @RequestMapping("/runProcesses")

    public void runProcesses() {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("LeaveApply");
        System.out.println("流程定义：" + pi);
        String businesskey=String.valueOf("123321");//使用leaveapply表的主键作为businesskey,连接业务数据和流程数据
        identityservice.setAuthenticatedUserId("罗杨");
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("applyuserid", "罗杨");
        ProcessInstance instance=runtimeService.startProcessInstanceByKey("LeaveApply",businesskey,variables);
        System.out.println(businesskey);
        String instanceid=instance.getId();

    }




}



