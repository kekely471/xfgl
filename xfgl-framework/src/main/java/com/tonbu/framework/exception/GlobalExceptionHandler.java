package com.tonbu.framework.exception;

import com.tonbu.framework.data.ResultEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

@ControllerAdvice
class GlobalExceptionHandler {
    Logger logger = LogManager.getLogger(GlobalExceptionHandler.class.getName());

    public static final String ERROR_VIEW_500 = "error/error-500";
    //500错误
    @ExceptionHandler(value = Exception.class)
    public ModelAndView defaultErrorHandler(Exception e) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("message", e.getMessage());
        mav.setViewName(ERROR_VIEW_500);
        return mav;
    }
    //500错误
    @ExceptionHandler(value = ActionException.class)
    public ModelAndView defaultErrorHandler(ActionException e) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("message", e.getMessage());
        mav.setViewName(ERROR_VIEW_500);
        return mav;
    }

    //ajax 500错误
    @ExceptionHandler(value = JSONException.class)
    @ResponseBody
    public ResultEntity jsonErrorHandler(JSONException e) {
        ResultEntity r = new ResultEntity();
        r.setMsg(e.getMessage());
        r.setCode(-1);
        return r;
    }

    @ExceptionHandler(value = {RuntimeException.class, BadSqlGrammarException.class, SQLSyntaxErrorException.class, ArithmeticException.class})
    @ResponseBody
    public Object defaultErrorHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
//        boolean isAjax = request.getHeader("X-Requested-With") != null &&
//                "XMLHttpRequest".equals(request.getHeader("X-Requested-With").toString());
//        if (isAjax) {
//
//        }
        ResultEntity r = new ResultEntity(1);
        if (e instanceof SQLException){
            r.setCode(Integer.valueOf(ExceptionEnum.SQLException.code));
            r.setMsg(ExceptionEnum.SQLException.value);
        } else if (e instanceof BadSqlGrammarException) {
            r.setCode(Integer.valueOf(ExceptionEnum.BadSqlGrammarException.code));
            r.setMsg(ExceptionEnum.BadSqlGrammarException.value);
        } else if (e instanceof CannotGetJdbcConnectionException) {
            r.setCode(Integer.valueOf(ExceptionEnum.CannotGetJdbcConnectionException.code));
            r.setMsg(ExceptionEnum.CannotGetJdbcConnectionException.value);
        }else if (e instanceof ArithmeticException) {
            r.setCode(Integer.valueOf(ExceptionEnum.ArithmeticException.code));
            r.setMsg(ExceptionEnum.ArithmeticException.value);
        }else if (e instanceof  NegativeArraySizeException){
            r.setCode(Integer.valueOf(ExceptionEnum.NegativeArraySizeException.code));
            r.setMsg(ExceptionEnum.NegativeArraySizeException.value);
        }else if (e instanceof CustomizeException){
            r.setCode(Integer.valueOf(ExceptionEnum.CustomizeException.code));
            r.setMsg(e.getMessage());
        } else {
            String exStr = e.getMessage();
            if (null == exStr){
                r.setCode(Integer.valueOf(ExceptionEnum.Exception.code));
                r.setMsg(ExceptionEnum.Exception.value);
            } else if (exStr.contains("ORA-")) {
                if (exStr.contains("值太大")) {
                    r.setCode(Integer.valueOf(ExceptionEnum.SQLException.code));
                    r.setMsg("字段值超出限制");
                } else if (exStr.contains("标识符无效")){
                    r.setCode(Integer.valueOf(ExceptionEnum.SQLException.code));
                    r.setMsg("标识符无效");
                } else if (exStr.contains("无效数字")){
                    r.setCode(Integer.valueOf(ExceptionEnum.SQLException.code));
                    r.setMsg("无效数字");
                } else {
                    r.setCode(Integer.valueOf(ExceptionEnum.SQLException.code));
                    r.setMsg(ExceptionEnum.SQLException.value);
                }
            } else {
                r.setCode(Integer.valueOf(ExceptionEnum.ArithmeticException.code));
                r.setMsg(ExceptionEnum.Exception.value);
            }
        }
        logger.error(e.getMessage(),e);
        return r;
    }
}
