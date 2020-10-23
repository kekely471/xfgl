package com.tonbu.web.admin.action;

import com.alibaba.fastjson.JSONArray;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.service.ConfigService;
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

@RequestMapping(value = "/config")
@Controller
public class ConfigAction {

    Logger logger = LogManager.getLogger(ConfigAction.class.getName());

    @Autowired
    HttpServletRequest request;


    @Autowired
    private ConfigService configService;

    /**
     * 点位配置管理页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/")
    public ModelAndView configManage() {
        ModelAndView view = new ModelAndView("admin/manage/config/list");
        return view;
    }

    /**
     * 获取所有配置项
     *
     * @return
     */
    @RequestMapping(value = "/getConfigList")
    @ResponseBody
    public Object getConfigList() {
        ResultEntity obj = new ResultEntity(1);
        obj.setDataset(DBHelper.queryForList("select * from b_config"));
        return obj;
    }

    /**
     * 保存所有配置项
     *
     * @return
     */
    @RequestMapping("newAddConfig")
    public Object newAddConfig(){
        ResultEntity obj = new ResultEntity(1);
        UserEntity u = CustomRealm.GetLoginUser();
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        JSONArray keyArray = JSONArray.parseArray(param.get("keys"));
        JSONArray codeArray = JSONArray.parseArray(param.get("codes"));
        JSONArray valueArray = JSONArray.parseArray(param.get("values"));
        JSONArray remarkArray = JSONArray.parseArray(param.get("remarks"));
        //首先删除数据库内所有数据
        DBHelper.execute("truncate table b_config");
        for(int i=0;i<keyArray.size();i++){
            StringBuffer sql = new StringBuffer("insert into b_config(id,key,value,create_id,create_time,update_id,update_time,code,remark)" +
                    "values(SEQ_CONFIG.Nextval,?,?,?,SYSDATE,?,SYSDATE,?,?)");
            DBHelper.execute(sql.toString(),keyArray.get(i),valueArray.get(i),u.getId(),u.getId(),codeArray.get(i),remarkArray.get(i));
        }
        return obj;
    }

    /**
     * 点位配置管理（新增、修改）页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/addOrUpdateConfigManage", "/addOrUpdateConfigManage/{id}"})
    public ModelAndView addOrUpdateConfigManage(@PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/config/add");
        if (!StringUtils.isEmpty(id)) {
            try {
                StringBuffer sql = new StringBuffer(" select ID,KEY,VALUE,CODE,REMARK from B_CONFIG  " +
                        "  where ID=? ");
                Map<String, Object> m = DBHelper.queryForMap(sql.toString(), id);
                view.addObject("CONFIG", m);
            } catch (ActionException ex) {
                throw ex;
            }
        }
        return view;
    }

    /**
     * 点位配置管理列表
     *
     * @return Object
     */
    @RequestMapping(value = "/configManageList")
    @ResponseBody
    public Object configManageList() {
        ResultEntity obj = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = configService.queryConfig(param);
            obj.setDataset(page);
            obj.setMsg("");
        } catch (RuntimeException ex) {
            obj.setCode(-1);
            obj.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return obj;
        }
    }

    /**
     * 点位配置删除（真删除）
     *
     * @return ResultEntity
     */
    @RequestMapping(value = "/relDeleteConfigByIdList")
    @ResponseBody
    public ResultEntity relDeleteConfigByIdList() {
        Map<String, String> param = RequestUtils.toMap(request);
        return configService.delConfig(param);
    }


    /**
     * 点位配置新增
     *
     * @return ResultEntity
     */
    @RequestMapping(value = "/addConfig")
    @ResponseBody
    public ResultEntity addConfig() {
        Map<String, String> param = RequestUtils.toMap(request);
        return configService.addConfig(param);
    }

    /**
     * 点位配置修改
     *
     * @return ResultEntity
     */
    @RequestMapping(value = "/updateConfig")
    @ResponseBody
    public ResultEntity updateConfig() {
        Map<String, String> param = RequestUtils.toMap(request);
        return configService.updateConfig(param);
    }
}
