package com.tonbu.web.app.itsm.action;

import com.tonbu.web.app.itsm.service.FaqService;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.util.RequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping(value="/faq")
public class FAQAction {


    Logger logger = LogManager.getLogger(FAQAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    private FaqService faqService;


    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("itsm/faq/list");

        return view;
    }



    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = faqService.loadList(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
            throw ex;
        }
        finally {
            return r;
        }
    }


    /**
     * 获取添加页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/add/{id}", "/add"})
    public ModelAndView add(@PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("itsm/faq/add");
        if (StringUtils.isEmpty(id)) {
            Map<String, Object> m=new HashMap<>();
            view.addObject("FAQ", m);
            return view;
        }
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.TITLE,V.CREATE_ID,V.CREATE_TIME,V.CONTENT,V.FAQ_TYPE,V.KEYWORDS,V.UPDATE_ID,V.UPDATE_TIME,V.VIEW FROM FAQ V WHERE V.ID=? ", id);
            view.addObject("FAQ", m);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }


    /**
     * 查看常见问题，累计阅读量
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/view/{id}"})
    public ModelAndView view(@PathVariable String id) {
        ModelAndView view = new ModelAndView("itsm/faq/detail");
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.TITLE,V.CREATE_ID,V.CREATE_TIME,V.CONTENT,V.FAQ_TYPE,V.KEYWORDS,V.UPDATE_ID,V.UPDATE_TIME,V.VIEW FROM FAQ V WHERE V.ID=? ", id);
            view.addObject("FAQ", m);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }


    /**
     * 获取一条数据
     *
     * @return ResultEntity
     */
    @RequestMapping(value = "/get/{id}")
    @ResponseBody
    public Object get(@PathVariable String id) {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        try {
            List<Map<String, Object>> l = DBHelper.queryForList("SELECT V.ID,V.TITLE,V.CREATE_ID,V.CREATE_TIME,V.CONTENT,V.FAQ_TYPE,V.KEYWORDS,V.UPDATE_ID,V.UPDATE_TIME,V.VIEW FROM FAQ V WHERE V.ID=? ", id);
            r.setDataset(l);
            r.setMsg("获取数据成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
            throw ex;
        }
        finally {
            return r;
        }
    }




    /**
     * 保存数据
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/save")
    @ResponseBody
    public Object save() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            faqService.save(param);
            r.setMsg("操作成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
            throw ex;
        }finally {
            return r;
        }
    }


    /**
     * 删除数据，假删除
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/del")
    @ResponseBody
    public Object del() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            faqService.del(param);
            r.setMsg("删除成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
            throw ex;
        }
        finally {
            return r;
        }
    }

    /**
     * 删除数据，真删除
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/realDel")
    @ResponseBody
    public Object relDel() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            faqService.realDel(param);
            r.setMsg("彻底删除成功！");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
            throw ex;
        }
        finally {
            return r;
        }
    }


}
