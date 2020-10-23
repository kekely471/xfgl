package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.RequestUtils;

import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.LeaveService;

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
import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/leave")
public class LeaveAction extends BaseAction {

    Logger logger = LogManager.getLogger(LeaveAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    LeaveService leaveService;

    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("admin/manage/leave/list");
        return view;
    }


    @RequestMapping(value = "/xfyxjtj")
    public ModelAndView xfyxjtj() {
        ModelAndView view = new ModelAndView("admin/manage/leave/xfyxjtj");
        return view;
    }

    @RequestMapping(value = "/dwxjtj")
    public ModelAndView xfyqjtj() {
        ModelAndView view = new ModelAndView("admin/manage/leave/dwxjtj");
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

            page = leaveService.loadList(param);
            //增加剩余可用天数计算
            ((List<Map<String, Object>>) page.getRows()).stream().forEach(item -> {
                item.put("SYTS", outLeftDays(trimToEmpty(item.get("user_id"))));
            });
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


    @RequestMapping(value = "/xfyxjtjlist")
    @ResponseBody
    public Object xfyxjtjlist() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = leaveService.loadxfyxjtjList(param);
            //增加请假天数count
            ((List<Map<String, Object>>) page.getRows()).stream().forEach(item -> {
                item.putAll(leaveService.countLeave(item.get("SS_ID") + ""));
            });
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


    @RequestMapping(value = "/xfydwtjlist")
    @ResponseBody
    public Object xfydwtjlist() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = leaveService.loadxfydwtjList(param);
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
     * 获取添加页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/add1/{id}", "/add1/"})
    public ModelAndView add1(@PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/leave/add1");
        if (StringUtils.isEmpty(id)) {
            return view;
        }
        try {
            Map<String, Object> m = DBHelper.queryForMap("select t.id," +
                    "       t.id as business_id," +
                    "       t.process_instance_id as process_id," +
                    "       t.user_id," +
                    "       u.name," +
                    "       to_char(t.start_time, 'yyyy/MM/dd HH24:mi:ss') AS start_time," +
                    "       to_char(t.end_time, 'yyyy/MM/dd HH24:mi:ss') AS end_time," +
                    "       d.name as leave_type," +
                    "       t.reason," +
                    "       to_char(t.apply_time, 'yyyy/MM/dd HH24:mi:ss') AS apply_time," +
                    "       t.leave_space," +
                    "       t.vacation," +
                    "       t.complete," +
                    "       t.leave_days," +
                    "       t.leave_days_true," +
                    "       t.leave_days_reward," +
                    "       t.report," +
                    "       dept.deptname" +
                    "  from TB_LEAVEAPPLY t" +
                    "  left join tb_user u" +
                    "    on u.ss_id = t.user_id" +
                    "  left join business_DICT d" +
                    "    on d.value = t.leave_type" +
                    "   and d.TYPE_CODE = 'qjlx'" +
                    "   AND d.status = 1" +
                    "  LEFT JOIN SS_DEPT dept" +
                    "    ON dept.id = u.DWMC" +
                    " where t.id= ?", id);

            List<Map<String, String>> history = leaveService.getHistoryByBussinessId(m.get("process_id") + "");

//            班长审核
//            中队审核
//            大队审核
//            警务科审核
//            政治部主任审核
            Map<String, String> bz = new HashMap<>();
            Map<String, String> zd = new HashMap<>();
            Map<String, String> dd = new HashMap<>();
            Map<String, String> jwk = new HashMap<>();
            Map<String, String> zzb = new HashMap<>();
            for (Map<String, String> his : history) {
                if ("班长".equals(his.get("activitiName"))) {
                    bz = his;
                } else if ("中队".equals(his.get("activitiName"))) {
                    zd = his;

                } else if ("大队".equals(his.get("activitiName"))) {
                    dd = his;

                } else if ("警务科".equals(his.get("activitiName"))) {
                    jwk = his;

                } else if ("政治部主任".equals(his.get("activitiName"))) {
                    zzb = his;

                }

            }
            view.addObject("LEAVE", m);


            view.addObject("bz", bz);
            view.addObject("zd", zd);
            view.addObject("dd", dd);
            view.addObject("jwk", jwk);
            view.addObject("zzb", zzb);
            view.addObject("SYTS", outLeftDays(trimToEmpty(m.get("user_id"))));
            view.addObject("id", id);
            return view;
        } catch (ActionException ex) {
            throw ex;
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
            leaveService.del(param);
            r.setMsg("删除成功");
        } catch (JSONException e) {
            r.setCode(-1);
            r.setMsg(e.getMessage());
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }


    //上级查看 在位率
    @RequestMapping(value = "/getpageZWL")
    @ResponseBody
    public Object getpageZWLforH5() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {

            List<Map<String, Object>> dwList = new ArrayList<>();

            dwList = DBHelper.queryForList("select t.id,t.deptname,deptno from ss_dept t where t.isunit=1 ");

            for (int i = 0; i < dwList.size(); i++) {
                //总人数
                String dwcount = DBHelper.queryForScalar("select count(1) from tb_user t  where t.dwmc=?", String.class, dwList.get(i).get("ID"));
                //请假人数
                String bzwcount = leaveService.getpageZWLforH5(trimToEmpty(dwList.get(i).get("ID")));
                dwList.get(i).put("dwcount", dwcount);
                dwList.get(i).put("bzwcount", bzwcount);
                dwList.get(i).put("zwcount", (Integer.valueOf(dwcount) - Integer.valueOf(bzwcount)) + "");
                BigDecimal dwcountDecimal = new BigDecimal(dwcount);
                BigDecimal zwcountDecimal = new BigDecimal(Integer.valueOf(dwcount) - Integer.valueOf(bzwcount));
                BigDecimal zwl = zwcountDecimal.divide(dwcountDecimal, 2, BigDecimal.ROUND_HALF_UP);
                dwList.get(i).put("zwl", zwl.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
            }

            r.setDataset(dwList);
            r.setMsg("操作成功");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
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
