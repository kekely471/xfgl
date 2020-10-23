package com.tonbu.web.admin.action.important;

import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.web.admin.common.UserUtils;
import com.tonbu.web.admin.service.CommonService;
import com.tonbu.web.admin.service.important.ImportantService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 要事登记手机端接口
 */
@RequestMapping(value = "/importantMobile")
@Controller
public class ImportantMobileAction {

    Logger logger = LogManager.getLogger(ImportantMobileAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    private ImportantService importantService;

    @Autowired
    private CommonService commonService;

    //手机端h5页面
    public static final String VIEW_DIR = "important/mobile/";





    /**
     * to添加页面
     * @param id
     * @return
     */
    @RequestMapping(value = {"/add/{id}", "/add"})
    public ModelAndView add(@PathVariable(required = false) String id ,String userId ) {
        ModelAndView view = new ModelAndView();
        view.addObject("userId",userId); //用户token传给页面
        view.setViewName(VIEW_DIR+"important_add");
        /*加载对应的请假外出人数反回给页面*/
        if (StringUtils.isEmpty(id)) {
            return view;

        }else{//修改
            StringBuffer sql = new StringBuffer();
            sql.append("select t.ID as id,t.CREATE_USER as userId ,a.USERNAME as userName ,t.IMPORTANT_TYPE as importantType,t.IMPORTANT_CONTENT as importantContent,t.AUDIT_USER as auditUserId,b.USERNAME as auditUserName,t.AUDIT_OPINION as auditOpinion, ")
                    .append("t.REIGN_NUMBER as reignNumber,t.LEAVE_NUMBER as leaveNumber,t.GO_HOME_NUMBER as goHomeNumber,t.STATUS as status, ")
                    .append(" TO_CHAR(t.CREATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  createTime, ")
                    .append(" TO_CHAR(t.AUDIT_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  auditTime ,")
                    .append(" zd.Name as typeName " )
                    .append(" from TB_IMPORTANT t ")
                    .append(" left join SS_USER a on a.ID = t.CREATE_USER ")
                    .append(" left join SS_USER b on b.ID = t.AUDIT_USER ")
                    .append(" left join BUSINESS_DICT zd on zd.ID= t.IMPORTANT_TYPE ");
            sql.append("where t.id =?");
            Map<String,Object> important= DBHelper.queryForMap(sql.toString(),id);
            view.addObject("id", id);
            view.addObject("important", important);
            String status = important.get("STATUS").toString();
            if (!"1".equals(status)){
                view.addObject("flag","1");
            }else {
                view.addObject("flag","2");
            }
            return view;
        }

    }

    /**
     * to要是登记办理
     * @return
     */
    @RequestMapping(value = "/toAuditImportant/{id}")
    public ModelAndView toAuditImportant(@PathVariable(required = true) String id,String userId) {

        ModelAndView view = new ModelAndView(VIEW_DIR +"audit_important");

        view.addObject("userId",userId);

        StringBuffer sql = new StringBuffer();
        sql.append("select t.ID as id,t.CREATE_USER as userId ,a.USERNAME as userName ,t.IMPORTANT_TYPE as importantType,t.IMPORTANT_CONTENT as importantContent,t.AUDIT_USER as auditUserId,b.USERNAME as auditUserName,t.AUDIT_OPINION as auditOpinion, ")
                .append("t.REIGN_NUMBER as reignNumber,t.LEAVE_NUMBER as leaveNumber,t.GO_HOME_NUMBER as goHomeNumber,t.STATUS as status, ")
                .append(" TO_CHAR(t.CREATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  createTime, ")
                .append(" TO_CHAR(t.AUDIT_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  auditTime ,")
                .append(" zd.Name as typeName " )
                .append(" from TB_IMPORTANT t ")
                .append(" left join SS_USER a on a.ID = t.CREATE_USER ")
                .append(" left join SS_USER b on b.ID = t.AUDIT_USER ")
                .append(" left join BUSINESS_DICT zd on zd.ID= t.IMPORTANT_TYPE ");
        sql.append("where t.id =?");
        Map<String,Object> important= DBHelper.queryForMap(sql.toString(),id);

        view.addObject("important",important);//要事登记信息
        view.addObject("id",id);//要事登记信息
        return view;
    }


    /**
     * to要是登记列表
     * @return
     */
    @RequestMapping(value = "/listIndex")
    public ModelAndView importantIndex() {
        ModelAndView view = new ModelAndView(VIEW_DIR +"important_list");
        Map<String, String> param = RequestUtils.toMap(request);

        String userId = param.get("userId");//获取用户token

        Map<String,Object> user = UserUtils.getUserByToken(userId);
        String job = null;
        if (user != null ){
            job = (String) user.get("job");
            if (StringUtils.isNotBlank(job)){
                view.addObject("flag","1");//职位领导
            }else {
                view.addObject("flag",0);//普通员工
            }
            view.addObject("userId",userId);
        }
        return view;
    }
    
    /**
     * 要是登记列表
     * @return
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String,Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId =user.get("SS_ID").toString();
        param.put("userId",userId);
        Page<?> page;
        try {
            page = importantService.findImportantMobilePage(param);
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
     * 要事登记详情页面
     */
    @RequestMapping(value = "/toViewImportant/{id}")
    public ModelAndView viewImportant(@PathVariable(required = true) String id) {
        ModelAndView view = new ModelAndView(VIEW_DIR+"important_details");
        StringBuffer sql = new StringBuffer();
        sql.append("select t.ID as id,t.CREATE_USER as userId ,a.USERNAME as userName ,t.IMPORTANT_TYPE as importantType,t.IMPORTANT_CONTENT as importantContent,t.AUDIT_USER as auditUserId,b.USERNAME as auditUserName,t.AUDIT_OPINION as auditOpinion, ")
                .append("t.REIGN_NUMBER as reignNumber,t.LEAVE_NUMBER as leaveNumber,t.GO_HOME_NUMBER as goHomeNumber,t.STATUS as status, ")
                .append(" TO_CHAR(t.CREATE_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  createTime, ")
                .append(" TO_CHAR(t.AUDIT_TIME,'" + Constants.ORACLE_FORMATDATE_FULL + "') as  auditTime ,")
                .append(" zd.Name as typeName " )
                .append(" from TB_IMPORTANT t ")
                .append(" left join SS_USER a on a.ID = t.CREATE_USER ")
                .append(" left join SS_USER b on b.ID = t.AUDIT_USER ")
                .append(" left join BUSINESS_DICT zd on zd.ID= t.IMPORTANT_TYPE ");
        sql.append("where t.id =?");
        Map<String,Object> important= DBHelper.queryForMap(sql.toString(),id);

        view.addObject("important",important);//要事登记信息

        StringBuffer sql1 = new StringBuffer();
        sql1.append("select t.AUDIT_OPINION,t.AUDIT_DATE,t.AUDIT_USERID,a.USERNAME ,")
                .append(" case when t.AUDIT_STATUS=3 then '通过'when t.AUDIT_STATUS=4 then '已退回' else '已归档' end AUDIT_STATUS  ")
                .append(" from  TB_AUDIT_IMPORTANT t ")
                .append(" LEFT JOIN SS_USER a ON a.id = t.AUDIT_USERID")
                .append(" where t.TB_IMPORTANT_ID = ?");
        List<Map<String,Object>> auditList = DBHelper.queryForList(sql1.toString(),id);//审核记录
        view.addObject("auditList",auditList);
        return view;
    }

    /**
     * 办理审核、退回要事登记
     */
    @RequestMapping(value = "/auditImportant")
    @ResponseBody
    public Object auditImportant() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            importantService.auditImportant(param);
            r.setMsg("操作成功");
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
     * 保存、更新要事登记记录DESCRIBE
     */
    @RequestMapping(value = "/saveOrUpdate" ,method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public Object saveOrUpdate() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            importantService.saveOrUpdate(param);
            r.setMsg("操作成功");
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
     * to要事登记编辑页面
     * @return
     */
   @RequestMapping(value = "/editImportant")
    public ModelAndView editImportant() {
        ModelAndView view = new ModelAndView(VIEW_DIR+"editImportant");
        return view;
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
            importantService.del(param);
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
     * 获取站长人员列表
     */
    @RequestMapping(value = "/findAuditUsers")
    @ResponseBody
    public Object findAuditUsers() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            List<Map<String,Object>> result = importantService.findAuditUsers(param);
            r.setDataset(result);
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


}
