package com.tonbu.web.admin.pojo;

import lombok.Data;

import java.util.Map;

//正在运行的流程
@Data
public class RunningProcess {
	String executionid;
	String processInstanceid;
	String businesskey;
	String activityid;
	Map<String, Object> map;
	
	
}
