package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.web.admin.service.RyfxService;
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
@RequestMapping(value = "/ryfx")
public class RyfxAction {
//    2212
Logger logger = LogManager.getLogger(RyglAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    RyfxService ryfxService;


    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("admin/manage/ryfx/list");
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
            page =ryfxService.loadList(param);
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
        ModelAndView view = new ModelAndView("admin/manage/ryfx/add");
        if (StringUtils.isEmpty(id)) {
            return view;
        }
        try {

            Map<String, Object> m = DBHelper.queryForMap("select t.ID, t.SS_ID, t.NAME, t.SEX, t.NL,t.MZ, t.ZJ,t.SG, t.JBZL, t.GRCZS, t.JSZT,t.STZT,t.DWMC,\n" +
                    "t.SHGN,t.GRZXQK,t.CZWT,t.DCFX,to_char(t.CREATE_TIME, 'YYYY-MM-DD HH24:MI:SS') CREATE_TIME, to_char(t.UPDATE_TIME, 'YYYY-MM-DD HH24:MI:SS') UPDATE_TIME,\n" +
                    "t.CZR,t.SFTJ,(SELECT NAME  FROM BUSINESS_DICT BD  WHERE BD.TYPE_CODE = 'zw' AND BD.VALUE = t.ZJ)ZJ_NAME,（SELECT DEPTNAME NAME  FROM SS_DEPT s\n" +
                    " WHERE s.id = t.DWMC AND s.STATUS <> '-1')DWMC_NAME, t.STATUS from TB_RYFX t  WHERE T.ID=? ", id);
            view.addObject("RYFX", m);
            view.addObject("id", id);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }

}
