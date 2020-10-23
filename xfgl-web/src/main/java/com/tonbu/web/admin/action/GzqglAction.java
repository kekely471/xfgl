package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.web.admin.service.GzqglService;
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
import java.util.Map;

@Controller
@RequestMapping(value = "/gzqgl")
public class GzqglAction {

    Logger logger = LogManager.getLogger(GzqglAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    GzqglService gzqglService;

    @RequestMapping
    public String home(){
        return "admin/manage/gzqgl/list";
    }

    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page =gzqglService.loadList(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }

    /**
     * 获取添加页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/add/{id}", "/add/"})
    public ModelAndView add(@PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/gzqgl/add");
        view.addObject("id", id);
//        if (StringUtils.isEmpty(id)) {
//
//        }
        return view;
//        try {
//            Map<String, Object> m = DBHelper.queryForMap("", id);
//            view.addObject("GGGL", m);
//            view.addObject("id", id);
//            return view;
//        } catch (ActionException ex) {
//            throw ex;
//        }
    }

    /**
     * 删除数据，真删除
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
            gzqglService.del(param);
            r.setMsg("删除成功");
        }catch (JSONException e){
            r.setCode(-1);
            r.setMsg(e.getMessage());
        }catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }

    //--------------------评论---------------------

    @RequestMapping(value = "/pllist")
    @ResponseBody
    public Object pllist() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page =gzqglService.loadPlList(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }

    /**
     * 删除数据，真删除
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/pldel")
    @ResponseBody
    public Object pldel() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            gzqglService.pldel(param);
            r.setMsg("删除成功");
        }catch (JSONException e){
            r.setCode(-1);
            r.setMsg(e.getMessage());
        }catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }

}
