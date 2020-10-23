package com.tonbu.web.app.itsm.action;

import com.tonbu.framework.Constants;
import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.Limit;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.service.FileService;
import com.tonbu.framework.util.JsonUtils;
import com.tonbu.framework.util.RandomUtils;
import com.tonbu.framework.util.RequestUtils;
import com.tonbu.support.helper.JwtTokenHelper;
import com.tonbu.support.redis.service.RedisService;
import com.tonbu.support.service.AccountService;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.support.util.CryptUtils;
import com.tonbu.web.activiti.common.DataGrid;
import com.tonbu.web.admin.common.HttpUtil;
import com.tonbu.web.admin.common.UserUtils;
import com.tonbu.web.admin.pojo.HistoryProcess;
import com.tonbu.web.admin.pojo.LeaveApply;
import com.tonbu.web.admin.pojo.RunningProcess;
import com.tonbu.web.admin.service.*;
import com.tonbu.web.admin.service.important.ImportantService;
import com.tonbu.web.admin.service.important.RyfxMoblieService;
import com.tonbu.web.app.itsm.service.GzqService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Controller
@RequestMapping(value = "/api/v1/question/")
@Api(tags = "问题发布")
public class ApiQuestionAction {

    Logger logger = LogManager.getLogger(ApiQuestionAction.class.getName());

    @Autowired
    HttpServletRequest request;


    @Autowired
    BgtService bgtService;

    @Autowired
    JhdyService jhdyService;


    /**
     * 曝光台--问题发布
     *
     * @return
     */
   /* @ApiOperation(value = "问题发布list查询")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "key_name", value = "key_name", required = false),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true)

    })
    @GetMapping(value = "/user/bgt/wtfb/list")
    @ResponseBody
    public Object bgtWtfb() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId = user.get("SS_ID").toString();
        param.put("userId", userId);
        Page<?> page;
        try {
            page = bgtService.loadList(param);
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

    *//**
     *问题发布--新增保存
     *   String id = param.get("id");
     *         String title = param.get("bt");
     *         String describe = param.get("ms");
     *         String url = param.get("url");
     *         String details = param.get("editor");
     *         String file_id = param.get("avatar");
     *         String tupian = param.get("userFileIdAVATAR_filepath");
     *         String type = param.get("type");
     *         String status = param.get("status") == null ? "0" : "1";
     *         String externally = param.get("externally") == null ? "0" : "1";
     * @return
     *//*

    @ApiOperation(value = "问题发布新增保存")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "id", value = "id", required = false),
            @ApiImplicitParam(dataType = "string", name = "bt", value = "bt", required = true),
            @ApiImplicitParam(dataType = "string", name = "ms", value = "ms", required = true),
            @ApiImplicitParam(dataType = "string", name = "url", value = "url", required = true),
            @ApiImplicitParam(dataType = "string", name = "editor", value = "editor", required = true),
            @ApiImplicitParam(dataType = "string", name = "avatar", value = "avatar", required = true),
            @ApiImplicitParam(dataType = "string", name = "userFileIdAVATAR_filepath", value = "userFileIdAVATAR_filepath", required = true),
            @ApiImplicitParam(dataType = "string", name = "type", value = "type", required = true),
            @ApiImplicitParam(dataType = "string", name = "是否外链", value = "externally", required = true),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true)


    })
    @PostMapping(value = "/user/bgt/wtfb/save")
    @ResponseBody
    public Object saveWtfb() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            String userId = user.get("SS_ID").toString();
            param.put("userId", userId);
            if (user == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            bgtService.save(param);
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

    *//**
     * 问题发布删除
     *//*
    @ApiOperation(value = "问题发布删除")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "ids", value = "ids", required = true),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true)

    })
    @DeleteMapping("/user/bgt/wtfb/del")
    @ResponseBody
    public Object delWtfb() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            String userId = user.get("SS_ID").toString();
            param.put("userId", userId);
            if (user == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            bgtService.del(param);
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

    *//**
     * 问题发布详情
     *
     * @return
     *//*
    @ApiOperation(value = "问题发布详情")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "id", value = "id", required = true),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true)

    })
    @GetMapping(value = "/user/bgt/wtfb/get")
    @ResponseBody
    public Object getWtfb(String id) {
        ResultEntity r = new ResultEntity(1);
        try {
            //整合参数
            Map<String, String> param = RequestUtils.toMap(request);
            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            String userId = user.get("SS_ID").toString();
            param.put("userId", userId);
            if (user == null) {
                r.setCode(0);
                r.setMsg("用户不存在！");
                return r;
            }
            if (id == null) {
                r.setCode(0);
                r.setMsg("id不存在！");
                return r;
            }
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> m = bgtService.getPageInformationById(id);
            list.add(m);
            r.setDataset(list);
            r.setMsg("操作成功");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            throw ex;
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }*/


    /**
     * 曝光台---问题上报
     *
     * @return
     */
    @ApiOperation(value = "问题上报list")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "key_name", value = "key_name", required = false),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true)

    })
    @PostMapping(value = "/user/bgt/wtsb/list")
    @ResponseBody
    public Object bgtWtsb() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId = user.get("SS_ID").toString();
        param.put("userId", userId);
        Page<?> page;
        try {
            page = bgtService.wtsbList(param);
            r.setDataset(page);
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
     *问题上报新增保存
     *   String id = param.get("id");
     *         String title = param.get("title");
     *         String describe = param.get("describe");
     *         String ss_id = u.getUser_id();
     *         String status = param.get("status");
     * @return
     */

    @ApiOperation(value = "问题上报新增保存")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "id", value = "id", required = false),
            @ApiImplicitParam(dataType = "string", name = "title", value = "title", required = true),
            @ApiImplicitParam(dataType = "string", name = "describe", value = "describe", required = true),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true),
            @ApiImplicitParam(dataType = "string", name = "status", value = "status", required = true),

    })
    @PostMapping(value = "/user/bgt/wtsb/save")
    @ResponseBody
    public Object saveWtsb() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            String userId = user.get("SS_ID").toString();
            param.put("userId", userId);
            if (userId == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            bgtService.bc(param);
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



    /**
     * 问题上报删除
     */
    @ApiOperation(value = "问题上报删除")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "ids", value = "ids", required = true),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true)

    })
    @PostMapping("/user/bgt/wtsb/del")
    @ResponseBody
    public Object delWtsb() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            String userId = user.get("SS_ID").toString();
            param.put("userId", userId);
            if (user == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            bgtService.wtsb(param);
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



    /**
     * 问题上报详情
     *
     * @return
     */
    @ApiOperation(value = "问题上报详情")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "id", value = "id", required = true),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true)

    })
    @PostMapping(value = "/user/wtsb/add/{id}")
    @ResponseBody
    public Object addWtsb(@PathVariable(required = false) String id) {
        ResultEntity r = new ResultEntity(1);
        try {
            //整合参数
            Map<String, String> param = RequestUtils.toMap(request);
           /* UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            param.put("id",id);
            param.put("userId", userEntity.getUser_id());*/
            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            String userId = user.get("SS_ID").toString();
            param.put("id",id);
            param.put("userId", userId);
            Page<?> getwtsb = bgtService.getwtsb(param);
            r.setDataset(getwtsb);
            r.setMsg("操作成功");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            throw ex;
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
     * 解惑答疑list查询
     *
     * @return
     */
   /* @ApiOperation(value = "解惑答疑list")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "key_name", value = "key_name", required = false),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true)

    })
    @PostMapping(value = "/user/jhdy/wtck/list")
    @ResponseBody
    public Object jhdywtck() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
       *//* UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
        param.put("userId", userEntity.getUser_id());*//*
        Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId = user.get("SS_ID").toString();
        param.put("userId", userId);
        Page<?> page;
        try {
            page = jhdyService.wtckList(param);
            r.setDataset(page);
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


    *//**
     * 解惑答疑问题查看详情
     *
     * @return
     *//*
    @ApiOperation(value = "解惑答疑问题查看详情")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "id", value = "id", required = true),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true)

    })
    @PostMapping(value = "/user/jhdy/wtck/add/{id}")
    @ResponseBody
    public Object addJhdywtck(@PathVariable(required = false) String id) {
        ResultEntity r = new ResultEntity(1);
        try {
            //整合参数
            Map<String, String> param = RequestUtils.toMap(request);
          *//*  UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            param.put("id",id);
            param.put("userId", userEntity.getUser_id());*//*
            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            String userId = user.get("SS_ID").toString();
            param.put("userId", userId);
            param.put("id",id);
            Page<?> getwtsb = jhdyService.getwtck(param);
            r.setDataset(getwtsb);
            r.setMsg("操作成功");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            throw ex;
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }*/

    /**
     * 解惑答疑问题管理list
     *
     * @return
     */
    @ApiOperation(value = "解惑答疑问题管理list")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "key_name", value = "key_name", required = false),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true),
            @ApiImplicitParam(dataType = "string", name = "status", value = "status", required = true)

    })
    @PostMapping(value = "/user/jhdy/wtgl/list")
    @ResponseBody
    public Object jhdywtgl() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
       /* if(trimToEmpty(u.getJob()).equals("")||trimToEmpty(u.getJob()).equals("4")||trimToEmpty(u.getJob()).equals("5")||trimToEmpty(u.getJob()).equals("6")){
            user.put("job", "一线消防员");
        }else{
            user.put("job", "管理员");
        }*/

        String userId = user.get("SS_ID").toString();
        String dwmc = DBHelper.queryForScalar("select dwmc from tb_user  where ss_id=? ",String.class,userId);
        String job =null==user.get("JOB") ? "": user.get("JOB").toString();
        param.put("job",job);
        param.put("userId", userId);
        param.put("dwmc",dwmc);
        Page<?> page;
        try {
            page = jhdyService.wtglList2(param);
            ((List<Map<String,String>>)page.getRows()).stream().forEach(item->{
                if (job.equals("0")){
                    item.put("EDIT","1");
                }else{
                    item.put("EDIT","0");
                }
            });
            r.setDataset(page);
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
     *解惑答疑问题管理新增保存
     *   String id = param.get("id");
     *         String title = param.get("title");
     *         String describe = param.get("describe");
     *         String ss_id = u.getUser_id();
     *         String status = param.get("status");
     * @return
     */

    @ApiOperation(value = "解惑答疑问题管理新增保存")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "id", value = "id", required = false),
            @ApiImplicitParam(dataType = "string", name = "title", value = "title", required = true),
            @ApiImplicitParam(dataType = "string", name = "describe", value = "describe", required = true),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true),

    })
    @PostMapping(value = "/user/jhdy/wtgl/save")
    @ResponseBody
    public Object saveWtgl() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            String userId = user.get("SS_ID").toString();
            param.put("userId", userId);

            if (userId == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            jhdyService.wtglsave(param);
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

    /**
     * 解惑答疑问题管理 删除
     */
    @ApiOperation(value = "解惑答疑问题管理测试删除")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "ids", value = "ids", required = true),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true)

    })
    @PostMapping("/user/jhdy/wtgl/del")
    @ResponseBody
    public Object delWtgl() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            String userId = user.get("SS_ID").toString();
            param.put("userId", userId);
            if (user == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            jhdyService.wtgldel(param);
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

    /**
     * 解惑答疑问题管理详情
     *
     * @return
     */
   /* @ApiOperation(value = "解惑答疑问题管理详情")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "id", value = "id", required = true),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true)

    })
    @PostMapping(value = "/user/jhdy/wtgl/add/{id}")
    @ResponseBody
    public Object addJhdywtgl(@PathVariable(required = false) String id) {
        ResultEntity r = new ResultEntity(1);
        try {
            //整合参数
            Map<String, String> param = RequestUtils.toMap(request);
            *//*UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            param.put("id",id);
            param.put("userId", userEntity.getUser_id());*//*
            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            String userId = user.get("SS_ID").toString();
            param.put("userId", userId);
            param.put("id",id);
            Page<?> getwtsb = jhdyService.getwtgl(param);
            r.setDataset(getwtsb);
            r.setMsg("操作成功");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            throw ex;
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }*/

    /**
     * 解惑答疑问题分析list
     *
     * @return
     */
  /*  @ApiOperation(value = "解惑答疑问题分析list")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "key_name", value = "key_name", required = false),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true),
            @ApiImplicitParam(dataType = "string", name = "status", value = "status", required = true)

    })
    @PostMapping(value = "/user/jhdy/wtfx/list")
    @ResponseBody
    public Object jhdywtfx() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId = user.get("SS_ID").toString();
        param.put("userId", userId);
        Page<?> page;
        try {
            page = jhdyService.wtfxList2(param);
            r.setDataset(page);
            r.setMsg("操作成功");
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }*/


    /**
     * 解惑答疑问题详情
     *
     * @return
     */
    @ApiOperation(value = "解惑答疑问题分析详情")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "id", value = "id", required = true),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true)

    })
    @PostMapping(value = "/user/jhdy/wtfx/add")
    @ResponseBody
    public Object addJhdywtfx(String id) {
        ResultEntity r = new ResultEntity(1);
        try {
            //整合参数
            Map<String, String> param = RequestUtils.toMap(request);
            /*UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            param.put("id",id);
            param.put("userId", userEntity.getUser_id());*/
            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            String userId = user.get("SS_ID").toString();
            param.put("userId", userId);
            param.put("id",id);
            Page<?> getwtsb = jhdyService.getwtfx(param);
            r.setDataset(getwtsb);
            r.setMsg("操作成功");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            throw ex;
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
     *解惑答疑问题分析回复问题
     *   String id = param.get("id");
     *         String title = param.get("title");
     *         String describe = param.get("describe");
     *         String ss_id = u.getUser_id();
     *         String status = param.get("status");
     * @return
     */

    @ApiOperation(value = "解惑答疑问题分析回复问题")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "id", value = "id", required = true),
            @ApiImplicitParam(dataType = "string", name = "fxdescribe", value = "fxdescribe", required = true),
            @ApiImplicitParam(dataType = "string", name = "sfgk", value = "sfgk", required = true),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true),

    })
    @PostMapping(value = "/user/jhdy/wtfx/save")
    @ResponseBody
    public Object saveWtfx() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            String userId = user.get("SS_ID").toString();
            param.put("userId", userId);

            if (userId == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            jhdyService.wtfxsave(param);
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