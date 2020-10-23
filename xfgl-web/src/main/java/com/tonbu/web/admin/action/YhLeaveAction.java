package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.support.helper.JwtTokenHelper;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.pojo.RunningProcess;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/yhleave")
public class YhLeaveAction extends BaseAction {

    private static String path = "admin/manage/yhleave";

    Logger logger = LogManager.getLogger(YhLeaveAction.class.getName());

    @Autowired
    RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    HttpServletRequest request;
    @Autowired
    HistoryService historyService;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    @RequestMapping(value = "/")
    public ModelAndView home() {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/list");
        view.addObject("userId",userId);
        return view;
    }

    /**
     * 获取添加页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/add/{id}", "/add/"})
    public ModelAndView add(@PathVariable(required = false) String id) {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        Map<String, Object> usermap = getUserInfo(u.getUser_id());
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/add");
        view.addObject("QJMK", usermap);
        view.addObject("userId", userId);
        return view;
    }

    /**
     * 用户请假详情
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/view/{id}", "/view/"})
    public ModelAndView view(@PathVariable(required = false) String id) {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        Map<String, Object> usermap = getUserInfo(u.getUser_id());
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/view");
        view.addObject("QJMK", usermap);
        view.addObject("userId", userId);
        try {
            Map<String, Object> map = DBHelper.queryForMap(" select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,t.leave_days_true,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,tu.ss_id ,tu.NATIVE_PLACE,tu.rwny,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = tu.POLICERANK) as POLICERANK " +
                    " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC  where t.id=? and d.type_code='qjlx'  ",id);

            view.addObject("VIEW", map);
            view.addObject("id", id);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }

    //-----------------------我的草稿---------------------

    @RequestMapping(value = "draft/")
    public ModelAndView draft() {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/draft/list");
        view.addObject("userId",userId);
        return view;
    }

    /**
     * 获取添加页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/draft/add/{id}", "/draft/add/"})
    public ModelAndView draftadd(@PathVariable(required = false) String id) {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        Map<String, Object> usermap = getUserInfo(u.getUser_id());
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/draft/add");
        view.addObject("QJMK", usermap);
        view.addObject("userId", userId);
        try {
            Map<String, Object> map = DBHelper.queryForMap(" select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,t.spr,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi:ss') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi:ss') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi:ss') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,t.leave_days_true,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,tu.ss_id ,tu.NATIVE_PLACE,tu.rwny,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = tu.POLICERANK) as POLICERANK " +
                    " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC  where t.id=? and d.type_code='qjlx'  ",id);

            view.addObject("VIEW", map);
            view.addObject("id", id);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }

    /**
     * 删除数据
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/draft/del")
    @ResponseBody
    public Object del() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);

        try {
            String ids = param.get("ids");
            String[] id_s = ids.split("\\+");
            for (int i = 0; i < id_s.length; i++) {
                DBHelper.execute(" DELETE FROM TB_LEAVEAPPLY WHERE ID=?", id_s[i]);
            }
            r.setMsg("删除成功");
        }catch (JSONException e){
            r.setCode(-1);
            r.setMsg(e.getMessage());
        }catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            return r;
        }
    }


    //-----------------------我的待办---------------------

    @RequestMapping(value = "mission/")
    public ModelAndView mission() {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/mission/list");
        view.addObject("userId",userId);
        return view;
    }

    @RequestMapping(value = "/mission/list")
    @ResponseBody
    public Object list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            List<Map<String,Object>> results = new ArrayList<>();
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            Limit limit = new Limit(param);
            int rowcount = limit.getSize();
            int current = limit.getStart()-1;
            List<Task> tasks=taskService.createTaskQuery().taskCandidateOrAssigned(userEntity.getUser_id()).listPage(current, rowcount);
            for(Task task:tasks){
                String instanceid=task.getProcessInstanceId();
                ProcessInstance ins=runtimeService.createProcessInstanceQuery().processInstanceId(instanceid).singleResult();
                String businesskey=ins.getBusinessKey();

                Map<String,Object>  a = DBHelper.queryForMap("select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE ,tu.NATIVE_PLACE,tu.ss_id " +
                        " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC  where t.id=? and d.type_code='qjlx' and to_char(t.apply_time,'yyyy')=to_char(sysdate,'yyyy')  ",businesskey);
                if (!trimToEmpty(a.get("ss_id")).equals(userEntity.getUser_id())){
                    results.add(a);
                }

            }
            r.setDataset(results);
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
    @RequestMapping(value = {"/mission/add/{id}/{user_id}", "/mission/add/"})
    public ModelAndView missionadd(@PathVariable(required = false) String id,@PathVariable(required = false) String user_id) {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        Map<String, Object> usermap = getUserInfo(user_id);
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/mission/add");
        view.addObject("QJMK", usermap);
        view.addObject("userId", userId);
        try {
            Map<String, Object> map = DBHelper.queryForMap(" select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,t.spr,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi:ss') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi:ss') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi:ss') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,t.leave_days_true,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,tu.ss_id ,tu.NATIVE_PLACE,tu.rwny,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = tu.POLICERANK) as POLICERANK " +
                    " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC  where t.id=? and d.type_code='qjlx'  ",id);

            view.addObject("VIEW", map);
            view.addObject("id", id);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }

    /**
     * 查看历史意见
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/mission/cklsyj/{businessid}", "/mission/cklsyj/"})
    public ModelAndView wdlsyj(@PathVariable(required = false) String businessid) {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        Map<String, Object> usermap = getUserInfo(u.getUser_id());
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/mission/cklsyj");
        view.addObject("QJMK", usermap);
        view.addObject("userId", userId);
        try {
            Map<String, Object> map = DBHelper.queryForMap(" select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,t.leave_days_true,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,tu.ss_id ,tu.NATIVE_PLACE,tu.rwny,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = tu.POLICERANK) as POLICERANK " +
                    " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC  where t.id=? and d.type_code='qjlx'  ",businessid);

            view.addObject("VIEW", map);
            view.addObject("id", businessid);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }

    /**
     * 查看历史请假
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/mission/cklsqj/{businessid}/{user_id}", "/mission/cklsqj/"})
    public ModelAndView missioncklsqj(@PathVariable(required = false) String businessid,@PathVariable(required = false) String user_id) {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        Map<String, Object> usermap = getUserInfo(user_id);
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/mission/cklsqj");
        view.addObject("QJMK", usermap);
        view.addObject("userId", userId);
        try {
            view.addObject("businessid", businessid);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }



    //-----------------------我的已办---------------------

    @RequestMapping(value = "hdone/")
    public ModelAndView hdone() {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/hdone/list");
        view.addObject("userId",userId);
        return view;
    }

    @RequestMapping(value = "/hdone/list")
    @ResponseBody
    public Object hdonelist() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            List<Map<String,Object>> results = new ArrayList<>();
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            Limit limit = new Limit(param);
            int rowcount = limit.getSize();
            int current = limit.getStart()-1;
            List<RunningProcess> list = new ArrayList<RunningProcess>();

            List<HistoricTaskInstance> taskList = historyService.createHistoricTaskInstanceQuery()
                    .taskAssignee(userEntity.getUser_id())
                    .finished()
                    .listPage(current, rowcount);

            for (HistoricTaskInstance h : taskList) {
                RunningProcess process = new RunningProcess();
                Map<String,Object>  a = DBHelper.queryForMap(" select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,tu.ss_id ,tu.NATIVE_PLACE,tu.rwny,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = tu.POLICERANK) as POLICERANK " +
                        " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC  where t.process_instance_id=? and d.type_code='qjlx'  ",h.getProcessInstanceId());


                if (!trimToEmpty(a.get("ss_id")).equals(userEntity.getUser_id())){
                    results.add(a);
                }

            }
            r.setDataset(results);
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
     * 已办详情
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/hdone/view/{id}", "/hdone/view/"})
    public ModelAndView hdoneview(@PathVariable(required = false) String id) {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        Map<String, Object> usermap = getUserInfo(u.getUser_id());
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/hdone/view");
        view.addObject("QJMK", usermap);
        view.addObject("userId", userId);
        try {
            Map<String, Object> map = DBHelper.queryForMap(" select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,t.leave_days_true,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,tu.ss_id ,tu.NATIVE_PLACE,tu.rwny,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = tu.POLICERANK) as POLICERANK " +
                    " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC  where t.id=? and d.type_code='qjlx'  ",id);

            view.addObject("VIEW", map);
            view.addObject("id", id);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }


    /*                 销假列表                 */
    @RequestMapping(value = "/absence")
    public ModelAndView absencehome() {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/absence/list");
        view.addObject("userId",userId);
        return view;
    }

    /**
     * 获取添加页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/absence/add/{id}", "/absence/add/"})
    public ModelAndView absenceadd(@PathVariable(required = false) String id) {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        Map<String, Object> usermap = getUserInfo(u.getUser_id());
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ModelAndView view = new ModelAndView(path+"/absence/add");
        view.addObject("QJMK", usermap);
        view.addObject("userId", userId);
        try {
            Map<String, Object> map = DBHelper.queryForMap(" select t.id,t.process_instance_id,t.user_id,tu.name,t.leave_type,t.spr,d.name as leave_type_name,to_char(t.start_time,'yyyy-mm-dd hh24:mi:ss') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi:ss') as end_time,t.reason,to_char(t.apply_time,'yyyy-mm-dd hh24:mi:ss') as apply_time,to_char(t.reality_start_time,'yyyy-mm-dd hh24:mi') as reality_start_time,to_char(t.reality_end_time,'yyyy-mm-dd hh24:mi') as reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days,t.leave_days_true,dept.DEPTNAME as DWMC,tu.DWMC as DWMC_TRUE,tu.ss_id ,tu.NATIVE_PLACE,tu.rwny,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = tu.POLICERANK) as POLICERANK " +
                    " from TB_LEAVEAPPLY t left join tb_user tu on t.user_id=tu.ss_id  left join business_dict d  on d.value=t.leave_type  LEFT JOIN SS_DEPT dept  ON dept.id = tu.DWMC  where t.id=? and d.type_code='qjlx'  ",id);

            view.addObject("VIEW", map);
            view.addObject("id", id);
            return view;
        } catch (ActionException ex) {
            throw ex;
        }
    }







    /*         token           */

    @RequestMapping(value = "/token")
    @ResponseBody
    public Object token() {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
        ResultEntity r = new ResultEntity(1);

        try {
            r.setMsg(userId);
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
