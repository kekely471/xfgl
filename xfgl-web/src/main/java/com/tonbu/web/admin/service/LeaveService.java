package com.tonbu.web.admin.service;

import com.tonbu.framework.data.UserEntity;
import com.tonbu.web.admin.pojo.LeaveApply;
import com.tonbu.framework.dao.support.Page;

import java.text.ParseException;
import java.util.List;
import java.util.Map;


public interface LeaveService {

    //在位率详情查看
    List<Map<String, Object>> zwlxqList(Map<String, String> param);

    Page<?> loadList(Map<String, String> param);

    Page<?> loadListForApp(Map<String, String> param);

    void del(Map<String, String> param);
    //字典表获取请假类型
    List<Map<String, Object>> leaveType();

    //字典表
    List<Map<String, Object>> business_dict(String type);

    //请假历史查询（个人，状态）
    List<Map<String, Object>> selectLeaveHistory(UserEntity userEntity , String leaveType);

    List<Map<String, Object>> selectLeaveHistoryForleaveTpyeOne(UserEntity userEntity , String leaveType);

    String startWorkflow(LeaveApply apply, Map<String,Object> variables);

    List<Map<String,Object>>  getPageDeptTask(String userid,int firstrow,int rowcount);

    List<Map<String,Object>>  getTaskForApp(String userid);

    int  getPageDeptTaskCount(String userid);

    void  updateStatus(String businesskey,String status);

    Page<?> getPageLeave(String userid,int firstrow,int rowcount);

    Page<?> getPageReport(String userid,int firstrow,int rowcount);

    Page<?> getPageLeaveForH5(String userid,String dwmc,int firstrow,int rowcount,String starttime,String endtime);

    Page<?> getPageLeaveForCount(String dwmc,Map<String, Object> map,int firstrow,int rowcount );

    Page<?> getpageleaveHistoryforH5(String ss_id,int firstrow,int rowcount);

    String getpageZWLforH5(String dwmc);

    Page<?> getSaveLeave(String userid,int firstrow,int rowcount);

    String getPageLeaveCount(String userid);

    String getSaveLeaveCount(String userid);

    Map<String,Object>  getTaskByProcessInstanceId(String processInstanceId);

    Map<String,Object>  getTaskByBussinessId(String businesskey);

    void  deleteTaskByBussinessId(String businesskey);

    String leaveSp (String process_instance_id,Map<String,Object> variables,String pz,String userid);

    List<Map<String,String>> getHistoryByBussinessId(String instanceid);

    void messageOfsp(List<String> sprSsId,String leaveUserSsid,String start_time,String end_time);

    void messageOfth(String leaveUserSsid);

    void messageOfPass(String leaveUserSsid);

    void messageForEvery(List<Map<String, Object>> list,String dwmc,String name,String sprId);

    Page<?> getMessage(String userid,int firstrow,int rowcount);

    void  updateReward(String businesskey,String status);

    void updateReport(String businesskey) throws ParseException;

    void leaveMessageTask();

    void sendAllSpr(String processid,String pz,String sprId);

    Page<?> loadxfyxjtjList(Map<String, String> param);

    Page<?> loadxfydwtjList(Map<String, String> param);

    Map<String,Object> countLeave(String userId);





}
