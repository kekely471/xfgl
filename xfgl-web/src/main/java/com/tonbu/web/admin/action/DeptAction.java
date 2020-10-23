package com.tonbu.web.admin.action;

import com.tonbu.framework.exception.JSONException;
import com.tonbu.web.admin.service.DeptService;
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

@RequestMapping(value = "/dept")
@Controller
public class DeptAction {
    Logger logger = LogManager.getLogger(DeptAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    private DeptService deptService;

    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("admin/manage/dept/list");
        return view;
    }


    @RequestMapping(value = {"/location/{location}","/location"})
    public ModelAndView location(@PathVariable(required = false) String location) {
        ModelAndView view = new ModelAndView("admin/manage/dept/location");
        view.addObject("location", location);
        return view;
    }

    /**
     * 部门用户树列表
     */
    @RequestMapping(value = "/userTree")
    @ResponseBody
    public Object deptUserTree(){
        ResultEntity r = new ResultEntity(1);
        Map<String,String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = deptService.loadzTreeList(param);
            r.setDataset(list);
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
    @RequestMapping(value = {"/add/{selectId}/{id}", "/add/{selectId}"})
    public ModelAndView add(@PathVariable String selectId, @PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/dept/add");
        if (StringUtils.isEmpty(id)) {
            Integer sort = DBHelper.queryForScalar("SELECT (MAX(SORT)+1) FROM SS_DEPT ", Integer.class);
            Map<String, Object> m=new HashMap<>();
            m.put("SORT",sort);
            view.addObject("DEPT", m);
            view.addObject("treeId", selectId);
            return view;
        }
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.DEPTNAME,V.DEPTNO,V.PARENTID,V.SORT,V.ADDRESS,V.LOCATION,V.ISUNIT,V.SIMPLENAME,V.STATUS,V.REMARK,V.TEL,v.LEAVE_LIMIT" +
                    " FROM SS_DEPT V WHERE V.ID=? ", id);
            view.addObject("DEPT", m);
            view.addObject("treeId", '0');
            view.addObject("id",id);
            return view;
        } catch (ActionException ex) {
            logger.error(ex.getMessage(),ex);
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
            List<Map<String, Object>> l = DBHelper.queryForList("SELECT V.ID,V.DEPTNAME,V.DEPTNO,V.PARENTID,V.SORT,V.ADDRESS,V.LOCATION,V.ISUNIT,V.SIMPLENAME,V.STATUS,V.REMARK,v.LEAVE_LIMIT " +
                    "FROM SS_DEPT V WHERE V.ID=? ", id);
            r.setDataset(l);
            r.setMsg("获取数据成功");
        }catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }


    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = deptService.loadList(param);
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
            deptService.save(param);
            r.setMsg("操作成功");
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
            deptService.del(param);
            r.setMsg("删除成功");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
        } catch (RuntimeException ex) {
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
    @RequestMapping(value = "/realDel")
    @ResponseBody
    public Object realDel() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            deptService.realDel(param);
            r.setMsg("彻底删除成功！");
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


}
