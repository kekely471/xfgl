package com.tonbu.web.admin.pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.activiti.engine.task.Task;

import java.io.Serializable;
import java.util.Date;

@Data
public class LeaveTask implements Serializable{
	//("主键")
	int id;
	//("流程实例id")
	String process_instance_id;
	//("用户名")
	String user_id;
	//("请假起始时间")
	String start_time;
	//("请假结束时间")
	String end_time;
	//("请假类型")
	String leave_type;
	//("请假天数")
	String leave_days;
	//("请假原因")
	String reason;
	//("申请时间")
	String apply_time;
	//("实际请假起始时间")
	String reality_start_time;
	//("实际请假结束时间")
	String reality_end_time;
	//("休假地点")
	String leave_space;
	//("SPR")
	String spr;
	//是否送审
	String complete;
	//任务id")
	String taskid;
	//任务名")
	String taskname;
	//流程实例id")
	String processinstanceid;
	//流程定义id")
	String processdefid;
	//任务创建时间")
	Date taskcreatetime;

	public LeaveTask(int id, String user_id, String start_time, String end_time, String leave_type, String reason, String leave_space, String spr, String complete){
		this.id=id;
		this.user_id=user_id;
		this.start_time=start_time;
		this.end_time=end_time;
		this.leave_type=leave_type;
		this.reason=reason;
		this.leave_space=leave_space;
		this.spr=spr;
		this.complete=complete;
	}
	
}
