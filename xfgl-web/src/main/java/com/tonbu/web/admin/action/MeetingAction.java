package com.tonbu.web.admin.action;

import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.web.admin.service.MeetingService;
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

@RequestMapping("/meeting")
@Controller
public class MeetingAction {
    Logger logger = LogManager.getLogger(MeetingAction.class);

    @Autowired
    HttpServletRequest request;


    @Autowired
    MeetingService meetingService;

    /**
     * 获取会议管理页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/meeting")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("admin/manage/meeting/list");
        return view;
    }

    /**
     * 获取我的会议页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/my_meeting")
    public ModelAndView myHome() {
        ModelAndView view = new ModelAndView("admin/manage/meeting/myList");
        return view;
    }

    /**
     * 获取部门人员树页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/person/{id}")
    public ModelAndView person(@PathVariable String id) {
        ModelAndView view = new ModelAndView("admin/manage/meeting/person");
        view.addObject("id",id);
        if(id.equals("1")){
            view.setViewName("admin/manage/meeting/joinPerson");
            return view;
        }
        return view;
    }

    /**
     * 查询会议列表
     *
     * @return ResultEntity
     */
    @RequestMapping(value = {"/list"})
    @ResponseBody
    public Object list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = meetingService.loadList(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
            throw ex;
        } finally {
            return r;
        }
    }

    /**
     * 查询我的会议列表
     *
     * @return ResultEntity
     */
    @RequestMapping(value = {"/mylist"})
    @ResponseBody
    public Object mylist() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = meetingService.loadMyList(param);
            r.setDataset(page);
            r.setMsg("");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
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
    @RequestMapping(value = {"/add/{id}", "/add"})
    public ModelAndView add(@PathVariable(required = false) String id,@PathVariable(required = false) String myView) {
        ModelAndView view = new ModelAndView("admin/manage/meeting/add");
        if (StringUtils.isEmpty(id)) {
            return view;
        }
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.TITLE,V.CONTENT,V.MEETING_TYPE,TO_CHAR(V.MEETING_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') MEETING_TIME,V.CREATE_ID,V.MEETING_ADDRESS,V.DEPT_ID,( SELECT USERNAME FROM SS_USER SU WHERE SU.ID = V.CREATE_ID ) AS HOST_USERNAME,(SELECT WM_CONCAT(TO_CHAR(SU.USERNAME)) FROM  MEETING_USER MU LEFT JOIN SS_USER SU ON SU.ID=MU.USER_ID WHERE MU.MEETING_ID = V.ID) AS JOIN_NAME FROM MEETING V WHERE V.ID=? ", id);
            List<Map<String, Object>> l = DBHelper.queryForList("SELECT V.USER_ID FROM MEETING_USER V WHERE V.MEETING_ID = ? ", id);
            view.addObject("MEETING", m);
            view.addObject("id", id);
            if(l.size()>0){
                String ids  = "";
                for(Map<String,Object> map : l){
                    ids += ",";
                    ids += map.get("USER_ID");
                }
                view.addObject("JOIN_IDS", ids.substring(1));
            }
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
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            meetingService.save(param);
            r.setMsg("操作成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
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
            meetingService.realDel(param);
            r.setMsg("彻底删除成功！");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
            throw ex;
        } finally {
            return r;
        }
    }

    /**
     * 修改查看状态
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/update/{id}")
    @ResponseBody
    public ModelAndView update(@PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/meeting/view");
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        param.put("id",id);
        try {
            Map<String, Object> m = DBHelper.queryForMap("SELECT V.ID,V.TITLE,V.CONTENT,V.MEETING_TYPE,TO_CHAR(V.MEETING_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') MEETING_TIME,V.CREATE_ID,V.MEETING_ADDRESS,V.DEPT_ID,( SELECT USERNAME FROM SS_USER SU WHERE SU.ID = V.CREATE_ID ) AS HOST_USERNAME,(SELECT WM_CONCAT(TO_CHAR(SU.USERNAME)) FROM  MEETING_USER MU LEFT JOIN SS_USER SU ON SU.ID=MU.USER_ID WHERE MU.MEETING_ID = V.ID) AS JOIN_NAME FROM MEETING V WHERE V.ID=? ", id);
            List<Map<String, Object>> l = DBHelper.queryForList("SELECT V.USER_ID FROM MEETING_USER V WHERE V.MEETING_ID = ? ", id);
            view.addObject("MEETING", m);
            view.addObject("id", id);
            if(l.size()>0){
                String ids  = "";
                for(Map<String,Object> map : l){
                    ids += ",";
                    ids += map.get("USER_ID");
                }
                view.addObject("joiner", ids.substring(1));
            }
            meetingService.update(param);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }

    }
}
