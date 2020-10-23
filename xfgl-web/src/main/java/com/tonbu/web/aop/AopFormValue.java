package com.tonbu.web.aop;

import java.lang.annotation.*;

/**
 * 自定义操作日志标签，模块名和方法名
 * */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AopFormValue {
}
