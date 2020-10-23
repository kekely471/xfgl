package com.tonbu.support.aop;

import com.google.gson.JsonSyntaxException;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.AppException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Aspect
@Component
@Order(2)
public class ResultAspect {
    //定义日志记录器--获取sl4j包下提供的logger
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("{spring.profiles.active}")
    private String profiles_active;

    /***
     * 定义切入点
     * 1、execution 表达式主体
     */
    @Pointcut("execution(Object com.tonbu..*.action..*(..)) && @annotation(org.springframework.web.bind.annotation.ResponseBody)")
    public void resultPointcut() { }



    @Around("resultPointcut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        ResultEntity re = new ResultEntity(1);
        /**
         * 1.获取request信息
         * 2.根据request获取session
         * 3.从session中取出登录用户信息
         */
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes)ra;
        HttpServletRequest request = sra.getRequest();
        String terminal = request.getParameter("terminal");

        Object resultObject = null;
        try{
            Object result = pjp.proceed();// result的值就是被拦截方法的返回值
            if (result != null) {
                if(String.class.isAssignableFrom((result.getClass()))){
                    return result;
                }
                if(ResultEntity.class.isAssignableFrom(result.getClass())){
                   return result;
                }else if (Page.class.isAssignableFrom(result.getClass())) {
                    re.setDataset(((Page)result));
                    re.setMsg("");
                    return re;
                } else if(result instanceof List){
                    re.setDataset((List)result);
                    re.setMsg("");
                    return re;
                }else if(result instanceof Map){
                    List  list = new ArrayList<>();
                    list.add(result);
                    re.setDataset(list);
                    re.setMsg("");
                    return re;
                }
            }

        }catch (JsonSyntaxException e){
            e.printStackTrace();
            re.setCodeMsg(-1,"解析异常");
        }catch (AppException e){
            e.printStackTrace();
            re = e.getResultEntity();
        }catch (Exception e){
           e.printStackTrace();
           re.setCodeMsg(-1,"网络异常");
        }
        resultObject = re;

        return resultObject;

    }



}
