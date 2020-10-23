package com.tonbu.framework.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/error")
public class ErrorAction implements ErrorController {



    Logger logger = LogManager.getLogger(ErrorAction.class.getName());

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping
    public ModelAndView error(HttpServletRequest request) {
        //获取statusCode:401,404,500
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        ModelAndView mav = new ModelAndView();
        if(statusCode == 404){
          mav.setViewName("error/error-404");
        }else if(statusCode == 403){
            mav.setViewName("error/error-403");
        }else{
            mav.setViewName("error/error");
        }
        mav.addObject("message", statusCode);
        return mav;
    }

}
