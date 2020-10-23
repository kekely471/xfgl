package com.tonbu.web.demo.action;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;

@RequestMapping(value = "/demo")
@Controller
public class DemoAction {
    Logger logger = LogManager.getLogger(DemoAction.class.getName());

    @Autowired
    HttpServletRequest request;


    @RequestMapping(value = "/form1")
    public ModelAndView form1() {
        ModelAndView view = new ModelAndView("demo/demoForm1");
        return view;
    }

    @RequestMapping(value = "/form2")
    public ModelAndView form2() {
        ModelAndView view = new ModelAndView("demo/demoForm2");
        return view;
    }

    @RequestMapping(value = "/form3")
    public ModelAndView form3() {
        ModelAndView view = new ModelAndView("demo/demoForm3");
        return view;
    }

    @RequestMapping(value = "/currentForm")
    public ModelAndView currentForm() {
        ModelAndView view = new ModelAndView("demo/currentForm");
        return view;
    }

    @RequestMapping(value = "/set")
    public ModelAndView set() {
        ModelAndView view = new ModelAndView("demo/set");
        return view;
    }

    @RequestMapping(value = "/icon")
    public ModelAndView icon() {
        ModelAndView view = new ModelAndView("demo/icon");
        return view;
    }

    @RequestMapping(value = "/map")
    public ModelAndView map() {
        ModelAndView view = new ModelAndView("demo/map");
        return view;
    }

    @RequestMapping(value = "/slider")
    public ModelAndView slider() {
        ModelAndView view = new ModelAndView("demo/slider");
        return view;
    }

    @RequestMapping(value = "/button")
    public ModelAndView button() {
        ModelAndView view = new ModelAndView("demo/button");
        return view;
    }

    @RequestMapping(value = "/ureport")
    public ModelAndView ureport() {
        ModelAndView view = new ModelAndView("demo/ureport");
        return view;
    }

    @RequestMapping(value = "/layuiExport")
    public ModelAndView layuiExport() {
        ModelAndView view = new ModelAndView("demo/layuiExport");
        return view;
    }

    @RequestMapping(value = "/export")
    public ModelAndView export() {
        ModelAndView view = new ModelAndView("demo/export");
        return view;
    }

    @RequestMapping(value = "/mIm")
    public ModelAndView mIm() {
        ModelAndView view = new ModelAndView("demo/mobileIm");
        return view;
    }

    @RequestMapping(value = "/list")
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("demo/list");
        return view;
    }

    @RequestMapping(value = "/list2")
    public ModelAndView list2() {
        ModelAndView view = new ModelAndView("demo/list2");
        return view;
    }

    @RequestMapping(value = "/list3")
    public ModelAndView list3() {
        ModelAndView view = new ModelAndView("demo/list3");
        return view;
    }

    @RequestMapping(value = "/update")
    public ModelAndView update() {
        ModelAndView view = new ModelAndView("demo/update");
        return view;
    }

    @RequestMapping(value = "/line")
    public ModelAndView line() {
        ModelAndView view = new ModelAndView("demo/line");
        return view;
    }

    @RequestMapping(value = "/bar")
    public ModelAndView bar() {
        ModelAndView view = new ModelAndView("demo/bar");
        return view;
    }
}
