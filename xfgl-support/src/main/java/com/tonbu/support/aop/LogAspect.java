package com.tonbu.support.aop;

import com.google.gson.Gson;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.support.service.AopLogService;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.support.util.IPUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class LogAspect{

    @Autowired
    private AopLogService aopLogService;

    @Pointcut("execution(public * com.tonbu.*.*.service..*.save*(..)) || execution(public * com.tonbu.*.*.service..*.del*(..)) || execution(public * com.tonbu.*.*.service..*.realDel*(..)) || execution(public * com.tonbu.*.*.service..*.update*(..))")
    public void Log(){
    }

    @AfterReturning(value = "Log()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        Map<String,String> map = new HashMap<>();
        // 记录下请求内容
        Object[] objArr = joinPoint.getArgs();
        StringBuffer sb = new StringBuffer();
        UserEntity u = CustomRealm.GetLoginUser();
        String userId = "";
        for (Object obj : objArr) {
            sb.append(obj.toString());
            try{
                //手机端接口
                if(u == null && obj instanceof UserEntity){
                     u = (UserEntity)obj;
                }
                if(u == null && obj instanceof  Map){
                    userId = ((Map) obj).get("userId") == null ? "" : ((Map) obj).get("userId").toString();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        map.put("param",sb.toString());
        map.put("url",request.getRequestURL().toString());
        map.put("responseInfo",result == null ? "" : new Gson().toJson(result));
        map.put("operateionType",request.getMethod());
        map.put("userIp", IPUtil.getIpAddr(request));
        map.put("location","");
        map.put("modelName",joinPoint.getSignature().getDeclaringTypeName());
        map.put("contentBack","");
        map.put("userApp","");
        if(u != null){
            map.put("accountId",u.getId());
            map.put("userId",u.getUser_id());
            map.put("userName",u.getUsername());
        }else{
            map.put("userId",userId);
        }
        Method method = ((MethodSignature)joinPoint.getSignature() ).getMethod();
        if (null != method) {
            AopLog op = method.getAnnotation(AopLog.class);
            if(op != null){
                map.put("modelName",op.module());
                map.put("operateionType",op.method());
            }
        }
        aopLogService.save(map);
    }

}