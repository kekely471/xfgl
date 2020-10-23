package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.web.admin.service.KqglService;
import com.tonbu.web.admin.service.YdxfService;
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
@RequestMapping(value = "/ydxfgl")
public class YdxfAction {
    Logger logger = LogManager.getLogger(RyglAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    YdxfService ydxfService;

    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("admin/manage/ydxf/list");
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
            page =ydxfService.loadList(param);
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
        ModelAndView view = new ModelAndView("admin/manage/ydxf/add");
        if (StringUtils.isEmpty(id)) {
            return view;
        }
        try {
            Map<String, Object> m = DBHelper.queryForMap( "select t.id,t.imel,t.ss_id,t.username,t.dept_id,(SELECT DEPTNAME NAME FROM SS_DEPT s WHERE s.id = t.DEPT_ID )dwmc,t.textbody,t.phone ,to_char(t.create_time,'YYYY-MM-DD HH24:MI:SS')CREATETIME from TB_MONTHLYCONSUMPTION t  where t.id=? ", id);
            view.addObject("YDXF", m);
            view.addObject("id", id);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }

}
