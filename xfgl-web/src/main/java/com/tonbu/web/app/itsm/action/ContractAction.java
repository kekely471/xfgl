package com.tonbu.web.app.itsm.action;

import com.tonbu.web.app.itsm.service.ContractService;
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
@RequestMapping(value="/contract")
public class ContractAction {


    Logger logger = LogManager.getLogger(ContractAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    private ContractService contractService;

    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("itsm/businessConfig/contract/list");
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
            page = contractService.loadList(param);
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
    @RequestMapping(value = {"/add/{selectId}/{id}", "/add/{selectId}"})
    public ModelAndView add(@PathVariable String selectId,@PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("itsm/businessConfig/contract/add");
        if (StringUtils.isEmpty(id)) {
            Map<String, Object> m=new HashMap<>();
            view.addObject("CONTRACT", m);
            view.addObject("treeId", selectId);
            return view;
        }
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.CONTRACTNAME,V.STATUS,V.SIGN_USER,V.SIGN_TIME,V.REMARK," +
                    "V.UPDATE_TIME,V.UPDATE_ID,V.CREATE_TIME,V.CREATE_ID,V.CONTRACTNO,V.CONTRACTDESCR,V.CONTRACTAMOUNT,V.CONTRACT_SDATE," +
                    "V.CONTRACT_EDATE,V.DEPT_ID,V.BESIGN_USER FROM BUSINESS_CONTRACT V WHERE V.ID=? ", id);
            //String sort=DBHelper.queryForScalar("SELECT MAX(SORT) FROM ACCOUNT ",String.class);
            //iew.addObject("sort",sort);
            view.addObject("CONTRACT", m);
            view.addObject("treeId", "0");
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
            List<Map<String, Object>> l = DBHelper.queryForList("SELECT V.ID,V.CONTRACTNAME,V.STATUS,V.SIGN_USER,V.SIGN_TIME,V.REMARK," +
                    "V.UPDATE_TIME,V.UPDATE_ID,V.CREATE_TIME,V.CREATE_ID,V.CONTRACTNO,V.CONTRACTDESCR,V.CONTRACTAMOUNT,V.CONTRACT_SDATE," +
                    "V.CONTRACT_EDATE,V.DEPT_ID,V.BESIGN_USER FROM BUSINESS_CONTRACT V V.ID=? ", id);
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
            contractService.save(param);
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
            contractService.del(param);
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
            contractService.realDel(param);
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
