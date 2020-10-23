package com.tonbu.web.admin.action;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;


@RequestMapping(value = "/app")
@Controller
public class AppAction {


    Logger logger = LogManager.getLogger(AppAction.class.getName());

    @Autowired
    HttpServletRequest request;


    /**
     * 社会助学-信息导入
     * @return
     */
    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("admin/manage/app/List");
        return view;
    }




}
