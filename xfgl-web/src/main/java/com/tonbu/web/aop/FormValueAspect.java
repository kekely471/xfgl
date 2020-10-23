package com.tonbu.web.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.tonbu.web.admin.service.CommonService;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
public class FormValueAspect {

    @Autowired
    private CommonService commonService;

    //    @Pointcut("execution(public * com.tonbu.*.*.service..*.save*(..)) || execution(public * com.tonbu.*.*.service..*.del*(..)) || execution(public * com.tonbu.*.*.service..*.realDel*(..))")
    @Pointcut("@annotation(com.tonbu.web.aop.AopFormValue)")
    public void Log() {
    }

    @AfterReturning("Log()")
    public void doAfterReturning(JoinPoint joinPoint) {
        Object[] objArr = joinPoint.getArgs();
        if (objArr != null) {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(objArr[0]);
            Map<String, String> params = JSONObject.parseObject(jsonObject.toJSONString(), new TypeReference<Map<String, String>>() {
            });
            // 保存自定义表单值
            if (StringUtils.isNotBlank(params.get("formType"))) {
                commonService.getRenderingKeyBySSFORMFRAME(params);
            }
        }

    }
}