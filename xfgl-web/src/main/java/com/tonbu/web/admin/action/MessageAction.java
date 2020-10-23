package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.web.admin.service.MessageService;
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

@RequestMapping("/message")
@Controller
public class MessageAction {

    Logger logger  = LogManager.getLogger(MessageAction.class);

    @Autowired
    HttpServletRequest request;

    @Autowired
    MessageService messageService;


    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("admin/manage/message/list");
        return view;
    }

    @RequestMapping(value = "/my")
    public ModelAndView myHome() {
        ModelAndView view = new ModelAndView("admin/manage/message/myList");
        return view;
    }


    @RequestMapping(value = {"/list"})
    @ResponseBody
    public Object list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = messageService.loadList(param);
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
    @RequestMapping(value = {"/add/{isMy}/{id}","/add/{id}", "/add"})
    public ModelAndView add(@PathVariable(required = false) String isMy, @PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/message/add");
        if (StringUtils.isEmpty(id)) {
            return view;
        }
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.TITLE,V.CONTENT,V.MESSAGE_TYPE,V.SEND_TIME,V.SEND_ID,V.STATUS,V.PROCESS_ID,V.TABLE_ID FROM MESSAGE V WHERE V.ID=? ", id);
            List<Map<String, Object>> l = DBHelper.queryForList("SELECT V.USER_ID FROM MESSAGE_USER V WHERE V.MESSAGE_ID = ? ", id);
            view.addObject("MESSAGE", m);
            view.addObject("id", id);
            if(l.size()>0){
                String ids  = "";
                for(Map<String,Object> map : l){
                    ids += ",";
                    ids += map.get("USER_ID");
                }
                view.addObject("receiver", ids.substring(1));
            }
            if(StringUtils.isNotEmpty(isMy)){
                messageService.update(id);
                view.addObject("isView", isMy);
            }
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
            List<Map<String, Object>> l = DBHelper.queryForList("SELECT V.ID,V.TITLE,V.CONTENT,V.MESSAGE_TYPE,V.SEND_TIME,V.SEND_ID,V.STATUS,V.PROCESS_ID,V.TABLE_ID FROM MESSAGE V WHERE V.ID=?", id);
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
    @RequestMapping(value = "/save")
    @ResponseBody
    public Object save() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            messageService.save(param);
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
            messageService.del(param);
            r.setMsg("删除成功");
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
    @RequestMapping(value = "/realDel")
    @ResponseBody
    public Object relDel() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            messageService.realDel(param);
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

    @RequestMapping(value = "/list/user")
    @ResponseBody
    public Object roleList() {
        ResultEntity r = new ResultEntity(1);
        List<?> list;
        try {
            list = messageService.loadUserList();
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
}
