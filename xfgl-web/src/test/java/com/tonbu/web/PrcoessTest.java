package com.tonbu.web;

import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.Test;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PrcoessTest {
    /*
     * 获取流程引擎对象，下面的方法会默认查找classpath目录下的名称为activi.cfg.xml
     * 里面bean的id为processEngineConfiguration的ProcessEngineConfiguration引擎配置对象来获取ProcessEngine对象
     */
    private ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
//    // 引擎配置
//    private ProcessEngineConfiguration pec=ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml");
//    // 获取流程引擎对象
//    private ProcessEngine engine=pec.buildProcessEngine();
//


//    @Test
//    public void deployInit() {
//        // 引擎配置
//        ProcessEngineConfiguration pec=ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml");
//        // 获取流程引擎对象
//        ProcessEngine processEngine=pec.buildProcessEngine();
//        //    //部署一个流程
//
//    }
    @Test
    public void deployProcessDefinition() {

        engine = ProcessEngines.getDefaultProcessEngine();
        //根据引擎获取资源service
        RepositoryService repositoryService = engine.getRepositoryService();
        //部署bpmn文件
        String bpmnName = "test_activiti.bpmn";
        InputStream bpmnIn = this.getClass().getClassLoader().getResourceAsStream("test_activiti.bpmn");
        //部署bpmn生成的图片1
        //添加这两个文件进行部署
        Deployment deployment = repositoryService.createDeployment()
                .addInputStream(bpmnName, bpmnIn)
//                .addInputStream(pngName, pngIn)
                .deploy();

        System.out.println("部署id:" + deployment.getId());
        System.out.println("部署的name:" + deployment.getDeploymentTime());

    }

    //查询流程定义
    @Test
    public void queryProcessDefinition() {

        RepositoryService repositoryService = engine.getRepositoryService();
        //创建流程定义查询对象
        ProcessDefinitionQuery definitionQuery = repositoryService.createProcessDefinitionQuery();

        String processDefinitionKey = "myProcess_1";
        //设置流程定义的key的查询条件
        definitionQuery.processDefinitionKey(processDefinitionKey);
        //查询所有的流程定义
        List<ProcessDefinition> processDefinitionList = definitionQuery.list();
        for (ProcessDefinition definition : processDefinitionList) {
            System.out.println("-------------------------");
            System.out.println("流程定义id:" + definition.getId());
            System.out.println("流程资源名：" + definition.getResourceName());
            System.out.println("流程部署id:" + definition.getDeploymentId());
        }


    }

    //启动一个流程实例
    @Test
    public void startProcessInstance() {

        RuntimeService runtimeService = engine.getRuntimeService();

        String processDefinitionKey = "test_activiti";
        //根据流程定义的key启动一个流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey);
        System.out.println("流程实例id:" + processInstance.getId());
        System.out.println("流程定义id:" + processInstance.getProcessDefinitionId());
    }

    //查询当前用户的代办任务
    @Test
    public void queryProcessInstance() {

        //查询任务使用的service
        TaskService taskService = engine.getTaskService();
        //获取任务查询对象
        TaskQuery taskQuery = taskService.createTaskQuery();
        taskQuery.taskAssignee("lisi");
        //查询该条件下的所有的任务
        List<Task> tasks = taskQuery.list();
        for (Task task : tasks) {
            System.out.println("当前任务id:" + task.getId());
            System.out.println("当前任务所属流程定义id:" + task.getProcessDefinitionId());
            System.out.println("当前任务的key:" + task.getTaskDefinitionKey());
        }

    }

    //完成一个流程
    @Test
    public void completeProcessInstance() {

        //任务的id，后期整合后会通过当前登录人身份查询到该用户的任务，然后获取到该id
        String taskId = "5002";
        TaskService taskService = engine.getTaskService();
        //根据任务id完成该任务
        taskService.complete(taskId);
        System.out.println("完成任务ID：" + taskId);
    }


    public static String trimToEmpty(final Object str) {
        return str == null ? "" : (str+"").trim();
    }
    public static int trimToNum(final Object str) {
        return ((str == null)||str.equals("")) ? 0 : Integer.parseInt ((str+"").trim());
    }

    //天数计算（开始时间-结束时间）
    public static int getDayDiffer(Date startDate, Date endDate) throws ParseException {
        int days=0;
        //判断是否跨年
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat hourFormat = new SimpleDateFormat("HHmm");

        String startYear = yearFormat.format(startDate);
        String endYear = yearFormat.format(endDate);
        String endhour = hourFormat.format(endDate);
        if (startYear.equals(endYear)) {
            /*  使用Calendar跨年的情况会出现问题    */
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            int startDay = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTime(endDate);
            int endDay = calendar.get(Calendar.DAY_OF_YEAR);
            if(trimToEmpty(startDay).equals(trimToEmpty(endDay))){
                if (trimToNum(endhour)>1800){
                    days= 2;
                }else{
                    days= 1;
                }
            }else{

                days= endDay - startDay+1;
                if (trimToNum(endhour)>1800){
                    ++days;
                }

            }

        } else {
            /*  跨年不会出现问题，需要注意不满24小时情况（2016-03-18 11:59:59 和 2016-03-19 00:00:01的话差值为 0）  */
            //  只格式化日期，消除不满24小时影响
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            long startDateTime = dateFormat.parse(dateFormat.format(startDate)).getTime();
            long endDateTime = dateFormat.parse(dateFormat.format(endDate)).getTime();
            days = (int) ((endDateTime - startDateTime) / (1000 * 3600 * 24));
        }

        return days;
    }


    public static int getDayDiffer1(Date startDate, Date endDate) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        long startDateTime = dateFormat.parse(dateFormat.format(startDate)).getTime();
        long endDateTime = dateFormat.parse(dateFormat.format(endDate)).getTime();
        return (int) ((endDateTime - startDateTime) / (1000 * 3600 * 24));
    }

    public static void main(String[] args) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");//注意月份是MM
        boolean f = isOverlap("2019-12-24 00:00","2019-12-24 17:59","2019-12-24 01:00","2019-12-24 16:59");


        System.out.println(f);

    }

    private static boolean isOverlap(String startdate1,String enddate1,String startdate2,String enddate2){
        boolean flag=false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");//注意月份是MM
        Date leftStartDate=null;
        Date leftEndDate=null;
        Date rightStartDate=null;
        Date rightEndDate=null;
        try{
            leftStartDate=format.parse(startdate1);
            leftEndDate=format.parse(enddate1);
            rightStartDate=format.parse(startdate2);
            rightEndDate=format.parse(enddate2);
        }catch(ParseException e){
            return false;
        }

        if((leftEndDate.getTime()<rightStartDate.getTime())||rightEndDate.getTime()<leftStartDate.getTime()){
            flag= false;
        }else{
            flag= true;
        }

        return flag;
    }
}
