package com.tonbu.web.admin.action;

import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.JSONException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequestMapping(value = "/point")
@Controller
public class PointAction {
    Logger logger = LogManager.getLogger(PointAction.class.getName());

    @Autowired
    HttpServletRequest request;




    @RequestMapping(value = "/import")
    public ModelAndView toImport() {
        ModelAndView view = new ModelAndView("admin/manage/rygl/import");
        return view;
    }




}
