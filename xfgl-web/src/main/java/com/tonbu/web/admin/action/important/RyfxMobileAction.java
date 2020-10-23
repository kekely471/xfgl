package com.tonbu.web.admin.action.important;

import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.web.admin.common.UserUtils;
import com.tonbu.web.admin.service.CommonService;
import com.tonbu.web.admin.service.important.RyfxMoblieService;
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
import java.util.List;
import java.util.Map;

/**
 * 人员分析手机端接口
 */
@RequestMapping(value = "/ryfxMobile")
@Controller
public class RyfxMobileAction {
    Logger logger = LogManager.getLogger(RyfxMobileAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    private CommonService commonService;

    @Autowired
     RyfxMoblieService ryfxMoblieService;

    //手机端h5页面
    public static final String VIEW_DIR = "important/mobile/";


    /**
     * to新增人员分析
     * @param id
     * @return
     */
    @RequestMapping(value = {"/add/{id}"})
    public ModelAndView add(@PathVariable(required = false) String id , String userId ) {
        ModelAndView view = new ModelAndView();
        view.addObject("userId",userId); //用户token传给页面
//        view.setViewName(VIEW_DIR+"ryfx_add");
        /*加载对应的请假外出人数反回给页面*/
        if (StringUtils.isEmpty(id)) {
            return view;
        }
        try{
            StringBuffer sql = new StringBuffer();
            sql.append("select t.id,t.ss_id,(select DEPTNAME NAME  FROM SS_DEPT s WHERE  s.id=t.dwmc)dwmc_name,t.dwmc,t.name,(select name from  business_dict b WHERE  b.type_code='sex' AND b.value=t.sex)sexname,sex,\n" +
                    "nl,mz,zj,sg,jbzl,grczs,jszt,stzt,shgn,grzxqk,czwt,dcfx,TO_CHAR(t.CREATE_TIME,'\" + Constants.ORACLE_FORMATDATE_FULL + \"') as  CREATE_TIME,\n" +
                    "TO_CHAR(t.UPDATE_TIME,'\" + Constants.ORACLE_FORMATDATE_FULL + \"') as  UPDATE_TIME,status,czr,sftj,\n" +
                    "case when t.STATUS=1 then '归档' when t.STATUS =0 then '未归档' end STATUSNAME,\n" +
                    " case when t.SFTJ= 0 THEN '否'  when t.STATUS =1  then '是' end SFTJNAME\n" +
                    "  from TB_RYFX t left join TB_USER c on c.SS_ID = t.SS_ID where t.id=? ");
            Map<String,Object> m= DBHelper.queryForMap(sql.toString(),id);
            view.addObject("RYFX", m);
            view.addObject("id", id);
            return view;
        }catch (ActionException ex){
            throw ex;
        }
    }
    /**
     * 人员分析列表未提交
     * @return
     */
    @RequestMapping(value = "/wtj/list")
    @ResponseBody
    public Object wtj() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String,Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId =user.get("SS_ID").toString();
        param.put("userId",userId);
        Page<?> page;
        try {
            page =ryfxMoblieService.findwtjryfxPage(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }


    /**
     * to未提交人员分析列表页面
     * @return
     */
    @RequestMapping(value = "/wjtlist")
    public ModelAndView wtjlist() {
        ModelAndView view = new ModelAndView("admin/important/moblie/ryfx_add.html");
        return view;
    }
    /**
     * to新增人员分析
     * @param
     * @return
     */
    @RequestMapping(value = "/ryfx/save")
    @ResponseBody
    public Object ryfxsave() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String,Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId =user.get("SS_ID").toString();
        param.put("userId",userId);
        Page<?> page;
        try {
            ryfxMoblieService.tj(param);
            r.setMsg("操作成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }
    /**
     * to已提交人员分析列表页面
     * @return
     */
    @RequestMapping(value = "/yjtlist")
    public ModelAndView ytjlist() {
        ModelAndView view = new ModelAndView("admin/important/moblie/ytj_list");
        return view;
    }

    /**
     * to 已归档页面
     * @return
     */
    @RequestMapping(value = "/ygdlist")
    public ModelAndView ygdlist() {
        ModelAndView view = new ModelAndView(VIEW_DIR+"rygl_ygd");
        return view;
    }
/*
未归档
 */
    @RequestMapping(value = "/wgdlist")
    public ModelAndView wgdlist() {
        ModelAndView view = new ModelAndView(VIEW_DIR+"rygl_wgd");
        return view;
    }
    /**
     * 人员分析列表已提交
     * @return
     */
    @RequestMapping(value = "/ytj/list")
    @ResponseBody
    public Object ytj() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String,Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId =user.get("SS_ID").toString();
        param.put("userId",userId);
        Page<?> page;
        try {
            page =ryfxMoblieService.findytjryfxPage(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    /**
     * 人员分析未归档列表
     * @return
     */
    @RequestMapping(value = "/wgd/list")
    @ResponseBody
    public Object wgd() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String,Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId =user.get("SS_ID").toString();
        param.put("userId",userId);
        Page<?> page;
        try {
            page =ryfxMoblieService.findytjryfxPage(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    /**
     * 人员分析已归档列表
     * @return
     */
    @RequestMapping(value = "/ygd/list")
    @ResponseBody
    public Object ygd() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String,Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId =user.get("SS_ID").toString();
        param.put("userId",userId);
        Page<?> page;
        try {
            page =ryfxMoblieService.findygdryfxPage(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }
    /**
     * 逻辑删除
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
            ryfxMoblieService.del(param);
            r.setMsg("删除成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }
    /**
     * 获取业务字典表
     * @return
     */
    @RequestMapping(value = "/dict/business")
    @ResponseBody
    public Object businessDictList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.loadBusDictList(param);
            r.setDataset(list);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }
    /**
     * 保存
     * @param
     * @return
     */
    @RequestMapping(value = "/ryfx/tj")
    @ResponseBody
    public Object ryfxtj() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String,Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId =user.get("SS_ID").toString();
        param.put("userId",userId);
        Page<?> page;
        try {
            ryfxMoblieService.bc(param);
            r.setMsg("操作成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

}
