package com.tonbu.web.app.activiti;

import cn.jiguang.common.TimeUnit;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.*;
import cn.jpush.api.push.model.PushPayload.Builder;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import cn.jpush.api.push.model.notification.*;
import cn.jpush.api.schedule.model.SchedulePayload;
import cn.jpush.api.schedule.model.TriggerPayload;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.tonbu.support.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 极光推送自定义公共类
 * 
 * @author CharryLi
 */
public class JPushUtils {

	protected static final Logger LOG = LoggerFactory.getLogger(JPushUtils.class);
//	@Value("${jg.appKey}")
//	private static  String appKey;
//	@Value("${jg.masterSecret}")
//	private static  String masterSecret;
	private static final String appKey = "47691475829fb19ad784d0d0";
	private static final String masterSecret = "c522ade149c824b456c04c3b";
	private static final int maxRetryTimes = 3;


	/**
	 * 发送推送
	 * 
	 * @param payload
	 * @return
	 */
	public static boolean senPush(PushPayload payload) {
		return sendPush(masterSecret, appKey, payload);
	}

	/**
	 * 发送推送
	 * 
	 * @param masterSecret
	 *            APP秘钥
	 * @param appKey
	 *            APP KEY
	 * @param payload
	 * @return
	 */
	public static boolean sendPush(String masterSecret, String appKey, PushPayload payload) {
		return sendPush(masterSecret, appKey, maxRetryTimes, payload);
	}

	/**
	 * 发送推送
	 * 
	 * @param masterSecret
	 *            APP秘钥
	 * @param appKey
	 *            APP KEY
	 * @param maxRetryTimes
	 *            最大重试次数
	 * @param payload
	 *            推送的PushPayload对象
	 * @return 返回true则表示请求得到结果，返回false表示发生异常
	 */
	public static boolean sendPush(String masterSecret, String appKey, int maxRetryTimes, PushPayload payload) {
		JPushClient jpushClient = new JPushClient(masterSecret, appKey, maxRetryTimes);

		try {
			PushResult result = jpushClient.sendPush(payload);
			LOG.info("Got result - " + result);
			return true;
		} catch (APIConnectionException e) {
			LOG.error("Connection error. Should retry later. ", e);
			return false;
		} catch (APIRequestException e) {
			LOG.error("Error response from JPush server. Should review and fix it. ", e);
			LOG.info("HTTP Status: " + e.getStatus());
			LOG.info("Error Code: " + e.getErrorCode());
			LOG.info("Error Message: " + e.getErrorMessage());
			LOG.info("Msg ID: " + e.getMsgId());
			return false;
		}
	}

	/**
	 * 创建一个推送到所有平台，title缺省值为APP名称，内容为ALERT
	 * 
	 * @param alert
	 *            推送的内容
	 * @return
	 */
	public static PushPayload createPushPayloadAll(String alert, boolean isProduction) {
		return createPushPayloadAll(null, alert, isProduction);
	}

	/**
	 * 创建一个推送到所有平台
	 * 
	 * @param title
	 *            推送的标题
	 * @param alert
	 *            推送的内容
	 * @return
	 */
	public static PushPayload createPushPayloadAll(String title, String alert, boolean isProduction) {
		PlatformNotification androidNotification = buildAndroidPlatformNotification(title, alert, null);
		PlatformNotification iosnNotification = buildIosPlatformNotification(alert, null, 0, null);
		return builderPushPayload(Platform.all(), null, null, buildNotification(androidNotification, iosnNotification), null, isProduction);
	}

	/**
	 * 创建一个推送到所有平台别名用户
	 * 
	 * @param title
	 *            推送的标题
	 * @param alert
	 *            推送的内容
	 * @param alias
	 *            用户别名
	 * @return
	 */
	public static PushPayload createPushPayloadAll(String title, String alert, boolean isProduction, String... alias) {
		return createPushPayloadAll(title, alert, isProduction, null, alias);
	}

	/**
	 * 创建一个推送到所有平台别名用户(带参数)
	 * 
	 * @param title
	 *            推送的标题
	 * @param alert
	 *            推送的内容
	 * @param extras
	 *            参数集合
	 * @param alias
	 *            用户别名
	 * @return
	 */
	public static PushPayload createPushPayloadAll(String title, String alert, boolean isProduction, Map<String, String> extras, String... alias) {
		Audience audience = Audience.alias(alias);
		PlatformNotification androidNotification = buildAndroidPlatformNotification(title, alert, extras);
		PlatformNotification iosnNotification = buildIosPlatformNotification(alert, null, 0, extras);
		Notification notification = buildNotification(androidNotification, iosnNotification);
		return builderPushPayload(Platform.all(), audience, null, notification, null, isProduction);
	}

	/**
	 * @param title
	 *            推送标题
	 * @param alert
	 *            推送内容
	 * @param extras
	 *            参数集合
	 * @param audience
	 *            通知对象
	 * @return
	 */
	public static PushPayload createPushPayloadAll(String title, String alert, boolean isProduction, Map<String, String> extras, Audience audience) {
		PlatformNotification androidNotification = buildAndroidPlatformNotification(title, alert, extras);
		PlatformNotification iosnNotification = buildIosPlatformNotification(alert, null, 0, extras);
		Notification notification = buildNotification(androidNotification, iosnNotification);
		return builderPushPayload(Platform.all(), audience, null, notification, null, isProduction);
	}

	/**
	 * 创建一个推送到所有平台别名用户
	 * 
	 * @param title
	 *            推送的标题
	 * @param alert
	 *            推送的内容
	 * @param aliases
	 *            用户别名
	 * @return
	 */
	public static PushPayload createPushPayloadAll(String title, String alert, Collection<String> aliases, boolean isProduction) {
		return createPushPayloadAll(title, alert, null, aliases, isProduction);
	}

	/**
	 * 创建一个推送到所有平台别名用户(带参数)
	 * 
	 * @param title
	 *            推送的标题
	 * @param alert
	 *            推送的内容
	 * @param extras
	 *            参数集合
	 * @param aliases
	 *            用户别名
	 * @return
	 */
	public static PushPayload createPushPayloadAll(String title, String alert, Map<String, String> extras, Collection<String> aliases, boolean isProduction) {
		Audience audience = Audience.alias(aliases);
		PlatformNotification androidNotification = buildAndroidPlatformNotification(title, alert, extras);
		PlatformNotification iosnNotification = buildIosPlatformNotification(alert, null, 0, extras);
		Notification notification = buildNotification(androidNotification, iosnNotification);
		return builderPushPayload(Platform.all(), audience, null, notification, null, isProduction);
	}

	/**
	 * 创建一个推送到Android平台所有用户的PushPayload对象
	 * 
	 * @param title
	 *            android客户端接收到的通知标题
	 * @param alert
	 *            android客户端接收到的通知内容
	 * @param msgContent
	 *            android客户端接收到的自定义消息内容
	 * @return
	 */
	public static PushPayload createAndroidPushPayload(String title, String alert, String msgContent, boolean isProduction) {
		return builderPushPayload(Platform.android(), null, Message.content(msgContent), buildAndroidNotification(title, alert), null, isProduction);
	}

	/**
	 * 创建一个推送到Android平台别名用户的PushPayload对象
	 * 
	 * @param title
	 *            android客户端接收到的通知标题
	 * @param alert
	 *            android客户端接收到的通知内容
	 * @param msgContent
	 *            android客户端接收到的自定义消息内容
	 * @param alias
	 *            android客户端接收的单个用户别名
	 * @return
	 */
	public static PushPayload createAndroidPushPayload(String title, String alert, String msgContent, boolean isProduction, String... alias) {
		Audience audience = Audience.alias(alias);// 设置别名
		return builderPushPayload(Platform.android(), audience, Message.content(msgContent), buildAndroidNotification(title, alert), null, isProduction);
	}

	/**
	 * 创建一个推送到Android平台别名用户的PushPayload对象(带参数)
	 * 
	 * @param title
	 *            android客户端接收到的通知标题
	 * @param alert
	 *            android客户端接收到的通知内容
	 * @param msgContent
	 *            android客户端接收到的自定义消息内容
	 * @param extras
	 *            android客户端接收的自定义参数
	 * @param alias
	 *            android客户端接收的单个用户别名
	 * @return
	 */
	public static PushPayload createAndroidPushPayload(String title, String alert, String msgContent, boolean isProduction, Map<String, String> extras, String... alias) {
		Audience audience = Audience.alias(alias);// 设置别名
		return builderPushPayload(Platform.android(), audience, Message.content(msgContent), buildAndroidNotification(title, alert, extras), null, isProduction);
	}

	/**
	 * 创建一个推送到Android平台别名用户的PushPayload对象
	 * 
	 * @param title
	 *            android客户端接收到的通知标题
	 * @param alert
	 *            android客户端接收到的通知内容
	 * @param msgContent
	 *            android客户端接收到的自定义消息内容
	 * @param aliases
	 *            android客户端接收的单个用户别名
	 * @return
	 */
	public static PushPayload createAndroidPushPayload(String title, String alert, String msgContent, Collection<String> aliases, boolean isProduction) {
		Audience audience = Audience.alias(aliases);// 设置别名
		return builderPushPayload(Platform.android(), audience, Message.content(msgContent), buildAndroidNotification(title, alert), null, isProduction);
	}

	/**
	 * 创建一个推送到IOS平台所有用户的PushPayload对象
	 * 
	 * @param alert
	 *            IOS客户端接收到的通知内容
	 * @param msgContent
	 *            IOS客户端接收到的自定义消息内容
	 * @return
	 */
	public static PushPayload createIosPushPayload(String alert, String msgContent, boolean isProduction) {
		return builderPushPayload(Platform.ios(), null, Message.content(msgContent), buildIosNotification(alert), null, isProduction);
	}

	/**
	 * 创建一个推送到IOS平台别名用户的PushPayload对象
	 * 
	 * @param alert
	 *            IOS客户端接收到的通知内容
	 * @param msgContent
	 *            IOS客户端接收到的自定义消息内容
	 * @param alias
	 *            接收消息的用户别名
	 * @return
	 */
	public static PushPayload createIosPushPayload(String alert, String msgContent, boolean isProduction, String... alias) {
		Audience audience = Audience.alias(alias);// 设置别名
		return builderPushPayload(Platform.ios(), audience, Message.content(msgContent), buildIosNotification(alert), null, isProduction);
	}

	/**
	 * 创建一个推送到IOS平台别名用户的PushPayload对象(带参数)
	 * 
	 * @param alert
	 *            IOS客户端接收到的通知内容
	 * @param msgContent
	 *            IOS客户端接收到的自定义消息内容
	 * @param extras
	 *            自定义参数列表
	 * @param alias
	 *            接收消息的用户别名
	 * @return
	 */
	public static PushPayload createIosPushPayload(String alert, String msgContent, boolean isProduction, Map<String, String> extras, String... alias) {
		Audience audience = Audience.alias(alias);// 设置别名
		return builderPushPayload(Platform.ios(), audience, Message.content(msgContent), buildIosNotification(alert, null, 0, extras), null, isProduction);
	}

	/**
	 * 创建一个推送到IOS平台别名用户的PushPayload对象
	 * 
	 * @param alert
	 *            IOS客户端接收到的通知内容
	 * @param msgContent
	 *            IOS客户端接收到的自定义消息内容
	 * @param aliases
	 *            接收消息的用户别名
	 * @return
	 */
	public static PushPayload createIosPushPayload(String alert, String msgContent, Collection<String> aliases, boolean isProduction) {
		Audience audience = Audience.alias(aliases);// 设置别名
		return builderPushPayload(Platform.ios(), audience, Message.content(msgContent), buildIosNotification(alert), null, isProduction);
	}

	/**
	 * 创建一个自定义推送对象
	 * 
	 * @param platform
	 *            所需要推送到的平台
	 * @param audience
	 *            所需要推送到的用户别名或tag
	 * @param message
	 *            所需要推送的自定义消息
	 * @param notification
	 *            所需要推送的标题和内容等
	 * @param options
	 *            推送选项
	 * @return
	 */
	public static PushPayload builderPushPayload(Platform platform, Audience audience, Message message, Notification notification, Options options, boolean isProduction) {

		Builder builder = PushPayload.newBuilder();
		// 如果平台参数为空，则默认推送到所有平台
		if (platform != null) {
			builder.setPlatform(platform);
		} else {
			builder.setPlatform(Platform.all());
		}

		// 如果听众参数为空，则默认推送到所有用户
		if (audience != null) {
			builder.setAudience(audience);
		} else {
			builder.setAudience(Audience.all());
		}

		// 如果自定义消息不为空，则设置自定义消息
		if (message != null) {
			builder.setMessage(message);
		}

		// 如果通知不为空则设置
		if (notification != null) {
			builder.setNotification(notification);
		}

		if (options != null) {
			options.setApnsProduction(isProduction);
		} else {
			options = Options.newBuilder().setApnsProduction(isProduction).build();
		}
		builder.setOptions(options);

		return builder.build();
	}

	/**
	 * 创建一个android平台通知对象
	 * 
	 * @param title
	 *            通知的标题
	 * @param alert
	 *            通知的内容
	 * @return
	 */
	public static Notification buildAndroidNotification(String title, String alert) {
		return buildAndroidNotification(title, alert, null);
	}

	/**
	 * 创建一个android通知对象
	 * 
	 * @param title
	 *            通知的标题
	 * @param alert
	 *            通知的内容
	 * @param extras
	 *            通知的自定义参数
	 * @return
	 */
	public static Notification buildAndroidNotification(String title, String alert, Map<String, String> extras) {
		return buildNotification(buildAndroidPlatformNotification(title, alert, extras));
	}

	/**
	 * 创建一个android平台通知对象
	 * 
	 * @param title
	 *            通知的标题
	 * @param alert
	 *            通知的内容
	 * @param extras
	 *            通知的自定义参数
	 * @return
	 */
	public static PlatformNotification buildAndroidPlatformNotification(String title, String alert, Map<String, String> extras) {
		AndroidNotification.Builder androidBuilder = AndroidNotification.newBuilder();
		if (title != null)
			androidBuilder.setTitle(title);
		if (alert != null)
			androidBuilder.setAlert(alert);
		if (extras != null && !extras.isEmpty())
			androidBuilder.addExtras(extras);
		return androidBuilder.build();
	}

	/**
	 * 创建一个IOS平台通知对象
	 *
	 * @param alert
	 * @return
	 */
	public static Notification buildIosNotification(String alert) {
		return buildIosNotification(alert, null, 0, null);
	}

	/**
	 * 创建一个IOS通知对象
	 *
	 * @param alert
	 *            通知内容
	 * @param sound
	 *            通知声音
	 * @param badge
	 *            通知徽章
	 * @param extras
	 *            自定义通知参数
	 * @return
	 */
	public static Notification buildIosNotification(String alert, String sound, int badge, Map<String, String> extras) {
		return buildNotification(buildIosPlatformNotification(alert, sound, badge, extras));
	}

	/**
	 * 创建一个IOS平台通知对象
	 *
	 * @param alert
	 *            通知内容
	 * @param sound
	 *            通知声音
	 * @param badge
	 *            通知徽章
	 * @param extras
	 *            自定义通知参数
	 * @return
	 */
	public static PlatformNotification buildIosPlatformNotification(String alert, String sound, int badge, Map<String, String> extras) {
		IosNotification.Builder iosBuilder = IosNotification.newBuilder();
		if (badge > 0)
			iosBuilder.setBadge(badge);
		else
			iosBuilder.autoBadge();
		if (sound != null)
			iosBuilder.setSound(sound);
		if (alert != null)
			iosBuilder.setAlert(alert);
		if (extras != null && !extras.isEmpty())
			iosBuilder.addExtras(extras);
		return iosBuilder.build();
	}

	/**
	 * 创建一个自定义多平台通知对象
	 *
	 * @param notifications
	 *            自定义的平台通知对象集合
	 * @return
	 */
	public static Notification buildNotification(PlatformNotification... notifications) {
		Notification.Builder builder = Notification.newBuilder();
		for (PlatformNotification platformNotification : notifications) {
			builder.addPlatformNotification(platformNotification);
		}
		return builder.build();
	}

	/**
	 * 创建一个自定义消息对象
	 *
	 * @param msgContent
	 *            自定义消息内容
	 * @return
	 */
	public static Message buildMessage(String msgContent) {
		return buildMessage(null, msgContent);
	}

	/**
	 * 创建一个自定义消息对象
	 *
	 * @param msgTitle
	 *            自定义消息标题
	 * @param msgContent
	 *            自定义消息内容
	 * @return
	 */
	public static Message buildMessage(String msgTitle, String msgContent) {
		return buildMessage(msgTitle, msgContent, null);
	}

	/**
	 * 创建一个自定义消息对象
	 *
	 * @param msgTitle
	 *            自定义消息标题
	 * @param msgContent
	 *            自定义消息内容
	 * @param extras
	 *            自定义消息参数
	 * @return
	 */
	public static Message buildMessage(String msgTitle, String msgContent, Map<String, String> extras) {
		Message.Builder builder = Message.newBuilder();
		if (msgTitle != null)
			builder.setTitle(msgTitle);
		if (msgContent != null)
			builder.setMsgContent(msgContent);
		if (extras != null && !extras.isEmpty())
			builder.addExtras(extras);
		return builder.build();
	}

	/**
	 * 创建一个自定义别名听众对象
	 *
	 * @param alias
	 *            听众用户别名
	 * @return
	 */
	public static Audience bulidAudienceAlias(String... alias) {
		Audience.Builder builder = Audience.newBuilder();
		builder.addAudienceTarget(AudienceTarget.alias(alias));
		return builder.build();
	}

	/**
	 * 创建一个自定义tag听众对象
	 *
	 * @param tags
	 *            听众所属tag
	 * @return
	 */
	public static Audience bulidAudienceTags(String... tags) {
		Audience.Builder builder = Audience.newBuilder();
		builder.addAudienceTarget(AudienceTarget.tag_and(tags));
		return builder.build();
	}

	/**
	 * 创建一个自定义听众对象
	 *
	 * @param aliases
	 *            需要推送到的用户别名集合，参数为null则不设置
	 * @param tags
	 *            需要推送到的用户tag集合， 推送到包含所有tag用户，参数为null则不设置
	 * @return
	 */
	public static Audience bulidAudience(Collection<String> aliases, Collection<String> tags) {
		Audience.Builder builder = Audience.newBuilder();
		if (aliases != null && aliases.size() != 0)
			builder.addAudienceTarget(AudienceTarget.alias(aliases));
		if (tags != null && tags.size() != 0)
			builder.addAudienceTarget(AudienceTarget.tag(tags));
		return builder.build();
	}

	/**
	 * 创建一个自定义听众对象
	 *
	 * @param aliases
	 *            需要推送到的用户别名集合，参数为null则不设置
	 * @param tags
	 *            需要推送到的用户tag交集集合， 只推送到满足该集合中所有tag交集的用户，参数为null则不设置
	 * @return
	 */
	public static Audience bulidAudienceTagsAnd(Collection<String> aliases, Collection<String> tags) {
		Audience.Builder builder = Audience.newBuilder();
		if (aliases != null && aliases.size() != 0)
			builder.addAudienceTarget(AudienceTarget.alias(aliases));
		if (tags != null && tags.size() != 0)
			builder.addAudienceTarget(AudienceTarget.tag_and(tags));
		return builder.build();
	}

	/********************************************* 极光定时任务送 ***************************************************/
	/**
	 * 创建定期任务
	 *
	 * @author qianbao
	 * @param start
	 *            表示定期任务有效起始时间，格式与必须严格为：'YYYY-mm-dd HH:MM:SS‘，且时间为24小时制。
	 * @param end
	 *            表示定期任务的过期时间，格式同上。
	 * @param time
	 *            表示触发定期任务的定期执行时间,，格式严格为：'HH:MM:SS’，且为24小时制。
	 * @param time_unit
	 *            表示定期任务的执行的最小时间单位有：day、week及month3种。大小写不敏感，如day,
	 *            Day,DAy均为合法的time_unit。
	 * @param frequency
	 *            此项与time_unit的乘积共同表示的定期任务的执行周期，如time_unit为day，frequency为2，
	 *            则表示每两天触发一次推送，目前支持的最大值为100。
	 * @param point
	 *            一个列表，此项与time_unit相对应：
	 ***/
	public static TriggerPayload createTriggerPayload(String start, String end, String time, TimeUnit time_unit, int frequency, String[] point) {
		TriggerPayload.Builder builder = TriggerPayload.newBuilder();
		CommonUtils.checkParam(start, end, time);
		builder.setPeriodTime(start, end, time);
		if (time_unit!=null & point!=null && point.length>0) {
			builder.setTimeFrequency(time_unit, frequency, point);
		}
		return builder.buildPeriodical();
	}

	/**
	 * 创建定时任务
	 *
	 * @author qianbao
	 * @param time 为必选项且格式为"YYYY-mm-dd HH:MM:SS“，如"2014-02-15
	 *            13:16:59"，不能为"2014-2-15 13:16:59"或"2014-12-15
	 *            13:16"，即日期时间格式必须完整
	 *
	 ***/
	public static TriggerPayload createTriggerPayload(String time) {
		TriggerPayload.Builder builder = TriggerPayload.newBuilder();
		if (org.apache.commons.lang.StringUtils.isNotBlank(time)) {
			builder.setSingleTime(time);
		}
		return builder.buildSingle();
	}

	/**
	 * 创建SchedulePayload
	 *
	 * @author qianbao
	 * @param name
	 * @param enabled 必须是true
	 * @param trigger 必须是任务
	 * @param push 接收提醒对象
	 *
	 ***/
	public static SchedulePayload createSchedulePayload(String name, Boolean enabled, TriggerPayload trigger, PushPayload push) {
		SchedulePayload.Builder builder = SchedulePayload.newBuilder();
		if (org.apache.commons.lang.StringUtils.isNotBlank(name)) {
			builder.setName(name);
		}
		if (enabled != null) {
			builder.setEnabled(enabled);
		}
		if (trigger != null) {
			builder.setTrigger(trigger);
		}
		if (push != null) {
			builder.setPush(push);
		}
		return builder.build();
	}


	/**
	 * 创建一个推送到所有平台别名用户的PushPayload对象(带参数)
	 *
	 * @param title
	 *            客户端接收到的通知标题
	 * @param alert
	 *            客户端接收到的通知内容
	 * @param msgContent
	 *            客户端接收到的自定义消息内容
	 * @param extras
	 *            客户端接收的自定义参数
	 * @param registrationId
	 *            注册id
	 * @return
	 */
	public static PushPayload createPushPayloadRegisterID(String title, String alert, String msgContent, boolean isProduction, Map<String, String> extras, String... registrationId) {
		Audience audience = Audience.registrationId(registrationId);// 设置别名
		return builderPushPayload(null, audience, Message.content(msgContent), buildAndroidNotification(title, alert, extras), null, isProduction);
	}

	/**
	 * 创建一个推送到所有平台别名用户(带参数)
	 *
	 * @param title
	 *            推送的标题
	 * @param alert
	 * 	 *        推送的内容
	 * @param message
	 * 	          自定义推送消息
	 * @param extras
	 *            参数集合
	 * @param aliases
	 *            用户别名
	 * @return
	 */
	public static PushPayload createPushPayloadAll(String title, String alert,Message message, boolean isProduction,Map<String, String> extras, String... aliases ) {
		Audience audience = Audience.alias(aliases);
		PlatformNotification androidNotification = buildAndroidPlatformNotification(title, alert, extras);
		PlatformNotification iosnNotification = buildIosPlatformNotification(alert, null, 0, extras);
		Notification notification = buildNotification(androidNotification, iosnNotification);
		return builderPushPayload(Platform.all(), audience, message, notification, null, isProduction);
	}


	/**
	 * 创建一个推送到所有平台别名用户(带参数)
	 *
	 * @param title
	 *            推送的标题
	 * @param alert
	 * 	 *        推送的内容
	 * @param message
	 * 	          自定义推送消息
	 * @param extras
	 *            参数集合
	 * @param aliases
	 *            用户别名
	 * @return
	 */
	public static PushPayload createPushPayloadAllForIOS(String title, IosAlert alert,Message message, boolean isProduction,Map<String, String> extras, String... aliases ) {

		Audience audience = Audience.alias(aliases);
		Notification notification = Notification.ios(alert, extras);;
		return builderPushPayload(Platform.all(), audience, message, notification, null, isProduction);
	}
//	https://community.jiguang.cn/article/277321 api
	public static void main(String args[]) throws APIConnectionException, APIRequestException {
		Map<String,String> pushMap = new HashMap<>();
		pushMap.put("sound","default");
		pushMap.put("badge","1");
		JPushClient jpushClient = new JPushClient(masterSecret, appKey);

//		PushResult result  =  jpushClient.sendAndroidNotificationWithAlias("标题","消息体",new HashMap<String, String>(),"638");
//		Map<String, String> parm = new HashMap<String, String>();
//		//这里的id是,移动端集成极光并登陆后,极光用户的rid
//		parm.put("id", "638");
//		//设置提示信息,内容是文章标题
//		parm.put("msg","测试测试,收到请联系发送人");
//
//
//		jpushAndroid(parm);

//		JPushUtils.senPush(JPushUtils.createPushPayloadRegisterID("您有一条待办消息！",
//				"alert","content",false,pushMap,"1517bfd3f7a91e32eef"));
//		//指定某个账号推送
		Message message = buildMessage("msgtitle！","messagecontent");
		IosAlert alert = IosAlert.newBuilder()
				.setTitleAndBody("TITLE", "", "BODY")
				.setActionLocKey("PLAY")
				.build();


		JPushUtils.senPush(JPushUtils.createPushPayloadAllForIOS("test3", alert,message,true,new HashMap<>(),"638"));
//




/*		JPushClient jpushClient = new JPushClient("c522ade149c824b456c04c3b", "47691475829fb19ad784d0d0");
//


//		//ios （带自定义消息体）
//		jpushClient.sendIosNotificationWithAlias(alert, new HashMap<String, String>(), "625");
//		//安卓（带自定义消息体）
		jpushClient.sendAndroidNotificationWithAlias("您收到一条消息","是我自己传过来的参数,同学们可以自定义参数",new HashMap<>(),"621");
//		jpushClient.sendAndroidMessageWithAlias("您收到一条消息!","dfsfdgsgdrsgesgesgsegr","638");
//
		jpushClient.close();*/
//		messageForIos("619","您收到一条消息","是我自己传过来的参数,同学们可以自定义参数");

//		messageForIos("635","title","body");
//		messageForAndroid("619","title1","body");


//		JPushClient jpushClient = new JPushClient("c522ade149c824b456c04c3b", "47691475829fb19ad784d0d0");
//
//		try{
//			//安卓（带自定义消息体）
//			PushResult result  =  jpushClient.sendAndroidNotificationWithAlias("43242432","43242432",pushMap,"619");
//
//		} catch (APIConnectionException e) {
//			e.printStackTrace();
//		} catch (APIRequestException e) {
//			e.printStackTrace();
//		}finally{
//			jpushClient.close();
//		}

	}

	//极光推送，调用Ios
	public static void messageForIos(String ssId,String title,String body)  {

		JPushClient jpushClient = new JPushClient(masterSecret, appKey);

		IosAlert alert = IosAlert.newBuilder()
				.setTitleAndBody(title, "", body)
				.setActionLocKey("PLAY")
				.build();

		try{
			//ios （带自定义消息体）

			PushResult result  = jpushClient.sendIosNotificationWithAlias(alert, new HashMap<String, String>(), ssId);

		} catch (APIConnectionException e) {
			//不作操作
		} catch (APIRequestException e) {
			//不作操作
		}finally{
			jpushClient.close();
		}

	}


//
//	//极光推送，调用Ios
//	public static void messageForIos(String ssId,String title,String body)  {
//
//		JPushClient jpushClient = new JPushClient(masterSecret, appKey);
//
//		IosAlert alert = IosAlert.newBuilder()
//				.setTitleAndBody(title, "", body)
//				.setActionLocKey("PLAY")
//				.build();
//
//		try{
//			//ios （带自定义消息体）
//			PushResult result  = jpushClient.sendIosNotificationWithAlias(alert, new HashMap<String, String>(), ssId);
//
//		} catch (APIConnectionException e) {
//			e.printStackTrace();
//		} catch (APIRequestException e) {
//			e.printStackTrace();
//		}finally{
//			jpushClient.close();
//		}
//
//	}
//
	//极光推送，调用Android
	public static void messageForAndroid(String ssId,String title,String body)  {

		JPushClient jpushClient = new JPushClient(masterSecret, appKey);

		try{
			//安卓（带自定义消息体）
			PushResult result  =  jpushClient.sendAndroidNotificationWithAlias(title,body,new HashMap<String, String>(),ssId);

		} catch (APIConnectionException e) {
			e.printStackTrace();
		} catch (APIRequestException e) {
			e.printStackTrace();
		}finally{
			jpushClient.close();
		}

	}

	//极光推送>>Android
	//Map<String, String> parm是我自己传过来的参数,同学们可以自定义参数
	public static void jpushAndroid(Map<String, String> parm) {

		//创建JPushClient(极光推送的实例)
		JPushClient jpushClient = new JPushClient(masterSecret, appKey);
		//推送的关键,构造一个payload
		PushPayload payload = PushPayload.newBuilder()
				.setPlatform(Platform.android())//指定android平台的用户
//				.setAudience(Audience.all())//你项目中的所有用户
				.setAudience(Audience.alias("638"))//registrationId指定用户
				.setNotification(Notification.android(parm.get("msg"), "这是title", parm))
				//发送内容
				.setOptions(Options.newBuilder().setApnsProduction(false).build())
				//这里是指定开发环境,不用设置也没关系
				.setMessage(Message.content(parm.get("msg")))//自定义信息
				.build();

		try {
			PushResult pu = jpushClient.sendPush(payload);
		} catch (APIConnectionException e) {
			e.printStackTrace();
		} catch (APIRequestException e) {
			e.printStackTrace();
		}
	}





}
