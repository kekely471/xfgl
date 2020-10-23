package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.web.admin.service.DictService;
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

@RequestMapping(value = "/dict")
@Controller
public class DictAction {

    Logger logger = LogManager.getLogger(DictAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    private DictService dictService;

    @RequestMapping(value = "/type/")
    public ModelAndView type_home() {
        ModelAndView view = new ModelAndView("admin/setting/dict/type/list");
        return view;
    }

    @RequestMapping(value = "/sys/")
    public ModelAndView sys_home() {
        ModelAndView view = new ModelAndView("admin/setting/dict/sys/list");
        return view;
    }

    @RequestMapping(value = "/business/")
    public ModelAndView business_home() {
        ModelAndView view = new ModelAndView("admin/setting/dict/business/list");
        return view;
    }


    @RequestMapping(value = "/set/{dict_type}/{type_code}")
    public ModelAndView business_home(@PathVariable String dict_type, @PathVariable String type_code) {
        ModelAndView view = new ModelAndView();
        view.addObject("type_code", type_code);
        if (StringUtils.equals("0", dict_type)) {
            view.setViewName("admin/setting/dict/sys/list");
            return view;

        } else {
            view.setViewName("admin/setting/siness/list");
            return view;
        }


    }


    @RequestMapping(value = "/type/list")
    @ResponseBody
    public Object type_list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = dictService.loadTypeList(param);
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
    @RequestMapping(value = {"/type/add/{id}", "/type/add"})
    public ModelAndView type_add(@PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/setting/dict/type/add");
        if (StringUtils.isEmpty(id)) {
            return view;
        }
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.CODE,V.TYPE,V.NAME,V.REMARK FROM SS_DICT_TYPE V WHERE V.ID=?", id);
            view.addObject("DICTTYPE", m);
            view.addObject("id", id);
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
    @RequestMapping(value = "/type/get/{id}")
    @ResponseBody
    public Object type_get(@PathVariable String id) {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        try {
            List<Map<String, Object>> l = DBHelper.queryForList("SELECT V.ID,V.CODE,V.TYPE,V.NAME,V.REMARK FROM SS_DICT_TYPE V WHERE V.ID=?", id);
            r.setDataset(l);
            r.setMsg("获取数据成功");
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
     * 保存数据
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/type/save")
    @ResponseBody
    public Object type_save() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            dictService.saveType(param);
            r.setMsg("操作成功");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
        }catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
        } finally {
            return r;
        }
    }

    /**
     * 删除数据，真删除
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/type/realDel")
    @ResponseBody
    public Object type_realDel() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            dictService.realDelType(param);
            r.setMsg("彻底删除成功！");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }

    /********系统字典**********/


    @RequestMapping(value = "/sys/list")
    @ResponseBody
    public Object sys_list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = dictService.loadSysList(param);
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
    @RequestMapping(value = {"/sys/add/{type_code}/{id}", "/sys/add/{type_code}"})
    public ModelAndView sys_add(@PathVariable String type_code, @PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/setting/dict/sys/add");
        view.addObject("type_code", type_code);
        if (StringUtils.isEmpty(id)) {
            Integer sort = DBHelper.queryForScalar("SELECT (MAX(SORT)+1) FROM SS_DICT ", Integer.class);
            Map<String, Object> m = new HashMap<>();
            m.put("SORT", sort);
            view.addObject("SYSDICT", m);
            return view;
        }
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.TYPE_CODE,V.VALUE,V.NAME,V.SORT,V.CN,V.EXT1,V.EXT2,V.EXT3,V.STATUS FROM SS_DICT V WHERE V.ID=?", id);
            view.addObject("SYSDICT", m);
            view.addObject("id", id);
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
    @RequestMapping(value = "/sys/get/{id}")
    @ResponseBody
    public Object sys_get(@PathVariable String id) {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        try {
            List<Map<String, Object>> l = DBHelper.queryForList("SELECT V.ID,V.TYPE_CODE,V.VALUE,V.NAME,V.SORT,V.CN,V.EXT1,V.EXT2,V.EXT3,V.STATUS FROM SS_DICT V WHERE V.ID=?", id);
            r.setDataset(l);
            r.setMsg("获取数据成功");
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
     * 保存数据
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/sys/save")
    @ResponseBody
    public Object sys_save() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            dictService.saveSys(param);
            r.setMsg("操作成功");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
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
    @RequestMapping(value = "/sys/del")
    @ResponseBody
    public Object sys_del() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            dictService.delSys(param);
            r.setMsg("彻底删除成功！");
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
    @RequestMapping(value = "/sys/realDel")
    @ResponseBody
    public Object sys_realDel() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            dictService.realDelSys(param);
            r.setMsg("彻底删除成功！");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }


    /********业务字典**********/


    @RequestMapping(value = "/business/list")
    @ResponseBody
    public Object business_list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = dictService.loadBusinessList(param);
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
    @RequestMapping(value = {"/business/add/{type_code}/{id}", "/business/add/{type_code}"})
    public ModelAndView business_add(@PathVariable String type_code, @PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/setting/dict/business/add");
        view.addObject("type_code", type_code);
        if (StringUtils.isEmpty(id)) {
            Integer sort = DBHelper.queryForScalar("SELECT (MAX(SORT)+1) FROM BUSINESS_DICT ", Integer.class);
            Map<String, Object> m = new HashMap<>();
            m.put("SORT", sort);
            view.addObject("BUSINESSDICT", m);
            return view;
        }
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.TYPE_CODE,V.VALUE,V.NAME,V.SORT,V.CN,V.EXT1,V.EXT2,V.EXT3,V.STATUS,V.REMARK FROM BUSINESS_DICT V WHERE V.ID=?", id);
            view.addObject("BUSINESSDICT", m);
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
    @RequestMapping(value = "/business/get/{id}")
    @ResponseBody
    public Object business_get(@PathVariable String id) {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        try {
            List<Map<String, Object>> l = DBHelper.queryForList("SELECT V.ID,V.TYPE_CODE,V.VALUE,V.NAME,V.SORT,V.CN,V.EXT1,V.EXT2,V.EXT3,V.STATUS FROM BUSINESS_DICT V WHERE V.ID=?", id);
            r.setDataset(l);
            r.setMsg("获取数据成功");
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
     * 保存数据
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/business/save")
    @ResponseBody
    public Object business_save() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            dictService.saveBusiness(param);
            r.setMsg("操作成功");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
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
    @RequestMapping(value = "/business/del")
    @ResponseBody
    public Object business_del() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            dictService.delBusiness(param);
            r.setMsg("删除成功！");
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
    @RequestMapping(value = "/business/realDel")
    @ResponseBody
    public Object business_realDel() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            dictService.realDelBusiness(param);
            r.setMsg("彻底删除成功！");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }


}
