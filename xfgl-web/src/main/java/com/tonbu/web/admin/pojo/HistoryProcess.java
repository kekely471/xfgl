package com.tonbu.web.admin.pojo;


import lombok.Data;

import java.util.Map;

@Data
public class HistoryProcess {
	String processDefinitionId;
	String businessKey;
	LeaveApply leaveapply;
	PurchaseApply purchaseapply;
	Map<String,Object> leaveMap;

	
	
}
