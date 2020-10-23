package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.web.admin.service.GgglService;
import com.tonbu.web.admin.service.RyglService;
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
@RequestMapping(value = "/gggl")
public class GgglAction {

    Logger logger = LogManager.getLogger(GgglAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    GgglService ggglService;
    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("admin/manage/gggl/list");
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
            page =ggglService.loadList(param);
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
        ModelAndView view = new ModelAndView("admin/manage/gggl/add");
        if (StringUtils.isEmpty(id)) {
            return view;
        }
        try {
            Map<String, Object> m = DBHelper.queryForMap("select  id,TITLE,DESCRIBE,URL,USER_ID,DETAILS,TUPIAN,TYPE,FILE_ID,\n" +
                    "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'sf'  AND BD.VALUE = t.TYPE)TYPE_NAME,STATUS,\n" +
                    " to_char(CREATE_DATE,'YYYY-MM-DD HH24:MI:SS')CREATE_DATE,to_char(UPDATE_TIME,'YYYY-MM-DD HH24:MI:SS')UPDATE_TIME\n" +
                    "   from   TB_CAROUSEL t  where t.id=? ", id);
            view.addObject("GGGL", m);
            view.addObject("id", id);
            return view;
        } catch (ActionException ex) {
            throw ex;
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
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            ggglService.save(param);
            r.setMsg("操作成功");
        } catch (JSONException e){
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
            ggglService.del(param);
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
