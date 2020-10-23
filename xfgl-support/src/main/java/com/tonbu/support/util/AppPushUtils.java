package com.tonbu.support.util;
/*
import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.AppMessage;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.NotificationTemplate;
import com.gexin.rp.sdk.template.style.Style0;
import com.ksoft.core.ConfigReader;
import com.ksoft.core.dao.DBHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;*/

public class AppPushUtils {
	/*protected static Log logger = LogFactory.getLog(AppPushUtils.class);
	
    private static String appId = "DCSXFiH3ug5niNSra5oFG4";
    private static String appKey = "FOhHZsgpqx5i8pUrFItDA5";
    private static String masterSecret = "zGs50yYxapAZ7L4veD5s07";
    
    private static boolean doPush = false;
    static {
    	ConfigReader reader = null;
		try {
			reader = new ConfigReader("config/apppush.properties");
			String isOpen = reader.get("app.push");
			doPush = "true".equals(isOpen);
			
			appId = StringUtils.isNotBlank(reader.get("appId")) ? reader.get("appId") : appId;
			appKey = StringUtils.isNotBlank(reader.get("appKey")) ? reader.get("appKey") : appKey;
			masterSecret = StringUtils.isNotBlank(reader.get("masterSecret")) ? reader.get("masterSecret") : masterSecret;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			reader = null;
		}
    }
    
    // 对单个用户推送消息  
    public static void pushMsgToSingle(String userId, String title, String content){
    	if (!doPush) {
    		return;
    	}
    	pushMsgToSingle(userId, title, content, "");
    }
    
    public static void pushMsgToSingle(String userId, String title, String content, String transmissionContent) {  
    	if (!doPush) {
    		return;
    	}
    	
    	Map<String, Object> map = DBHelper.queryForMap("SELECT cid FROM app_push_cid WHERE user_id = ?", userId);
    	if (map == null || map.isEmpty()) {
    		return;
    	}
    	
    	String cid = map.get("cid").toString();
    	
        IGtPush push = new IGtPush(appKey, masterSecret);  
        NotificationTemplate template = getNotifacationTemplate(title, content, transmissionContent);  
//        TransmissionTemplate template = getTransmissionTemplate(content);  
        SingleMessage message = new SingleMessage();  
        message.setData(template); 
        message.setOffline(true);
        message.setOfflineExpireTime(1000 * 60 * 60 * 2);
        
        Target target = new Target();  
        target.setAppId(appId);  
        target.setClientId(cid);  
        IPushResult result = push.pushMessageToSingle(message, target);  
        *//*
         * 1. 失败：{result=sign_error} 
         * 2. 成功：{result=ok, taskId=OSS-0212_1b7578259b74972b2bba556bb12a9f9a, status=successed_online} 
         * 3. 异常 
         *//*
        logger.info(result.getResponse().toString());
    }  
    
    public static void pushMsgToGroup(List<String> userId, String title, String content){
    	if (!doPush) {
    		return;
    	}
    	for (String uid : userId){
    		if (StringUtils.isBlank(uid)) { continue;}
    		pushMsgToSingle(uid, title, content);
    	}
    }
    
    public static void pushMsgToGroup(String userIds, String title, String content){
    	pushMsgToGroup(userIds.split(","), title, content);
    }
    
    public static void pushMsgToGroup(String[] userId, String title, String content){
    	if (!doPush) {
    		return;
    	}
    	
    	try {
    		for (String uid : userId){
        		if (StringUtils.isBlank(uid)) { continue;}
        		pushMsgToSingle(uid, title, content);
        	}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
    	
    }
    
    *//**
     *	全员推送
     *  @param title
     *  @param content void
     *//*
    public static void pushMsgToAll(String title, String content){
    	if (!doPush) {
    		return;
    	}
    	pushMsgToAll(title, content, "");
    }
    public static void pushMsgToAll(String title, String content, String transmissionContent){
    	if (!doPush) {
    		return;
    	}
    	IGtPush push = new IGtPush(appKey, masterSecret);  
        NotificationTemplate template = getNotifacationTemplate(title, content, transmissionContent); 
        
        List<String> appIds = new ArrayList<String>();
        appIds.add(appId);

        AppMessage message = new AppMessage();
        message.setData(template);
        message.setAppIdList(appIds);
        message.setOffline(true);
        message.setOfflineExpireTime(1000 * 60 * 60 * 12);

        IPushResult ret = push.pushMessageToApp(message);
        logger.info(ret.getResponse().toString());
    }
    
    
    
    public static void main(String[] args) {
    	
//    	pushMsgToAll("通知", "内容", "透");
    	
    	pushMsgToSingle("132c1dd597ec4fbf4a5793a4f4c019b9", "title", "content", "tou");
    	
	}
    
    *//* 设置通知消息模板
     *//*
    private static NotificationTemplate getNotifacationTemplate(String title, String content, String transmissionContent){  
        // 在通知栏显示一条含图标、标题等的通知，用户点击后激活您的应用  
        NotificationTemplate template = new NotificationTemplate();  
        // 设置appid，appkey  
        template.setAppId(appId);  
        template.setAppkey(appKey);  
        // 穿透消息设置为，1 强制启动应用  
        template.setTransmissionType(1);  
        // 设置穿透内容  
        template.setTransmissionContent(transmissionContent);  
        // 设置style  
        Style0 style = new Style0();  
        // 设置通知栏标题和内容  
        style.setTitle(title);  
        style.setText(content);  
        // 设置通知，响铃、震动、可清除  
        style.setRing(true);  
        style.setVibrate(true);  
        style.setClearable(true);  
        // 设置  
        template.setStyle(style);  
          
        return template;  
    } */
    
}