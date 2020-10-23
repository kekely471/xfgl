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
import com.tonbu.web.admin.action.BaseAction;
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
@RequestMapping(value = "/api/v1")
@Api(tags = "手机端接口对接")
public class ApiAction extends BaseAction {

    Logger logger = LogManager.getLogger(ApiAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    JwtTokenHelper jwtTokenHelper;
    @Autowired
    LbtService lbtService;
    @Autowired
    RyfxMoblieService ryfxMoblieService;
    @Autowired
    XgmmService xgmmService;
    @Autowired
    FileService fileService;
    @Autowired
    GzqService gzqService;
    @Autowired
    AccountService accountService;
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    HistoryService historyService;
    @Autowired
    YdxfService ydxfService;
    @Autowired
    CommonService commonService;
    @Autowired
    RepositoryService rep;
    @Autowired
    DqService dqService;
    @Autowired
    XxfbService xxfbService;
    @Autowired
    ZbztService zbztService;

    @Autowired
    LeaveService leaveService;

    @Autowired
    ImportantService importantService;

    @Autowired
    BgtService bgtService;

    @Value("${file.filter.url}")
    private String fileFilterUrl;


    @Value("${server.file.root}")
    private String rootPath; //root 名称

    @Autowired
    RedisService redisService;

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    private static final String SYMBOLS = "0123456789"; // 随机数数字
    private static final Random RANDOM = new SecureRandom();//完全随机数
    /**
     * 用户登录接口
     *
     * @return
     */
    @ApiOperation(value = "登录")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "username", value = "username", required = true),
            @ApiImplicitParam(dataType = "string", name = "password", value = "password", required = true)
    })
    @PostMapping(value = "/user/login")
    @ResponseBody
    public Object userLogin() {
        ResultEntity r = new ResultEntity();
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            String username = param.get("username");
            String password = param.get("password");
            Map<String, Object> map = new HashMap<>();

            UserEntity u = accountService.findByloginname(username.trim());
            if (u == null) {
                r.setCode(-1);
                r.setMsg("登录失败，账号不存在或未关联系统用户！");
                return r;
            }

            String oldpwd = CryptUtils.md5(password, u.getSalt());
            if (!StringUtils.equals(u.getPassword(), oldpwd)) {
                r.setCode(-1);
                r.setMsg("登录失败，密码不正确！");
                return r;
            }

            loginLog(u.getId(), u.getLoginname(), u.getUsername(), 0, 1, request.getRemoteAddr(), param.get("deviceId"));
            String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
            List<Map<String, Object>> roles = DBHelper.queryForList(" SELECT R.ID AS ROLEID,R.ROLENAME,R.JSBS FROM  ACCOUNT_ROLE AR,SS_ROLE  R  WHERE AR.ROLE_ID = R.ID AND AR.ACCOUNT_ID = ?", u.getId());
            System.out.println(u.getId());
            if (roles.size() == 0 || roles == null) {
                r.setCode(-1);
                r.setMsg("登录失败，请分配角色！");
                return r;
            }

            String user_id = DBHelper.queryForScalar("select t.user_id from ACCOUNT_USER t where t.account_id = ?", String.class, u.getId());
//            System.out.println(u.getId());
            if (user_id == null || StringUtils.isBlank(user_id)) {
                r.setCode(-1);
                r.setMsg("登录失败，用户不存在！");
                return r;
            } else {
                map = DBHelper.queryForMap("select * from ss_user where id = ?", user_id);
                if (map == null || map.size() == 0) {
                    r.setCode(-1);
                    r.setMsg("登录失败，用户不存在！");
                    return r;
                }
            }

            //绑定设备
//            accountService.saveAccountDevice(u,param);

            Map<String, Object> user = new HashMap<>();
            user.put("user_id", userId);
            user.put("id", u.getId());
            user.put("ss_id", trimToEmpty(map.get("ID")));
            user.put("username", u.getUsername());
            user.put("deptname", u.getDeptname());
            user.put("loginname", u.getLoginname());
/*            if(trimToEmpty(u.getJob()).equals("")||trimToEmpty(u.getJob()).equals("4")||trimToEmpty(u.getJob()).equals("5")||trimToEmpty(u.getJob()).equals("6")){
                user.put("job", "一线消防员");
            }else if(trimToEmpty(u.getJob()).equals("0")){
                user.put("job", "队务科长");
            }else{
                user.put("job", "管理员");

            }*/
            user.put("job", u.getJob());

            user.put("jobname", u.getJobname());
//            user.put("roles",roles);
            //若有初始密码相同则提示
            String initPwd = CryptUtils.md5(Constants.INIT_PASSWORD, u.getSalt());
            if (initPwd.equals(u.getPassword())) {
                user.put("updatepwd", "1");
            } else {
                user.put("updatepwd", "0");
            }
            DBHelper.execute(" UPDATE ACCOUNT SET LAST_LOGIN_TIME=SYSDATE WHERE LOGINNAME=? ", username);
            List<Object> l = new ArrayList<Object>();
            l.add(user);
            r.setCode(1);
            r.setDataset(l);
            r.setMsg("登陆成功！");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
        } catch (Exception exception) {
            logger.error(exception.getMessage(), exception);
            r.setMsg("登录失败，发生未知错误：");
            r.setCode(-1);
        } finally {
            return r;
        }
    }


    private static String[] model_name = {"登录手机端", "登出手机端"};


    /**
     * 写登录登出记录
     *
     * @param user_id
     * @param index   0:登录(客户端) 1:下班 2：注销 3:登录管理端 4:登出管理端
     */
    public void loginLog(String user_id, String loginname, String username, int index, int loginstatus, String host, String remark) {
        try {
            DBHelper.execute("INSERT INTO SS_LOG_LOGIN (ID,USER_ID,LOGINNAME,USERNAME,LOGINTIME,TYPE,LOGINIP,REMARK,STATUS) VALUES (SEQ_SS_LOG.NEXTVAL,?,?,?,SYSDATE,?,?,?,?)",
                    user_id, loginname, username, model_name[index], host, remark, loginstatus);
        } catch (Exception exception) {
            logger.error("登录日志写入异常：" + exception);
        }
    }

    /**
     * 密码修改
     *
     * @param userId
     * @param oldpwd
     * @param newpwd
     * @param newpwd2
     */
    @RequestMapping(value = "/user/pwd/update", method = RequestMethod.POST)
    @ResponseBody
    public Object updateUserPwd(@RequestParam(value = "userId") String userId,
                                @RequestParam(value = "oldpwd") String oldpwd,
                                @RequestParam(value = "newpwd") String newpwd,
                                @RequestParam(value = "newpwd2") String newpwd2) {
        ResultEntity r = new ResultEntity(1);
        try {
            UserEntity userEntity = CustomRealm.GetLoginUser(userId);
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            String oldpassword = CryptUtils.md5(oldpwd, userEntity.getSalt());
            if (!StringUtils.equals(userEntity.getPassword(), oldpassword)) {
                throw new JSONException("旧密码不正确！");
            }
            if (!StringUtils.equals(newpwd, newpwd2)) {
                throw new JSONException("两次输入的新密码不一致！");
            }
            String regex = "(?=.*[0-9])(?=.*[a-zA-Z]).{8,20}";
            if (!newpwd.matches(regex)) {
                throw new JSONException("密码必须包含字母和数字，长度8-20位!");
            }
            String npwd = CryptUtils.md5(newpwd, userEntity.getSalt());
            DBHelper.update("UPDATE ACCOUNT SET PASSWORD=? WHERE ID=? ", npwd, userEntity.getId());
            r.setMsg("密码修改成功！");
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
     * 密码重置
     *
     * @param userId
     */
    @RequestMapping(value = "/user/pwd/reset")
    @ResponseBody
    public Object resetpassword(@RequestParam(value = "userId") String userId) {
        ResultEntity r = new ResultEntity(1);
        try {
            UserEntity userEntity = CustomRealm.GetLoginUser(userId);
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            DBHelper.update("UPDATE ACCOUNT SET PASSWORD=?  WHERE ID=? ", 666666, userEntity.getId());
            r.setMsg("密码重置成功！");
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
     * 获取最新apk版本
     *
     * @return
     */
    @RequestMapping(value = "/version/Lastest")
    @ResponseBody
    public Object getLastestVersion(@RequestParam(value = "platform", required = true) String platform) {
        ResultEntity r = new ResultEntity(1);
        try {
            //整合参数
            Map<String, String> param = RequestUtils.toMap(request);
            List list = DBHelper.queryForList("SELECT * FROM (SELECT V.ID,V.VERSIONNAME,V.VERSIONCODE,V.FILESIZE,V.DOWNLOADURL,V.REMARK,V.VERSIONSTATE FROM B_VERSION  V WHERE V.USEPLATFORM = ? ORDER BY to_number(VERSIONCODE) DESC) WHERE ROWNUM=1 ", platform);
            r.setDataset(list);
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
     * 获取用户信息
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/user/get")
    @ResponseBody
    public Object userInfo() {
        Map<String, String> param = RequestUtils.toMap(request);
        ResultEntity r = new ResultEntity(1);
        try {
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            List<Map<String, Object>> m = DBHelper.queryForList("select  id,phone,email,idcard,name," +
                    "   avatar,acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)JOB," +
                    "   (SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK)POLICERANK," +
                    "   to_char(CREATETIME,'YYYY-MM-DD HH24:MI:SS')CREATETIME,to_char(UPDATETIME,'YYYY-MM-DD HH24:MI:SS')UPDATETIME," +
                    "   DWBM,(SELECT DEPTNAME FROM Ss_Dept BD WHERE BD.id = t.dwmc )DWMC,SJDWBM," +
                    "   (SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'sjdwmc'  AND BD.VALUE = t.SJDWMC)SJDWMC,RWNY,SS_ID,TQXJTS,NXJTS,TQXJTSSFJB,SFKYLDZS,NATIVE_PLACE    from   tb_user t where t.SS_ID=?", userEntity.getUser_id());
            System.out.println(userEntity.getUser_id());
            r.setDataset(m);
            r.setMsg("操作成功！");
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
     * 消防员
     *
     * @param
     * @return
     */
    @RequestMapping(value = "user/xfy/get")
    @ResponseBody
    public Object xfy() {
        Map<String, String> param = RequestUtils.toMap(request);
        ResultEntity r = new ResultEntity(1);
        try {
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            List<Map<String, Object>> m = DBHelper.queryForList("select id,name,ss_id from tb_user \n" +
                    "where dwmc=\n" +
                    "(select dwmc from tb_user where ss_id=?) and job!='7'and job !='3'", userEntity.getUser_id());
            System.out.println(userEntity.getUser_id());
            r.setDataset(m);
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
     * 消防员
     *
     * @param
     * @return
     */
    @RequestMapping(value = "user/dw/get")
    @ResponseBody
    public Object dw() {
        Map<String, String> param = RequestUtils.toMap(request);
        ResultEntity r = new ResultEntity(1);
        try {
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            List<Map<String, Object>> m = DBHelper.queryForList("select t.id,\n" +
                    "       t.ss_id,\n" +
                    "       (select DEPTNAME NAME FROM SS_DEPT s WHERE s.id = t.dwmc) dwmc_name,\n" +
                    "       t.dwmc\n" +
                    "  from TB_USER t where t.ss_id=? ", userEntity.getUser_id());
            System.out.println(userEntity.getUser_id());
            r.setDataset(m);
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
     * 消防员
     *
     * @param
     * @return
     */
    @RequestMapping(value = "user/zj/get")
    @ResponseBody
    public Object zj() {
        Map<String, String> param = RequestUtils.toMap(request);
        ResultEntity r = new ResultEntity(1);
        try {
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            List<Map<String, Object>> m = DBHelper.queryForList("select b.value,name from business_dict b where b.type_code='zw' ");
//            System.out.println(userEntity.getUser_id());
            r.setDataset(m);
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
     * 打卡
     *
     * @return
     */

    @RequestMapping(value = "/user/dq")
    @ResponseBody
    public Object dq(String rq, String sj, String jd, String wd, String dz, String type) {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            if (type == null) {
                r.setCode(-1);
                r.setMsg("请输入上下班！");
                return r;
            }
            if (rq == null) {
                r.setCode(-1);
                r.setMsg("请输入日期！");
                return r;
            }
            if (sj == null) {
                r.setCode(-1);
                r.setMsg("请输入时间！");
                return r;
            }
            if (jd == null) {
                r.setCode(-1);
                r.setMsg("请输入经度！");
                return r;
            }
            if (wd == null) {
                r.setCode(-1);
                r.setMsg("请输入纬度！");
                return r;
            }
            if (dz == null) {
                r.setCode(-1);
                r.setMsg("请输入地址！");
                return r;
            }

            String msg ="";
            if (type.equals("3")){
                //抽查
                msg = dqService.spotDq(param, userEntity, request);
            }else{
                //上下班
                msg = dqService.Dq(param, userEntity, request);

            }

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
     * 工作圈列表
     */
    @RequestMapping("/user/gzq/list")
    @ResponseBody
    public Object flsbList(String type) {
        ResultEntity r = new ResultEntity(1);
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
        try {
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            if (type == null) {
                r.setCode(-1);
                r.setMsg("请输入type！");
                return r;
            }
            if ("1".equals(type)) {
                page = gzqService.loadList(param, userEntity);
                ((List<Map<String,Object>>)page.getRows()).stream().forEach(item-> {
                    List<Map<String,Object>>dzlist=DBHelper.queryForList("select  t.GOOD_TO_ACCOUNT as DZZID,t.GOOD_STATUS,u.name as GOOD_NAME  from tb_good t  left join tb_user u on u.ss_id=t.good_to_account   where t.BUSINESSID=?",item.get("ID"));
                    item.put("DZLIST",dzlist);
                });

                r.setDataset(page);
                r.setMsg("操作成功");
            }
            if ("2".equals(type)) {
                page = gzqService.loadListxq(param, userEntity);
                ((List<Map<String,Object>>)page.getRows()).stream().forEach(item-> {
                    List<Map<String,Object>>dzlist=DBHelper.queryForList("select  t.GOOD_TO_ACCOUNT as DZZID,t.GOOD_STATUS,u.name as GOOD_NAME  from tb_good t  left join tb_user u on u.ss_id=t.good_to_account   where t.BUSINESSID=?",item.get("ID"));
                    item.put("DZLIST",dzlist);
                });
                r.setDataset(page);
                r.setMsg("操作成功");
            }


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
     * 工作圈新增
     *
     * @return
     */

    @RequestMapping(value = "/user/gzq/save")
    @ResponseBody
    public Object savegzq(String content, String address, String publish_platform) {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            if (content == null) {
                r.setCode(-1);
                r.setMsg("请输入工作圈内容！");
                return r;
            }
            if (address == null) {
                r.setCode(-1);
                r.setMsg("请输入地址！");
                return r;
            }
            if (publish_platform == null) {
                r.setCode(-1);
                r.setMsg("请输入发布平台！");
                return r;
            }
            gzqService.save(param, userEntity);
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
     * 文件上传
     *
     * @return ResultEntity
     **/
    @RequestMapping(value = "/file/upload", method = RequestMethod.POST)
    @ResponseBody
    public Object upload(HttpServletRequest request) throws IOException {
        String mode = request.getParameter("mode");
        String callback = "upload_iframe_callback";
        String root = rootPath;
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
        Map<String, Object> retMap = new HashMap<String, Object>();
        if (root == null) {
            logger.warn("未取到文件上传的根目录信息,请先检查配置");
            retMap.put("code", "-1");
            retMap.put("msg", "未取到文件上传的根目录信息,请先检查配置");
            return retMap;
        }
        String[] fileguids = RandomUtils.getUUID(files.size());
//        String ym = DateUtils.format(new Date(), "yyyy") + File.separator + DateUtils.format(new Date(), "MM") + File.separator;
        String ym = "";
        List<Map<String, String>> filelist = new ArrayList<Map<String, String>>();
        String target = ym;
        for (int i = 0; i < files.size(); i++) {
            MultipartFile multipartfile = files.get(i);

            if (multipartfile == null) {
                continue;
            }

            Map<String, String> filemap = new HashMap<String, String>();

            String guid = fileguids[i];

            target = ym + guid + "." + FilenameUtils.getExtension(multipartfile.getOriginalFilename());

            float filesize = multipartfile.getSize();
            String size = new DecimalFormat("#.00").format(filesize / 1024 / 1024);

            try {
                FileUtils.copyInputStreamToFile(multipartfile.getInputStream(), new File(root, target));

                filemap.put("id", guid);
                filemap.put("name", FilenameUtils.getBaseName(multipartfile.getOriginalFilename()));
                filemap.put("ext", FilenameUtils.getExtension(multipartfile.getOriginalFilename()));
                filemap.put("path", target);
                filemap.put("ctype", multipartfile.getContentType());
                filemap.put("size", size);

                filelist.add(filemap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("filelist", filelist);

        ResultEntity entity = new ResultEntity(1);
        try {
            fileService.UploadBatch(filelist);
            entity.setDataset(filelist);
        } catch (RuntimeException ex) {
            entity.setCode(-1);
            entity.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
        }
        if ("kindeditor".equals(mode)) {
            retMap.put("error", entity.getCode() > 0 ? 0 : -1);
            retMap.put("url", request.getContextPath() + "file/download?id=" + fileguids[0]);
        } else {
            retMap.put("code", entity.getCode());
            retMap.put("msg", entity.getMsg());
            retMap.put("ids", fileguids);
            retMap.put("dataset", entity.getDataset());
        }

        String retString = JsonUtils.toJson(retMap);

        if ("iframe".equals(mode)) {
            return "<script>if(parent && parent." + callback + "){parent." + callback + "(" + retString + ");}</script>";
        }

        return retString;
    }


    /**
     * 工作圈删除
     */
    @RequestMapping("/user/gzq/del")
    @ResponseBody
    public Object delgzq() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            gzqService.delete(param, userEntity);
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
     * 工作圈详情
     *
     * @return
     */
    @RequestMapping(value = "/user/gzq/get")
    @ResponseBody
    public Object getFlsb(String id) {
        ResultEntity r = new ResultEntity(1);
        try {
            //整合参数
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(0);
                r.setMsg("用户不存在！");
                return r;
            }
            if (id == null) {
                r.setCode(0);
                r.setMsg("工作圈id不存在！");
                return r;
            }
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> m = gzqService.get(param, userEntity);
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
    }

    /**
     * 工作圈评论新增
     *
     * @return
     */

    @RequestMapping(value = "/user/pl/save")
    @ResponseBody
    public Object savepl(String gzqid, String plzt, String nr, String ss_id) {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            if (gzqid == null) {
                r.setCode(-1);
                r.setMsg("请输入工作圈ID！");
                return r;
            }
            if (plzt == null) {
                r.setCode(-1);
                r.setMsg("请输入评论状态！");
                return r;
            }
            if (nr == null) {
                r.setCode(-1);
                r.setMsg("请输入评论内容！");
                return r;
            }
            if (ss_id == null) {
                r.setCode(-1);
                r.setMsg("请输入ss_id！");
                return r;
            }
            gzqService.savepl(param, userEntity);
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
     * 人员分析list
     *
     * @return
     */

    @RequestMapping(value = "/user/ryfx/list")
    @ResponseBody
    public Object loadryfxlist() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            dqService.save(param);
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
     * 修改个人信息
     *
     * @return
     */

    @RequestMapping(value = "/user/grxx/save")
    @ResponseBody
    public Object saveggxx() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            dqService.save(param);
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
     * 修改手机号
     */
    @RequestMapping(value = "/user/sjhm/save")
    @ResponseBody
    public Object xgsj(String ss_id, String phone) {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            String code =  trimToEmpty(param.get("code"));
            //获取Redis的对应验证码
            String rediscode = trimToEmpty(redisService.get(phone));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            if (ss_id == null) {
                r.setCode(-1);
                r.setMsg("需要输出ss_id！");
                return r;
            }
            if (phone == null) {
                r.setCode(-1);
                r.setMsg("输入更改手机号！");
                return r;
            }else{
                String id=DBHelper.queryForScalar("SELECT id FROM  TB_USER WHERE PHONE=?",String.class,phone);
                if (!"".equals(trimToEmpty(id))){
                    r.setCode(-1);
                    r.setMsg("更换的手机号已经被绑定！");
                    return r;
                }
            }
            if (code.equals(rediscode)&&code!=""){
                xgmmService.save(param);
                r.setMsg("操作成功");
                r.setCode(1);
            }else{
                r.setCode(-1);
                r.setMsg("验证码错误！");
                return r;
            }
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
     * 评论删除
     */
    @RequestMapping("/user/pl/del")
    @ResponseBody
    public Object delpl(String plid) {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            if (plid == null) {
                r.setCode(-1);
                r.setMsg("评论id不存在！");
                return r;
            }
            gzqService.delpl(param, userEntity);
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
     * 评论删除
     */
    @RequestMapping("/user/ryfx/sh")
    @ResponseBody
    public Object sh() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            ryfxMoblieService.ryfxsh(param);
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
     * 工作圈点赞/取消点赞
     *
     * @return
     */

    @RequestMapping(value = "/user/dz/save")
    @ResponseBody
    public Object savedz(String gzqid, String dzid, String dzzt) {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }

            if (gzqid == null) {
                r.setCode(-1);
                r.setMsg("请输入工作圈ID！");
                return r;
            }
            if (dzid == null) {
                r.setCode(-1);
                r.setMsg("请输入点赞者ID！");
                return r;
            }
            if (dzzt == null) {
                r.setCode(-1);
                r.setMsg("请输入点赞状态！");
                return r;
            }
            gzqService.savedz(param, userEntity);
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
     * 同步
     *
     * @return
     */

    @RequestMapping(value = "/user/ryfx/tb")
    @ResponseBody
    public Object tb(String ss_id) {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            if (ss_id == null) {
                r.setCode(-1);
                r.setMsg("请输入用户！");
                return r;
            }
            List<Map<String, Object>> list = ryfxMoblieService.findusers(param);
            r.setDataset(list);
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
     * 获得打卡人信息
     *
     * @return
     */

    @RequestMapping(value = "/user/get/dq")
    @ResponseBody
    public Object getDq() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            List<Map<String, Object>> list = dqService.getDq(param, userEntity);
            r.setDataset(list);
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
     * 权限管理
     *
     * @return
     */

    @RequestMapping(value = "/user/ryfx/qx")
    @ResponseBody
    public Object qx() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            List<Map<String, Object>> list = ryfxMoblieService.qx(param);
            List<Map<String, Object>> type = new ArrayList<>();
            if (!StringUtils.isBlank((CharSequence) list.get(0).get("JOB"))) {
                for (int i = 0; i < list.size(); i++) {
                    String roletype = "";
                    Map<String, Object> map = new HashMap<>();
                    //中队长和指导员提交
                    if ("3".equals(list.get(i).get("JOB").toString())||"7".equals(list.get(i).get("JOB").toString())||"8".equals(list.get(i).get("JOB").toString())) {
                        roletype = "1";
                        //大队长和教导员可以审批
                    } else if ("1".equals(list.get(i).get("JOB").toString())||"2".equals(list.get(i).get("JOB").toString())) {
                        roletype = "2";
                        //队务科参谋长查看
                    } else if("0".equals(list.get(i).get("JOB").toString())||"x".equals(list.get(i).get("JOB").toString())){
                        roletype = "3";
                    }else{
                        roletype = "0";
                    }
                    map.put("roletype", roletype);
                    type.add(map);
                }
            } else {
                Map<String, Object> map = new HashMap<>();
                String roletype = "0";
                map.put("roletype", roletype);
                type.add(map);
            }
            r.setDataset(type);
            r.setMsg("");

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
     * 轮播图列表
     */
    @RequestMapping("/user/lbt/list")
    @ResponseBody
    public Object lbtList(String type) {
        ResultEntity r = new ResultEntity(1);
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
        try {

            if (type == "1" && userEntity != null) {
                r.setCode(-1);
                r.setMsg("无需输入token");
                return r;
            }
            if (type == "2" && userEntity == null) {
                r.setCode(-1);
                r.setMsg("需输入token");
                return r;
            }
            page = lbtService.loadList(param);
            r.setDataset(page);
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
     * 逻辑删除
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "user/ryfx/del")
    @ResponseBody
    public Object del(String id) {
        ResultEntity r = new ResultEntity(1);
        //整合参数

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            if (id == null) {
                r.setCode(-1);
                r.setMsg("请输入ID！");
                return r;
            }
            ryfxMoblieService.del(param);
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

    //    @RequestMapping(value = "user/ryfx/tj")
//    @ResponseBody
//    public Object saveryfx() {
//        ResultEntity r = new ResultEntity(1);
//        //整合参数
//        Map<String, String> param = RequestUtils.toMap(request);
//        Map<String,Object> user = UserUtils.getUserByToken(param.get("userId"));
//        String userId =user.get("SS_ID").toString();
//        param.put("userId",userId);
//        Page<?> page;
//        try {
//            ryfxMoblieService.tj(param);
//            r.setMsg("操作成功");
//        } catch (RuntimeException ex) {
//            r.setCode(-1);
//            r.setMsg("网络异常");
//            logger.error(ex.getMessage(), ex);
//            throw ex;
//        } finally {
//            return r;
//        }
//    }
    @RequestMapping(value = {"/download"})
    public ModelAndView download() {
        ModelAndView view = new ModelAndView("mobile/download");
        return view;
    }

    @RequestMapping(value = {"/download/apk"})
    public String downloadapk() {
        String platform = request.getParameter("platform");
        Map map = DBHelper.queryForMap("SELECT * FROM (SELECT V.ID,V.VERSIONNAME,V.VERSIONCODE,V.FILESIZE,V.DOWNLOADURL,V.REMARK,V.VERSIONSTATE FROM B_VERSION  V WHERE USEPLATFORM=? ORDER BY TO_NUMBER(VERSIONCODE) DESC) WHERE ROWNUM=1 ", platform);
        if (map != null && map.get("DOWNLOADURL") != null && StringUtils.isNotBlank(map.get("DOWNLOADURL").toString())) {
            String apkPath = map.get("DOWNLOADURL").toString();
            return "redirect:" + fileFilterUrl + apkPath;
        }
        return "";
    }


//TODO
//###############################请假部分#################################

    /**
     * 用户请假类型权限
     *
     * @return
     */

    @RequestMapping(value = "/user/get/leavetype")
    @ResponseBody
    public Object getLeaveType() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            //用户信息
            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());
            //查询字典表的请假类型
            List<Map<String, Object>> leavetype = leaveService.leaveType();
            List<Map<String, Object>> true_leavetype = new ArrayList<>();
            //判断当前用户可用的请假类型
            for (Map<String, Object> leave : leavetype) {

                switch (leave.get("value") + "") {
                    //事假
                    case "1":
                        true_leavetype.add(leave);
                        break;
                    //探亲假
                    case "2":
                        //可否探亲休假
                        if (!(trimToNum(usermap.get("TQXJTS")) + "").equals("0")) {
//                            List<Map<String, Object>> leavehistory2 = leaveService.selectLeaveHistory(userEntity,"2");
//                            if (leavehistory2.size()==0){
                            true_leavetype.add(leave);
//                            }
                        }
                        break;
                    //年假
                    case "3":
                        if (!(trimToNum(usermap.get("NXJTS")) + "").equals("0")) {
                            //警衔
                            if ((trimToEmpty(usermap.get("POLICERANK_NAME"))).indexOf("消防长") > -1) {
                                true_leavetype.add(leave);
                            }
                        }
                        break;
                    //婚假
                    case "4":
                        List<Map<String, Object>> leavehistory4 = leaveService.selectLeaveHistory(userEntity, "4");
                        if (leavehistory4.size() == 0) {
                            true_leavetype.add(leave);
                        }
                        break;
                    //护理假
                    case "5":
                        List<Map<String, Object>> leavehistory5 = leaveService.selectLeaveHistory(userEntity, "5");
                        if (leavehistory5.size() == 0) {
                            true_leavetype.add(leave);
                        }
                        break;
                    //病假
                    case "6":
                        true_leavetype.add(leave);
                        break;
                    //离队住宿
                    case "7":
                        if (usermap.size() == 1) {
                            //警衔
                            if ((trimToEmpty(usermap.get("SFKYLDZS"))).equals("1")) {
                                true_leavetype.add(leave);
                            }
                        }
                        break;
                    //因公外出
                    case "8":
                        true_leavetype.add(leave);
                        break;
                    //集训培训
                    case "9":
                        true_leavetype.add(leave);
                        break;
                    //其他
                    case "10":
                        true_leavetype.add(leave);
                        break;
                    //轮休（驻地）
                    case "11":
                        true_leavetype.add(leave);
                        break;
                    //轮休（异地）
                    case "12":
                        true_leavetype.add(leave);
                        break;
                }
            }
            r.setDataset(true_leavetype);
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
     * 用户请假下级审批人
     *
     * @return
     */

    @RequestMapping(value = "/user/get/leavesjperson")
    @ResponseBody
    public Object getLeaveSJperson() {
        ResultEntity r = new ResultEntity(1);
        List<Map<String, Object>> sjlist = new ArrayList<>();
        List<Map<String, Object>> sjlist_jdy = new ArrayList<>();
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }


            //用户信息
            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());

//            int LEAVE_DAYS =0;
            if (usermap.size() > 0) {
                String START_TIME = "";
                String END_TIME = "";
                if (!"".equals(trimToEmpty(param.get("BUSINESSID")))) {
                    //请假时间
                    Map<String, Object> map = leaveService.getTaskByBussinessId(trimToEmpty(param.get("BUSINESSID")));
                    START_TIME = trimToEmpty(map.get("START_TIME"));
                    END_TIME = trimToEmpty(map.get("END_TIME"));
                } else {
                    //请假时间
                    START_TIME = trimToEmpty(param.get("START_TIME"));
                    END_TIME = trimToEmpty(param.get("END_TIME"));
                }
                //计算请假天数
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");//注意月份是MM
                Date date1 = simpleDateFormat.parse(START_TIME);
                Date date2 = simpleDateFormat.parse(END_TIME);
//                LEAVE_DAYS = getDayDiffer(date1,date2);
                //流程走向计算
                int DaysForActiviti = getDayDifferForActiviti(date1, date2);
                //用户提交申请阶段
                if ("".equals(trimToEmpty(param.get("BUSINESSID")))) {
                    //一天以上直接站长长
                    if (DaysForActiviti >= 1) {
                        //先上级站长
                        sjlist = sjlist("3", "7", trimToEmpty(usermap.get("DWMC_TRUE")), false);
                    } else {
                        //用户是班长
                        if ((trimToEmpty(usermap.get("JOB"))).indexOf("班长") > -1) {
                            //先查询班长的上级站长
                            sjlist = sjlist("3", "7", trimToEmpty(usermap.get("DWMC_TRUE")), false);
                            //用户是士兵
                        } else if ((trimToEmpty(usermap.get("JOB"))).equals("")) {
                            sjlist = sjlist("4", "5", trimToEmpty(usermap.get("DWMC_TRUE")), false);
                        } else {
                            sjlist = sjlist("4", "5", trimToEmpty(usermap.get("DWMC_TRUE")), false);
                        }
                    }
                } else {
                    //审批人审批阶段(班长选择)
                    if ((trimToEmpty(usermap.get("JOB"))).indexOf("班长") > -1) {
                        //先查询班长 的上级站长
                        sjlist = sjlist("3", "7", trimToEmpty(usermap.get("DWMC_TRUE")), false);
                    } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("站长") > -1)||((trimToEmpty(usermap.get("JOB"))).indexOf("指导员") > -1)) {
                        //一天以上不需要选择下一级的审批人
                        if (DaysForActiviti >= 1) {
                            //查询站长 的上级 大队长和教导员
                            sjlist = sjlist("1", "2", trimToEmpty(usermap.get("DWMC_TRUE")), true);
                        } else {
                            //一天以内，站长没有下级审批人
                            Map<String, Object> pa = new HashMap<String, Object>();
                            pa.put("spr", "");
                            sjlist.add(pa);
                        }

                    } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("大队长") > -1) || ((trimToEmpty(usermap.get("JOB"))).indexOf("教导员") > -1)) {
                        //查询大队长和教导员 的上级 队务科科长
                        sjlist = sjlist("0", "未知", trimToEmpty(usermap.get("DWMC_TRUE")), true);
                    } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("队务科") > -1)) {
                        //查询队务科长 的上级 政治部主任
                        sjlist = sjlist("x", "未知", trimToEmpty(usermap.get("DWMC_TRUE")), true);
                    } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("政治部") > -1)) {
                        //一天以上，政治部主任没有下级审批人
                        Map<String, Object> pa = new HashMap<String, Object>();
                        pa.put("spr", "");
                        sjlist.add(pa);
                    }


                }
            }


            r.setDataset(sjlist);
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
     * 用户请假下级审批人
     *
     * @return
     */

    @RequestMapping(value = "/user/get/leavesprinit")
    @ResponseBody
    public Object getLeaveSprInit() {
        ResultEntity r = new ResultEntity(1);
        List<Map<String, Object>> sjlist = new ArrayList<>();
        boolean flag=true;
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }


            //用户信息
            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());

//            int LEAVE_DAYS =0;
            if (usermap.size() > 0) {
                String START_TIME = "";
                String END_TIME = "";
                if (!"".equals(trimToEmpty(param.get("BUSINESSID")))) {
                    //请假时间
                    Map<String, Object> map = leaveService.getTaskByBussinessId(trimToEmpty(param.get("BUSINESSID")));
                    START_TIME = trimToEmpty(map.get("START_TIME"));
                    END_TIME = trimToEmpty(map.get("END_TIME"));
                } else {
                    //请假时间
                    START_TIME = trimToEmpty(param.get("START_TIME"));
                    END_TIME = trimToEmpty(param.get("END_TIME"));
                }
                //计算请假天数
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");//注意月份是MM
                Date date1 = simpleDateFormat.parse(START_TIME);
                Date date2 = simpleDateFormat.parse(END_TIME);
//                LEAVE_DAYS = getDayDiffer(date1,date2);
                //流程走向计算
                int DaysForActiviti = getDayDifferForActiviti(date1, date2);
                //用户提交申请阶段
                if ("".equals(trimToEmpty(param.get("BUSINESSID")))) {
                    //一天以上直接站长长
                    if (DaysForActiviti >= 1) {
                        //先上级站长
                        flag=false;
                    } else {
                        //用户是班长
                        if ((trimToEmpty(usermap.get("JOB"))).indexOf("班长") > -1) {
                            //先查询班长的上级站长
                            flag=false;
                            //用户是士兵
                        } else if ((trimToEmpty(usermap.get("JOB"))).equals("")) {
                            flag=true;
                        } else {
                            flag=true;
                        }
                    }
                } else {

//                    if (DaysForActiviti >= 1) {
//                        //先上级站长
//                        flag=false;
//                    } else {
                        //审批人审批阶段(班长选择)
                        if ((trimToEmpty(usermap.get("JOB"))).indexOf("班长") > -1) {
                            //先查询班长 的上级站长
                            flag = false;
                        } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("站长") > -1) || ((trimToEmpty(usermap.get("JOB"))).indexOf("指导员") > -1)) {
                            //一天以上不需要选择下一级的审批人
                            if (DaysForActiviti >= 1) {
                                //查询站长 的上级 大队长和教导员
                                flag = false;
                            } else {
                                flag = true;
                            }

                        } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("大队长") > -1) || ((trimToEmpty(usermap.get("JOB"))).indexOf("教导员") > -1)) {
                            //查询大队长和教导员 的上级 队务科科长
                            flag = true;
                        } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("队务科") > -1)) {
                            //查询队务科长 的上级 政治部主任
                            flag = true;
                        } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("政治部") > -1)) {
                            flag = false;
                        }
//                    }

                }
            }

            Map<String,Object> map = new HashMap<>();
            map.put("sprdisplay",flag);
            sjlist.add(map);
            r.setDataset(sjlist);
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

    //审批人查询
    List<Map<String, Object>> sjlist(String job1, String job2, String dwmc, boolean ifsj) {
        //是否查询上级
        if (ifsj) {
            dwmc = DBHelper.queryForScalar("select dd.parentid from SS_DEPT dd where dd.id=?", String.class, dwmc);
        }
        return DBHelper.queryForList("select  t.ss_id as id,t.phone,t.email,t.idcard,t.name," +
                "t.avatar,t.acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)job," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK) as POLICERANK_NAME,POLICERANK, " +
                "to_char(CREATETIME,'yyyy-mm-dd hh24:mi')CREATETIME,to_char(UPDATETIME,'yyyy-mm-dd hh24:mi')UPDATETIME," +
                "RWNY,SS_ID,SFKYLDZS,NATIVE_PLACE,d.DEPTNAME as DWMC,DWMC as DWMC_TRUE     from   tb_user t   LEFT JOIN SS_DEPT D  ON D.id = t.DWMC   where t.job in (?,?) and dwmc=? and acc_status=1", job1, job2, dwmc);

    }

    /**
     * 用户当年已经请假天数
     *
     * @return
     */

    @RequestMapping(value = "/user/get/leavehistorycount")
    @ResponseBody
    public Object getLeaveHistoryCount() {
        ResultEntity r = new ResultEntity(1);
        List<Map<String, Object>> leavehistorycount = new ArrayList<>();
        try {
            Map<String, String> param = RequestUtils.toMap(request);

            String userId = trimToEmpty(param.get("userId"));
            UserEntity userEntity = CustomRealm.GetLoginUser(userId);
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            String ss_id=userEntity.getUser_id();
            //如果传入businessid，则查请假人的剩余天数
            if (!trimToEmpty(param.get("BUSINESSID")).equals("")){
                Map<String,Object> leaveMap = leaveService.getTaskByBussinessId(param.get("BUSINESSID"));
                ss_id = trimToEmpty(leaveMap.get("SS_ID"));
            }

            //用户信息
            Map<String, Object> usermap = getUserInfo(ss_id);
            int allcount = 0;
            if ("1".equals(trimToEmpty(trimToNum(usermap.get("TQXJTSSFJB"))))) {
                allcount = (trimToNum(usermap.get("NXJTS")) + trimToNum(usermap.get("TQXJTS"))) / 2;
            } else {
                allcount = trimToNum(usermap.get("NXJTS")) + trimToNum(usermap.get("TQXJTS"));
            }
            //剩余
            int surplus = 0;


            BigDecimal b1 = new BigDecimal("0");
            if (usermap.size() > 1) {


                //探亲假
                List<Map<String, Object>> leavehistory2 = leaveService.selectLeaveHistory(userEntity, "2");
                if (leavehistory2.size() != 0) {
                    for (Map<String, Object> leave : leavehistory2) {
                        BigDecimal b3 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                        b1 = b1.add(b3);
                    }
                }


                //年假
//                if ((trimToEmpty(usermap.get("POLICERANK_NAME"))).indexOf("消防长")>-1) {
                List<Map<String, Object>> leavehistory3 = leaveService.selectLeaveHistory(userEntity, "3");
                if (leavehistory3.size() != 0) {
                    for (Map<String, Object> leave : leavehistory3) {
                        BigDecimal b3 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                        b1 = b1.add(b3);
                    }
                }
//                }


                //事假，一天以上
                List<Map<String, Object>> leavehistory1 = leaveService.selectLeaveHistory(userEntity, "1");
                //计算总天数
                if (leavehistory1.size() != 0) {
                    BigDecimal b2 = new BigDecimal("1");
                    for (Map<String, Object> leave : leavehistory1) {


                        BigDecimal b3 = new BigDecimal(trimToNum(leave.get("leave_days_true")));
                        //大于一天,计算入内(减去一天)
                        if (b3.compareTo(b2) > 0) {
                            BigDecimal b4 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                            if (b4.intValue() < 1) {
                                b4 = new BigDecimal("1");
                            }
                            b1 = b1.add(b4).subtract(b2);
                        }
                    }
                }


                //计算还剩多少假期（探亲，年假）
                surplus = allcount - b1.intValue();
                if (surplus < 0) {
                    surplus = 0;
                }
/*
                //离队住宿
                if ((trimToEmpty(usermap.get("SFKYLDZS"))).equals("1")){
                    List<Map<String, Object>> leavehistory7 = leaveService.selectLeaveHistory(userEntity,"7");
                    if (leavehistory7.size()!=0){
                        for (Map<String, Object> leave:leavehistory7) {
                            BigDecimal b3 = new BigDecimal(trimToEmpty(leave.get("leave_days_true"))) ;
                            b1 = b1.add(b3);
                        }
                    }
                }*/
            }
            Map<String, Object> map = new HashMap<>();
            map.put("leavehistorycount", "已休" + b1.intValue() + "天探亲年假，剩余" + surplus + "天");
            map.put("username", trimToEmpty(usermap.get("name")));
            map.put("already", b1.intValue());
            map.put("notyet", surplus);
            leavehistorycount.add(map);
            r.setDataset(leavehistorycount);
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
     * 用户请假信息保存
     *
     * @return
     */
    @RequestMapping(value = "/user/leavesave")
    @ResponseBody
    @Transactional(value = "primaryTransactionManager")
    public Object LeaveSave() {
        ResultEntity r = new ResultEntity(1);
        List<Map<String, Object>> leaveins = new ArrayList<>();
        List<Map<String, Object>> leaveList = new ArrayList<>();
        List<Map<String, Object>> sjlist = new ArrayList<>();
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }

            String LEAVE_TYPE = trimToEmpty(param.get("LEAVE_TYPE"));
            //请假时间，需要拆分
            String START_TIME = trimToEmpty(param.get("START_TIME"));
            String END_TIME = trimToEmpty(param.get("END_TIME"));
            String LEAVE_SPACE = trimToEmpty(param.get("LEAVE_SPACE"));
            String REASON = trimToEmpty(param.get("REASON"));
            String SPR = trimToEmpty(param.get("SPR"));
            String COMPLETE = trimToEmpty(param.get("COMPLETE"));
            //业务id（保存后送审）
            int LEAVE_ID = trimToNum(param.get("BUSINESSID"));

            //是否是退回后重新提交申请（重新生成流程）
            int returnStr = trimToNum(param.get("RETURNSTR"));


            //用户信息
            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());

            //控制在务率，站长是否可以请假（满足同时间只能固定人数同时请假）
            if (LeaveZDzwlCtr(userEntity.getUser_id(), START_TIME, END_TIME)) {

                String dwmc = trimToEmpty(usermap.get("DWMC_TRUE"));
                String countStr = DBHelper.queryForScalar("select t.leave_limit from ss_dept t where t.id=?", String.class, dwmc);
                Map<String, Object> map = new HashMap<>();
                map.put("zdzwl", "超过设定的请假人数");
                leaveList.add(map);
                r.setCode(-1);
                r.setDataset(leaveList);
                r.setMsg("根据管理规定，消防员在位率规定达到98%，当前已有" + countStr + "人请假！");
                return r;
            }


            LeaveApply apply = new LeaveApply(LEAVE_ID, userEntity.getUser_id(), START_TIME, END_TIME, LEAVE_TYPE, REASON, LEAVE_SPACE, new ArrayList<String>(Arrays.asList(SPR)), COMPLETE);


            //请假天数
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");//注意月份是MM
            Date date1 = simpleDateFormat.parse(START_TIME);
            Date date2 = simpleDateFormat.parse(END_TIME);
            //天数计算
            int LEAVE_DAYS = getDayDiffer(date1, date2);
            //流程走向计算
            int DaysForActiviti = getDayDifferForActiviti(date1, date2);
            //实际请假天数
            int LEAVE_DAYS_TRUE = getDaysByHoliday(date1, date2, LEAVE_DAYS);


            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("applyUserId", userEntity.getUser_id());
            variables.put("audit", 1);
            variables.put("returnStr", returnStr);

            //确定流程走向
            if (DaysForActiviti >= 1) {
                //时间超过一天
                variables.put("days", 1);
                List<String> assigneeList = new ArrayList<String>();

                sjlist("3", "7", trimToEmpty(usermap.get("DWMC_TRUE")), false).forEach(item->{
                    assigneeList.add(item.get("id")+"");
                });
                apply.setSpr(null);
                apply.setSpr(assigneeList);
                variables.put("ZdAssigneeList", assigneeList);

            } else {
                //时间不超过一天
                variables.put("days", 0);
                if ((trimToEmpty(usermap.get("JOB"))).indexOf("班长") > -1) {
                    //班长
                    List<String> assigneeList = new ArrayList<String>();

                    sjlist("3", "7", trimToEmpty(usermap.get("DWMC_TRUE")), false).forEach(item->{
                        assigneeList.add(item.get("id")+"");
                    });
                    apply.setSpr(null);
                    apply.setSpr(assigneeList);
                    variables.put("bz", 1);
                    variables.put("ZdAssigneeList", assigneeList);
                } else {
                    //非班长
                    variables.put("bz", 0);
                    variables.put("BzAudit", SPR);
                }

            }


            apply.setLeave_days(LEAVE_DAYS + "");
            apply.setLeave_days_true(LEAVE_DAYS_TRUE + "");

            String ins = "";


            ResultEntity tqnjts = null;

            //探亲假年假天数充足的情况下，可以请假
            switch (LEAVE_TYPE) {
                //事假
                case "1":

                    tqnjts = Tqnxjts(LEAVE_DAYS, usermap, userEntity, leaveList, r);
                    if (tqnjts.getCode() == -1) {
                        return tqnjts;
                    }

                    ins = leaveService.startWorkflow(apply, variables);

                    break;


                //探亲假
                case "2":

                    tqnjts = Tqnxjts(LEAVE_DAYS, usermap, userEntity, leaveList, r);
                    if (tqnjts.getCode() == -1) {
                        return tqnjts;
                    }
                    ins = leaveService.startWorkflow(apply, variables);

                    break;


                //年假
                case "3":
                    tqnjts = Tqnxjts(LEAVE_DAYS, usermap, userEntity, leaveList, r);
                    if (tqnjts.getCode() == -1) {
                        return tqnjts;
                    }
                    ins = leaveService.startWorkflow(apply, variables);


                    break;
                //婚假
                case "4":
                    List<Map<String, Object>> leavehistory4 = leaveService.selectLeaveHistory(userEntity, "4");
                    if (leavehistory4.size() == 0) {
                        ins = leaveService.startWorkflow(apply, variables);

                    }
                    break;


                //护理假
                case "5":
                    List<Map<String, Object>> leavehistory5 = leaveService.selectLeaveHistory(userEntity, "5");
                    if (leavehistory5.size() == 0) {
                        ins = leaveService.startWorkflow(apply, variables);

                    }
                    break;


                //病假
                case "6":
                    ins = leaveService.startWorkflow(apply, variables);

                    break;


                //离队住宿
                case "7":
                    //可否离队住宿
                    if ((usermap.get("SFKYLDZS") + "").equals("1")) {
                        List<Map<String, Object>> business_dict = leaveService.business_dict("ldzsts");
                        //可以请假天数
                        BigDecimal LDZS_all7 = new BigDecimal(trimToEmpty(business_dict.get(0).get("VALUE")));
                        //已经请假天数
                        BigDecimal LDZS_true7 = new BigDecimal(0);
                        List<Map<String, Object>> leavehistory7 = leaveService.selectLeaveHistory(userEntity, "7");
                        if (leavehistory7.size() != 0) {
                            for (Map<String, Object> leave : leavehistory7) {
                                LDZS_true7 = LDZS_true7.add(new BigDecimal(trimToEmpty(leave.get("leave_days_true"))));
                            }
                        }
                        //实际请假天数加上本次需要请假天数超过了实际可以请假天数
                        if ((LDZS_true7.add(new BigDecimal(LEAVE_DAYS))).compareTo(LDZS_all7) > 0) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("days_remaining", LDZS_all7.subtract(LDZS_true7));
                            leaveList.add(map);
                            r.setCode(-1);
                            r.setDataset(leaveList);
                            r.setMsg("离队住宿天数不足");
                            return r;
                        } else {
                            ins = leaveService.startWorkflow(apply, variables);

                        }

                    }

                    break;

                //因公外出
                case "8":

                    ins = leaveService.startWorkflow(apply, variables);
                    break;

                //集训培训
                case "9":

                    ins = leaveService.startWorkflow(apply, variables);
                    break;

                //其他
                case "10":

                    ins = leaveService.startWorkflow(apply, variables);
                    break;
                //轮休（驻地）
                case "11":

                    ins = leaveService.startWorkflow(apply, variables);
                    break;
                //轮休（异地）
                case "12":

                    ins = leaveService.startWorkflow(apply, variables);
                    break;
            }


            Map<String, Object> map = new HashMap<>();
            map.put("leaveins", ins);
            leaveins.add(map);
            r.setDataset(leaveins);
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


    public ResultEntity Tqnxjts(int LEAVE_DAYS, Map<String, Object> usermap, UserEntity userEntity, List<Map<String, Object>> leaveList, ResultEntity r) {

        int allcount = 0;
        if ("1".equals(trimToEmpty(trimToNum(usermap.get("TQXJTSSFJB"))))) {
            allcount = (trimToNum(usermap.get("NXJTS")) + trimToNum(usermap.get("TQXJTS"))) / 2;
        } else {
            allcount = trimToNum(usermap.get("NXJTS")) + trimToNum(usermap.get("TQXJTS"));
        }
        //剩余
        int surplus = 0;


        BigDecimal b1 = new BigDecimal("0");


        //探亲假
        List<Map<String, Object>> leavehistory2 = leaveService.selectLeaveHistory(userEntity, "2");
        if (leavehistory2.size() != 0) {
            for (Map<String, Object> leave : leavehistory2) {
                BigDecimal b3 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                b1 = b1.add(b3);
            }
        }


        //年假
        List<Map<String, Object>> leavehistory3 = leaveService.selectLeaveHistory(userEntity, "3");
        if (leavehistory3.size() != 0) {
            for (Map<String, Object> leave : leavehistory3) {
                BigDecimal b3 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                b1 = b1.add(b3);
            }
        }


        //事假，一天以上
        List<Map<String, Object>> leavehistory1 = leaveService.selectLeaveHistory(userEntity, "1");
        //计算总天数
        if (leavehistory1.size() != 0) {
            BigDecimal b2 = new BigDecimal("1");
            for (Map<String, Object> leave : leavehistory1) {


                BigDecimal b3 = new BigDecimal(trimToNum(leave.get("leave_days_true")));
                //大于一天,计算入内(减去一天)
                if (b3.compareTo(b2) > 0) {
                    BigDecimal b4 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                    if (b4.intValue() < 1) {
                        b4 = new BigDecimal("1");
                    }
                    b1 = b1.add(b4).subtract(b2);
                }
            }
        }

        //计算还剩多少假期（探亲，年假）
        surplus = allcount - b1.intValue();
        if (surplus < 0) {
            surplus = 0;
        }

        //实际请假天数加上本次需要请假天数超过了实际可以请假天数
        if ((b1.add(new BigDecimal(LEAVE_DAYS))).compareTo(new BigDecimal(allcount)) > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("days_remaining", surplus);
            leaveList.add(map);
            r.setCode(-1);
            r.setDataset(leaveList);
            r.setMsg("探亲假天数不足");
            return r;
        } else {
            r.setCode(1);
            return r;
        }
    }


    //控制在务率，站队内，
    public boolean LeaveZDzwlCtr(String ssid, String start_time, String end_time) throws ParseException {
        List<Map<String, Object>> jiaojilist = new ArrayList<>();
        List<Map<String, Object>> list = new ArrayList<>();
        boolean ifLeave = false;
        Map<String, Object> usermap = getUserInfo(ssid);
        String dwmc = trimToEmpty(usermap.get("DWMC_TRUE"));
        String countStr = DBHelper.queryForScalar("select t.leave_limit from ss_dept t where t.id=?", String.class, dwmc);
        int count = trimToNum(countStr);


        //开始和结束时间转化为月，同月，查询此单位此月的请假，不同月，循环查询
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");//注意月份是MM
        Date start_time_date = simpleDateFormat.parse(start_time);
        Date end_time_date = simpleDateFormat.parse(end_time);
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
        String starttime = dayFormat.format(start_time_date);
        String endtime = dayFormat.format(end_time_date);
        //时间交集
        list = DBHelper.queryForList(" select t.id,t.process_instance_id,t.user_id,to_char(t.start_time,'yyyy-MM-dd') as start_time,to_char(t.end_time,'yyyy-MM-dd') as end_time,t.reason,t.apply_time,t.reality_start_time,t.reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days " +
                " from TB_LEAVEAPPLY t  left join tb_user tu on t.user_id=tu.ss_id where  tu.dwmc=?  and to_char(t.start_time,'yyyy-MM-dd')<=? and to_char(t.end_time,'yyyy-MM-dd')>=? and complete=2  ", dwmc, endtime, starttime);


        //获取与请假时间有交集的请假数据
        for (Map<String, Object> map : list) {
            //有交集计算
            if (isOverlap(trimToEmpty(map.get("START_TIME")), trimToEmpty(map.get("END_TIME")), starttime, endtime, "yyyy-MM-dd")) {
                jiaojilist.add(map);
            }
        }
        //所有交集数据中，最大交集数
        int max = 0;
        for (Map<String, Object> map : jiaojilist) {
            String starttime_map = trimToEmpty(map.get("START_TIME"));
            String endtime_map = trimToEmpty(map.get("END_TIME"));

            int i = 0;
            for (Map<String, Object> map_in : jiaojilist) {
                if (isOverlap(trimToEmpty(map_in.get("START_TIME")), trimToEmpty(map_in.get("END_TIME")), starttime_map, endtime_map, "yyyy-MM-dd")) {
                    i++;
                }
                max = i > max ? i : max;//max原本需要减去与自己的交集-1，再加上和请假时间的交集+1，此处不做运算
            }


        }

        if (max >= count) {
            ifLeave = true;
        }


//        return ifLeave;
        return false;//在位率去除
    }

    /**
     * 用户请假信息保存(提示查询)
     *
     * @return
     */
    @RequestMapping(value = "/user/leavebefore")
    @ResponseBody
    public Object LeaveBefore() {
        ResultEntity r = new ResultEntity(1);
        List<Map<String, Object>> leaveins = new ArrayList<>();
        List<Map<String, Object>> leaveList = new ArrayList<>();
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }


            //请假时间，需要拆分
            String START_TIME = trimToEmpty(param.get("START_TIME"));
            String END_TIME = trimToEmpty(param.get("END_TIME"));
            String USER_ID = userEntity.getUser_id();

            if (!"".equals(trimToEmpty(param.get("BUSINESSID")))) {
                //请假时间
                Map<String, Object> map = leaveService.getTaskByBussinessId(trimToEmpty(param.get("BUSINESSID")));
                START_TIME = trimToEmpty(map.get("START_TIME"));
                END_TIME = trimToEmpty(map.get("END_TIME"));
                USER_ID = trimToEmpty(map.get("USER_ID"));

            }


            List<Map<String, Object>> list = DBHelper.queryForList(" select t.id,t.process_instance_id,t.user_id,to_char(t.start_time,'yyyy-mm-dd hh24:mi') as start_time,to_char(t.end_time,'yyyy-mm-dd hh24:mi') as end_time,t.reason,t.apply_time,t.reality_start_time,t.reality_end_time,t.leave_space,t.vacation,t.complete,t.leave_days " +
                    " from TB_LEAVEAPPLY t where t.user_id=?  and to_char(t.apply_time,'yyyy')=to_char(sysdate,'yyyy') ", USER_ID);

            String auth = "false";
            for (Map<String, Object> map : list) {
                if ((trimToEmpty(map.get("id"))).equals(trimToEmpty(param.get("BUSINESSID")))) {
                    continue;
                }
                String start_time = trimToEmpty(map.get("START_TIME"));
                String end_time = trimToEmpty(map.get("END_TIME"));
                if (isOverlap(START_TIME, END_TIME, start_time, end_time, "yyyy-MM-dd HH:mm")) {
                    auth = "true";
                    break;
                }
            }


            Map<String, Object> map = new HashMap<>();
            map.put("auth", auth);
            leaveins.add(map);
            r.setDataset(leaveins);
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


    //true 交集，false 不交集
    private static boolean isOverlap(String startdate1, String enddate1, String startdate2, String enddate2, String type) {
        boolean flag = false;
        SimpleDateFormat format = new SimpleDateFormat(type);//注意月份是MM
        Date leftStartDate = null;
        Date leftEndDate = null;
        Date rightStartDate = null;
        Date rightEndDate = null;
        try {
            leftStartDate = format.parse(startdate1);
            leftEndDate = format.parse(enddate1);
            rightStartDate = format.parse(startdate2);
            rightEndDate = format.parse(enddate2);
        } catch (ParseException e) {
            return false;
        }

        if ((leftEndDate.getTime() < rightStartDate.getTime()) || rightEndDate.getTime() < leftStartDate.getTime()) {
            flag = false;
        } else {
            flag = true;
        }

        return flag;
    }


    //我的待办
    @RequestMapping(value = "/user/getpagedepttask")
    @ResponseBody
    public Object getPageDeptTask() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            //分页
            int rowcount = trimToNum(param.get("ROWCOUNT"));
            int current = trimToNum(param.get("CURRENT"));


            int firstrow = (current - 1) * rowcount;
            //查询我的待办
            result = leaveService.getPageDeptTask(trimToEmpty(userEntity.getUser_id()), firstrow, rowcount);
            //统计
            int count = leaveService.getPageDeptTaskCount(trimToEmpty(userEntity.getUser_id()));
            DataGrid<Map<String, Object>> grid = new DataGrid<Map<String, Object>>();
            grid.setRowCount(rowcount);
            grid.setCurrent(current);
            grid.setTotal(count);
            grid.setRows(result);

            Map<String, Object> map = new HashMap<>();
            map.put("grid", grid);
            results.add(map);
            r.setDataset(results);
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


    //我的待办 (app)
    @RequestMapping(value = "/user/gettaskforapp")
    @ResponseBody
    public Object getTaskForApp() {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }

            result = leaveService.getTaskForApp(trimToEmpty(userEntity.getUser_id()));

            for (Map<String, Object> map : result) {
                String resultstr = "";
//            南环路站队曹杰锋请（4天）事假,请您处理
                if (result.size() > 0) {
                    resultstr = trimToEmpty(map.get("DWMC")) + trimToEmpty(map.get("NAME")) + "请（" + trimToEmpty(map.get("LEAVE_DAYS")) + "天）" + trimToEmpty(map.get("LEAVE_TYPE_NAME")) + ",请您处理";
                    map.put("STR", resultstr);
                }
            }


            r.setDataset(result);
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


    //我的待办，已办，申请 count
    @RequestMapping(value = "/user/getmytaskcount")
    @ResponseBody
    public Object getPageDeptTaskCount() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            //用户信息
            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            //待办
            int dbcount = leaveService.getPageDeptTaskCount(trimToEmpty(userEntity.getUser_id()));
            ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
            //已办
            List<RunningProcess> list = new ArrayList<RunningProcess>();

            List<HistoricTaskInstance> taskList = historyService.createHistoricTaskInstanceQuery()
                    .taskAssignee(userEntity.getUser_id())
                    .finished()
                    .list();

            for (HistoricTaskInstance h : taskList) {
                RunningProcess process = new RunningProcess();
                Map<String, Object> map = leaveService.getTaskByProcessInstanceId(h.getProcessInstanceId());
                if (!trimToEmpty(map.get("ss_id")).equals(userEntity.getUser_id())) {
                    process.setMap(map);
                    list.add(process);
                }
            }
            //申请
            String sqcount = leaveService.getPageLeaveCount(trimToEmpty(userEntity.getUser_id()));
            //保存
            String bccount = leaveService.getSaveLeaveCount(trimToEmpty(userEntity.getUser_id()));


            Map<String, Object> map = new HashMap<>();
            map.put("dbcount", dbcount + "");
            map.put("ybcount", list.size() + "");
            map.put("sqcount", sqcount);
            map.put("bccount", bccount);
            results.add(map);
            r.setDataset(results);
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


    //我的待办(详情)
    @RequestMapping(value = "/user/getdepttask")
    @ResponseBody
    public Object getDeptTask() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            //用户信息(受理人)
            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            //业务主键
            String BUSINESSID = trimToEmpty(param.get("BUSINESSID"));

            //查询我的待办
            Map<String, Object> task = leaveService.getTaskByBussinessId(BUSINESSID);

            Map<String, Object> user = getUserInfo(trimToEmpty(task.get("SS_ID")));

            results.add(user);
            results.add(task);
            r.setDataset(results);
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


    //我的结办
    @RequestMapping(value = "/user/getpagefinishedtask")
    @ResponseBody
    public Object getPageFinishedTask() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            //用户信息
            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            //分页
            int rowcount = trimToNum(param.get("ROWCOUNT"));
            int current = trimToNum(param.get("CURRENT"));

            HistoricProcessInstanceQuery process = historyService.createHistoricProcessInstanceQuery()
                    .processDefinitionKey("leave").startedBy(trimToEmpty(userEntity.getUser_id())).finished();
            int total = (int) process.count();
            int firstrow = (current - 1) * rowcount;
            List<HistoricProcessInstance> info = process.listPage(firstrow, rowcount);
            List<HistoryProcess> list = new ArrayList<HistoryProcess>();
            for (HistoricProcessInstance history : info) {
                HistoryProcess his = new HistoryProcess();
                String bussinesskey = history.getBusinessKey();
                Map<String, Object> apply = leaveService.getTaskByBussinessId(bussinesskey);
                his.setLeaveMap(apply);
                his.setBusinessKey(bussinesskey);
                his.setProcessDefinitionId(history.getProcessDefinitionId());
                list.add(his);
            }

            DataGrid<Map<String, Object>> grid = new DataGrid<Map<String, Object>>();
            grid.setRowCount(rowcount);
            grid.setCurrent(current);
            grid.setTotal(total);
            grid.setRows(result);

            Map<String, Object> map = new HashMap<>();
            map.put("grid", grid);
            results.add(map);
            r.setDataset(results);
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


    //我的申请（我的请假）
    @RequestMapping(value = "/user/getmyleaves")
    @ResponseBody
    public Object getPageLeave() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            //用户信息
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            //分页
            int rowcount = trimToNum(param.get("ROWCOUNT"));
            int current = trimToNum(param.get("CURRENT"));
            Limit limit = new Limit(param);
            if(rowcount==0 && current==0){
                rowcount = limit.getSize();
                current = limit.getStart();
            }
            //分页查询我的申请（直接查数据库leave表）
            Page<?> page = leaveService.getPageLeave(trimToEmpty(userEntity.getUser_id()), current, rowcount);

            r.setDataset(page);
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


    //我的草稿
    @RequestMapping(value = "/user/getsaveleaves")
    @ResponseBody
    public Object getSaveLeave() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            //用户信息
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            //分页
            int rowcount = trimToNum(param.get("ROWCOUNT"));
            int current = trimToNum(param.get("CURRENT"));
            Limit limit = new Limit(param);
            if(rowcount==0 && current==0){
                rowcount = limit.getSize();
                current = limit.getStart();
            }
            //分页查询我的申请（直接查数据库leave表）
            Page<?> page = leaveService.getSaveLeave(trimToEmpty(userEntity.getUser_id()), current, rowcount);

            r.setDataset(page);
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
     * 人员分析列表未提交
     *
     * @return
     */
    @RequestMapping(value = "user/wtj/list")
    @ResponseBody
    public Object wtj() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId = user.get("SS_ID").toString();
        param.put("userId", userId);
        Page<?> page;
        try {
            page = ryfxMoblieService.findwtjryfxPage(param);
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
     * 人员分析列表已提交
     *
     * @return
     */
    @RequestMapping(value = "user/ytj/list")
    @ResponseBody
    public Object ytj() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId = user.get("SS_ID").toString();
        param.put("userId", userId);
        Page<?> page;
        try {
            page = ryfxMoblieService.findytjryfxPage(param);
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
     * 人员分析已归档列表
     *
     * @return
     */
    @RequestMapping(value = "user/ygd/list")
    @ResponseBody
    public Object ygd() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId = user.get("SS_ID").toString();
        param.put("userId", userId);
        Page<?> page;
        try {
            page = ryfxMoblieService.findygdryfxPage(param);
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
     * 人员分析未归档列表
     *
     * @return
     */
    @RequestMapping(value = "user/wgd/list")
    @ResponseBody
    public Object wgd() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId = user.get("SS_ID").toString();
        param.put("userId", userId);
        Page<?> page;
        try {
            page = ryfxMoblieService.findwgdryfxPage(param);
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
     * to新增人员分析
     *
     * @param
     * @return
     */
    @RequestMapping(value = "user/ryfx/add/{id}")
    @ResponseBody
    @Transactional(value = "primaryTransactionManager")
    public Object ryfxadd(@PathVariable(required = false) String id) {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId = user.get("SS_ID").toString();
        param.put("id", id);
        param.put("userId", userId);
        Page<?> page;
        try {
            page = ryfxMoblieService.getryfx(param);
            r.setDataset(page.getRows());
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
     * to新增归档页面
     *
     * @param
     * @return
     */
    @RequestMapping(value = "user/rygd/add/{id}")
    @ResponseBody
    @Transactional(value = "primaryTransactionManager")
    public Object rygdadd(@PathVariable(required = false) String id) {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId = user.get("SS_ID").toString();
        param.put("id", id);
        param.put("userId", userId);
        Page<?> page;
        try {
            page = ryfxMoblieService.getrygd(param);
            r.setDataset(page.getRows());
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
     * 保存/提交人员分析
     *
     * @return
     */
    @RequestMapping(value = "user/ryfx/tj")
    @ResponseBody
    @Transactional(value = "primaryTransactionManager")
    public Object ryfxtj() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId = user.get("SS_ID").toString();
        param.put("userId", userId);
        Page<?> page;
        try {
            ryfxMoblieService.tj(param);
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
     * 月度消费
     *
     * @return
     */
    @RequestMapping(value = "user/ydxf/save")
    @ResponseBody
    @Transactional(value = "primaryTransactionManager")
    public Object ydxfbc(String ss_id, String phone, String textbody, String deviceId) {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            String userId = user.get("SS_ID").toString();
            if (userId == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            if (ss_id == null) {
                r.setCode(-1);
                r.setMsg("请输入ss_id！");
                return r;
            }

            if (phone == null) {
                r.setCode(-1);
                r.setMsg("请输入电话！");
                return r;
            }
            if (textbody == null) {
                r.setCode(-1);
                r.setMsg("请输入消费内容！");
                return r;
            }
            if (deviceId == null) {
                r.setCode(-1);
                r.setMsg("请输入IMEL设备号！");
                return r;
            }
            ydxfService.save(param);
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
     * 保存人员分析
     *
     * @return
     */
    @RequestMapping(value = "user/ryfx/save")
    @ResponseBody
    @Transactional(value = "primaryTransactionManager")
    public Object ryfxsave(String dwmc, String sex, String nl, String mz, String zj, String sg, String jbzl, String grczs, String jszt, String stzt, String shgn, String grzxqk, String czwt, String dcfx,String rystatus, String ss_id) {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
            String userId = user.get("SS_ID").toString();
//            param.put("ss_id",userId);
            if (userId == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            if (dwmc == null) {
                r.setCode(-1);
                r.setMsg("选择单位名称！");
                return r;
            }

            if (sex == null) {
                r.setCode(-1);
                r.setMsg("输入性别！");
                return r;
            }
            if (nl == null) {
                r.setCode(-1);
                r.setMsg("输入年龄！");
                return r;
            }
            if (mz == null) {
                r.setCode(-1);
                r.setMsg("输入名族！");
                return r;
            }
            if (zj == null) {
                r.setCode(-1);
                r.setMsg("输入职级！");
                return r;
            }
            if (sg == null) {
                r.setCode(-1);
                r.setMsg("输入身高！");
                return r;
            }
            if (jbzl == null) {
                r.setCode(-1);
                r.setMsg("输入基本资料！");
                return r;
            }
            if (grczs == null) {
                r.setCode(-1);
                r.setMsg("输入个人成长史！");
                return r;
            }
            if (jszt == null) {
                r.setCode(-1);
                r.setMsg("输入精神状态！");
                return r;
            }
            if (stzt == null) {
                r.setCode(-1);
                r.setMsg("输入身体状态！");
                return r;
            }
            if (shgn == null) {
                r.setCode(-1);
                r.setMsg("输入社会功能！");
                return r;
            }
            if (grzxqk == null) {
                r.setCode(-1);
                r.setMsg("输入个人征信情况！");
                return r;
            }
            if (czwt == null) {
                r.setCode(-1);
                r.setMsg("输入存在问题！");
                return r;
            }
            if (dcfx == null) {
                r.setCode(-1);
                r.setMsg("输入对策分析！");
                return r;
            }
//            if (sftj == null) {
//                r.setCode(-1);
//                r.setMsg("输入提交状态！");
//                return r;
//            }

            if (ss_id == null) {
                r.setCode(-1);
                r.setMsg("输入用户id！");
                return r;
            }
            ryfxMoblieService.bc(param);
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

    //我参与的流程(history) (流程未结束)我的已办
    @RequestMapping(value = "/user/getmyleavesbyprocess")
    @ResponseBody
    public Object getMyLeaves() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            //用户信息
//            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }

            int rowcount = trimToNum(param.get("ROWCOUNT"));
            int current = trimToNum(param.get("CURRENT"));
            //分页
            int firstrow = (current - 1) * rowcount;

            List<RunningProcess> list = new ArrayList<RunningProcess>();

            List<HistoricTaskInstance> taskList = historyService.createHistoricTaskInstanceQuery()
                    .taskAssignee(userEntity.getUser_id())
                    .finished()
                    .listPage(firstrow, rowcount);

            List<HistoricTaskInstance> alltaskList = historyService.createHistoricTaskInstanceQuery()
                    .taskAssignee(userEntity.getUser_id())
                    .finished()
                    .list();

            for (HistoricTaskInstance h : taskList) {
                RunningProcess process = new RunningProcess();
                Map<String, Object> map = leaveService.getTaskByProcessInstanceId(h.getProcessInstanceId());
                if (!trimToEmpty(map.get("ss_id")).equals(userEntity.getUser_id())) {
                    process.setMap(map);
                    list.add(process);
                }
            }

            DataGrid<RunningProcess> grid = new DataGrid<RunningProcess>();
            grid.setCurrent(current);
            grid.setRowCount(rowcount);
            grid.setTotal(alltaskList.size());
            grid.setRows(list);

            Map<String, Object> map = new HashMap<>();
            map.put("grid", grid);

            results.add(map);
            r.setDataset(results);
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


    //单个流程(业务主键查询)
    @RequestMapping(value = "/user/gettaskbybussinessid")
    @ResponseBody
    public Object getTaskByBussinessId() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            String businesskey = trimToEmpty(param.get("BUSINESSID"));
            //用户信息
//            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            Map<String, Object> map = leaveService.getTaskByBussinessId(businesskey);
            results.add(map);
            r.setDataset(results);
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


    //任务主键、下一级审批人、和批注还有是否通过
    @RequestMapping(value = "/user/leavesp")
    @ResponseBody
    @Transactional(value = "primaryTransactionManager")
    public Object leaveSp() {
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        ResultEntity r = new ResultEntity(1);
        List<Map<String, Object>> sjlist = new ArrayList<>();
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            String businesskey = trimToEmpty(param.get("BUSINESSID"));
            String audit = trimToEmpty(param.get("AUDIT"));
            String spr = trimToEmpty(param.get("SPR"));
            String pz = trimToEmpty(param.get("PZ"));
            //奖励假期
            String leave_days_reward = "";
            String ForReward = trimToEmpty(param.get("ForReward"));
            if (ForReward.equals("1")) {
                leave_days_reward = trimToEmpty(param.get("JLJQ"));
            }


            //用户信息
            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }

            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("audit", trimToNum(audit));

            int LEAVE_DAYS = 0;

            String START_TIME = "";
            String END_TIME = "";
            if (!"".equals(trimToEmpty(param.get("BUSINESSID")))) {
                //请假信息
                map = leaveService.getTaskByBussinessId(businesskey);
                START_TIME = trimToEmpty(map.get("START_TIME"));
                END_TIME = trimToEmpty(map.get("END_TIME"));
            }
            variables.put("sprForJg", trimToEmpty(spr));
            variables.put("ssIdForJg", trimToEmpty(map.get("SS_ID")));

            //计算请假天数
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date1 = simpleDateFormat.parse(START_TIME);
            Date date2 = simpleDateFormat.parse(END_TIME);
            LEAVE_DAYS = getDayDiffer(date1, date2);
            //流程走向计算
            int DaysForActiviti = getDayDifferForActiviti(date1, date2);

            //不通过
            if ("0".equals(trimToEmpty(audit))) {
                //修改leave表状态为0 "保存"    3 退回
                leaveService.updateStatus(businesskey, "3");
                variables.put("applyUserId", "");
                //发消息给所有历史审批人
                leaveService.leaveSp(trimToEmpty(map.get("PROCESS_INSTANCE_ID")), variables, pz, userEntity.getUser_id());
                leaveService.sendAllSpr(trimToEmpty(map.get("PROCESS_INSTANCE_ID")),pz,userEntity.getUser_id());


                //极光 退回
                leaveService.messageOfth(trimToEmpty(map.get("SS_ID")));
                map.put("审批环节", "success");
                results.add(map);
                r.setDataset(results);
                r.setMsg("操作成功");
                return r;
            }


            if ((trimToEmpty(usermap.get("JOB"))).indexOf("班长") > -1) {
                //审批人审批阶段(班长选择)
                List<String> assigneeList = new ArrayList<String>();
                sjlist("3", "7", trimToEmpty(usermap.get("DWMC_TRUE")), false).forEach(item->{
                    assigneeList.add(item.get("id")+"");
                });
                variables.put("ZdAssigneeList", assigneeList);


                //极光 审批
//                leaveService.messageOfsp(assigneeList, trimToEmpty(map.get("SS_ID")),START_TIME,END_TIME);
//                sjlist = sjlist("4", "5", trimToEmpty(usermap.get("DWMC_TRUE")), false);






            } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("站长") > -1)||((trimToEmpty(usermap.get("JOB"))).indexOf("指导员") > -1)) {
                //一天以上不需要选择下一级的审批人
                if (DaysForActiviti >= 1) {
                    //查询站长 的上级 大队长和教导员
//                    variables.put("DdAudit", trimToEmpty(spr));

                    List<String> assigneeList = new ArrayList<String>();
                    sjlist("1", "2", trimToEmpty(usermap.get("DWMC_TRUE")), true).forEach(item->{
                        assigneeList.add(item.get("id")+"");
                    });
                    variables.put("DdAssigneeList", assigneeList);

                    //极光 审批
                    leaveService.messageOfsp(assigneeList, trimToEmpty(map.get("SS_ID")),START_TIME,END_TIME);
                } else {
                    //结束流程

                    leaveService.updateStatus(businesskey, "2");
                    //极光 通过
                    leaveService.messageOfPass(trimToEmpty(map.get("SS_ID")));
                }
//                sjlist = sjlist("3", "7", trimToEmpty(usermap.get("DWMC_TRUE")), false);

            } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("大队长") > -1) || ((trimToEmpty(usermap.get("JOB"))).indexOf("教导员") > -1)) {
                //查询大队长和教导员 的上级 队务科科长
                variables.put("JwkAudit", trimToEmpty(spr));
                //极光 审批new ArrayList<String>(Arrays.asList("o1", "o2"));
                leaveService.messageOfsp(new ArrayList<String>(Arrays.asList(spr)), trimToEmpty(map.get("SS_ID")),START_TIME,END_TIME);
                sjlist = sjlist("1", "2", trimToEmpty(usermap.get("DWMC_TRUE")), false);

            } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("队务科") > -1)) {
                //查询队务科长 的上级 政治部主任
                variables.put("CmzAudit", trimToEmpty(spr));
                //极光 审批
                leaveService.messageOfsp(new ArrayList<String>(Arrays.asList(spr)), trimToEmpty(map.get("SS_ID")),START_TIME,END_TIME);
                //奖励假期 修改
                leaveService.updateReward(businesskey, leave_days_reward);
                sjlist = sjlist("0", "未知", trimToEmpty(usermap.get("DWMC_TRUE")), false);


            } else if (((trimToEmpty(usermap.get("JOB"))).indexOf("政治部") > -1)) {
                //结束流程
                leaveService.updateStatus(businesskey, "2");
                //极光 通过
                leaveService.messageOfPass(trimToEmpty(map.get("SS_ID")));
                sjlist = sjlist("x", "未知", trimToEmpty(usermap.get("DWMC_TRUE")), false);

            }
            if (sjlist.size()!=0){
                //给每个领导发消息
                leaveService.messageForEvery(sjlist,map.get("DWMC")+"",map.get("NAME")+"",userEntity.getUser_id());
            }

            //审批
            leaveService.leaveSp(trimToEmpty(map.get("PROCESS_INSTANCE_ID")), variables, pz, userEntity.getUser_id());

            map.put("审批环节", "success");
            results.add(map);
            r.setDataset(results);
            r.setMsg("操作成功");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (Exception E) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(E.getMessage(), E);
        } finally {
        }


        return r;

    }

    //单个流程(业务主键查询)
    @RequestMapping(value = "/user/gethistorybybussinessid")
    @ResponseBody
    public Object getHistoryByBussinessId() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            String businesskey = trimToEmpty(param.get("BUSINESSID"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            Map<String, Object> map = leaveService.getTaskByBussinessId(businesskey);

            List<Map<String, String>> result = leaveService.getHistoryByBussinessId(trimToEmpty(map.get("PROCESS_INSTANCE_ID")));

            r.setDataset(result);
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


    //单个流程(业务主键查询)
    @RequestMapping(value = "/user/leavedelete")
    @ResponseBody
    public Object leaveDelete() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            String businesskey = trimToEmpty(param.get("BUSINESSID"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            Map<String, Object> map = leaveService.getTaskByBussinessId(businesskey);

            leaveService.deleteTaskByBussinessId(businesskey);

            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().
                    processInstanceBusinessKey(businesskey + "").
                    singleResult();

            String processInstanceId = hpi.getId(); //流程实例ID
            //　　判断该流程实例是否结束，未结束和结束两者删除表的信息是不一样的。
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
                    .singleResult();

            if (pi == null) {
                //该流程实例已经完成了
                historyService.deleteHistoricProcessInstance(processInstanceId);
            } else {
                //该流程实例未结束的
                runtimeService.suspendProcessInstanceById(processInstanceId);//挂起流程
                runtimeService.deleteProcessInstance(processInstanceId, "");
                historyService.deleteHistoricProcessInstance(processInstanceId);//(顺序不能换)
            }
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


    //判断当前用户是否可以请假
    @RequestMapping(value = "/user/getleaveauth")
    @ResponseBody
    public Object getLeaveAuth() {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            //用户信息
            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            String job = trimToEmpty(usermap.get("JOB_TRUE"));
            Map<String, Object> map = new HashMap<>();
            if ("7".equals(job)||job.equals("3") || job.equals("2") || job.equals("1") || job.equals("0") || job.equals("x")) {
                map.put("auth", 0);
            } else {
                map.put("auth", 1);
            }
            result.add(map);
            r.setDataset(result);
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

    //判断当前战备状态
    @RequestMapping(value = "/user/getleavezbzt")
    @ResponseBody
    public Object getLeaveZbzt() {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, Object> map = new HashMap<>();
            String zbzt = zbztService.query();//3正常 2二级战备 1一级战备
            map.put("zbzt", zbzt);
            result.add(map);
            r.setDataset(result);
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


    public static String trimToEmpty(final Object str) {
        return str == null ? "" : (str + "").trim();
    }

    public static int trimToNum(final Object str) {
        return ((str == null) || str.equals("")) ? 0 : Integer.parseInt((str + "").trim());
    }

    @Override
    public Map<String, Object> getUserInfo(String userId) {
        //用户信息
        Map<String, Object> usermap = DBHelper.queryForMap("select  t.id,t.phone,t.email,t.idcard,t.name," +
                "t.avatar,t.acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)job,job as job_true," +
                "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK) as POLICERANK_NAME,POLICERANK, " +
                "to_char(CREATETIME,'yyyy-mm-dd hh24:mi')CREATETIME,to_char(UPDATETIME,'yyyy-mm-dd hh24:mi')UPDATETIME," +
                "RWNY,SS_ID,SFKYLDZS,TQXJTSSFJB,NXJTS,TQXJTS,NATIVE_PLACE,d.DEPTNAME as DWMC,DWMC as DWMC_TRUE    " +
                "  from   tb_user t  LEFT JOIN SS_DEPT D  ON D.id = t.DWMC  where t.SS_ID=? ", userId);
        return usermap;
    }


    //天数计算（开始时间-结束时间）
    public static int getDayDiffer(Date startDate, Date endDate) throws ParseException {
        int days = 0;
        //判断是否跨年
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat hourFormat = new SimpleDateFormat("HHmm");
        String startYear = yearFormat.format(startDate);
        String endYear = yearFormat.format(endDate);
        String endhour = hourFormat.format(endDate);
        if (startYear.equals(endYear)) {
            /*  使用Calendar跨年的情况会出现问题    */
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            int startDay = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTime(endDate);
            int endDay = calendar.get(Calendar.DAY_OF_YEAR);
            if (trimToEmpty(startDay).equals(trimToEmpty(endDay))) {
                //变更，计算同一天情况下，结束时间是否超过18点，超过1，不超0，不是同一天1
                days = 1;
            } else {
                days = endDay - startDay + 1;
            }
        } else {
            /*  跨年不会出现问题，需要注意不满24小时情况（2016-03-18 11:59:59 和 2016-03-19 00:00:01的话差值为 0）  */
            //  只格式化日期，消除不满24小时影响
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            long startDateTime = dateFormat.parse(dateFormat.format(startDate)).getTime();
            long endDateTime = dateFormat.parse(dateFormat.format(endDate)).getTime();
            days = (int) ((endDateTime - startDateTime) / (1000 * 3600 * 24));
        }

        return days;
    }

    /**
     * 定时
     */
//    @Scheduled(cron = "0/5 * * * * ?") //第一次延迟1秒后执行,上一次执行完毕时间点之后5秒再执行
//    public  static  void jcghsjTask() {
//        SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        System.out.println("开始时间"+sm.format(new Date()));
//        try {
//            List<Map<String,Object>>list=DBHelper.queryForList("SELECT  account  from  TB_KQ_ATTENDANCE ");
//            if (list!=null){
//                for(int i=0;i<list.size();i++){
//                    List<Map<String,Object>>dqlist=DBHelper.queryForList("select t.imel  from TB_KQ_ATTENDANCE t  where  account = ? and t.imel is  not null  group by  t.imel",list.get(i).get("ACCOUNT").toString());
//                    System.out.println(list.get(i).toString());
//                    if(dqlist.size()>3){
//                        throw  new JSONException(list.get(i)+"经常更换手机,频繁打卡");
//                    }
//                }
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//            System.out.println(e);
//        }
//        System.out.println("完成时间"+sm.format(new Date()));
//
//    }


    /**
     * 用户请假信息保存(提示查询) 时间是否交集
     *
     * @return
     */
    @RequestMapping(value = "/user/leavejljq")
    @ResponseBody
    public Object leaveJljq() {
        ResultEntity r = new ResultEntity(1);
        List<Map<String, Object>> leaveins = new ArrayList<>();
        List<Map<String, Object>> leaveList = new ArrayList<>();
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());
            String auth = "false";
            if (usermap.get("JOB").equals("队务科长")) {
                auth = "true";
            }

            Map<String, Object> map = new HashMap<>();
            map.put("auth", auth);
            leaveins.add(map);
            r.setDataset(leaveins);
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


    //我的消息
    @RequestMapping(value = "/user/getmessage")
    @ResponseBody
    public Object getMessage() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            //用户信息
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            //分页
            int rowcount = trimToNum(param.get("ROWCOUNT"));
            int current = trimToNum(param.get("CURRENT"));

            //分页查询我的申请（直接查数据库leave表）
            Page<?> page = leaveService.getMessage(trimToEmpty(userEntity.getUser_id()), current, rowcount);

            r.setDataset(page);
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

    //销假
    @RequestMapping(value = "/user/leavereport")
    @ResponseBody
    public Object leaveReport() {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }

            String businessid = trimToEmpty(param.get("BUSINESSID"));

            leaveService.updateReport(businessid);

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

    //TODO
//###############################统计部分#################################
    //我的请假列表
    @RequestMapping(value = "/user/getpageleaveforH5")
    @ResponseBody
    public Object getPageLeaveForH5() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            //用户信息
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            String starttime = trimToEmpty(param.get("STARTTIME"));
            String endtime = trimToEmpty(param.get("ENDTIME"));

            //分页
            int rowcount = trimToNum(param.get("ROWCOUNT"));
            int current = trimToNum(param.get("CURRENT"));

            //分页查询我的申请（直接查数据库leave表）
            Page<?> page = leaveService.getPageLeaveForH5(trimToEmpty(userEntity.getUser_id()), "", current, rowcount, starttime, endtime);

            r.setDataset(page);
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

    //我的请假列表
    @RequestMapping(value = "/user/getpageleaveforH5ForYour")
    @ResponseBody
    public Object getPageLeaveForH5ForYour() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUserByuserId(param.get("ssId"));

            //用户信息
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            String starttime = trimToEmpty(param.get("STARTTIME"));
            String endtime = trimToEmpty(param.get("ENDTIME"));

            //分页
            int rowcount = trimToNum(param.get("ROWCOUNT"));
            int current = trimToNum(param.get("CURRENT"));

            //分页查询我的申请（直接查数据库leave表）
            Page<?> page = leaveService.getPageLeaveForH5(trimToEmpty(userEntity.getUser_id()), "", current, rowcount, starttime, endtime);

            r.setDataset(page);
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

    //单位字典
    @RequestMapping(value = "/user/getdwmc")
    @ResponseBody
    public Object getDwmc() {

        ResultEntity r = new ResultEntity(1);
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            //用户信息
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }

            //用户信息
            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());
            //根据实际权级展示对应部职级数据
            if ((trimToEmpty(usermap.get("JOB")).contains("站长")) || (trimToEmpty(usermap.get("JOB")).contains("指导员"))) {
                results = DBHelper.queryForList("select t.id,t.deptname,deptno from ss_dept t where t.isunit=1 and t.id=?", trimToEmpty(usermap.get("DWMC_TRUE")));
            } else if ((trimToEmpty(usermap.get("JOB")).contains("大队长")) || (trimToEmpty(usermap.get("JOB")).contains("教导员"))) {
                results = DBHelper.queryForList("select t.id,t.deptname,deptno from ss_dept t where t.isunit=1 and t.parentid=?", trimToEmpty(usermap.get("DWMC_TRUE")));
            } else if ((trimToEmpty(usermap.get("JOB")).contains("队务科")) || (trimToEmpty(usermap.get("JOB")).contains("政治部主任"))) {
                results = DBHelper.queryForList("select t.id,t.deptname,deptno from ss_dept t where t.isunit=1 ");
            }

            r.setDataset(results);
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

    //上级查看 请假
    @RequestMapping(value = "/user/getpageleaveByZDforH5")
    @ResponseBody
    public Object getpageleaveByZDforH5() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));


            //用户信息
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());

            String starttime = trimToEmpty(param.get("STARTTIME"));
            String endtime = trimToEmpty(param.get("ENDTIME"));

            //分页
            int rowcount = trimToNum(param.get("ROWCOUNT"));
            int current = trimToNum(param.get("CURRENT"));
            Page<?> page = null;

            String dwmc = trimToEmpty(param.get("DWMC"));


            if (!"".equals(trimToEmpty(usermap.get("JOB")))
                    ||!"消防员".equals(trimToEmpty(usermap.get("JOB")))
                    ||!"班长".equals(trimToEmpty(usermap.get("JOB")))
            ) {
                page = leaveService.getPageLeaveForCount(dwmc, usermap, current, rowcount);
                //增加请假天数count
                ((List<Map<String, Object>>)page.getRows()).stream().forEach(item -> {
                    item.putAll(leaveService.countLeave(item.get("SS_ID")+"")) ;
                });
            }

            r.setDataset(page);
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


    //上级查看 在位率
    @RequestMapping(value = "/user/getpageZWLforH5")
    @ResponseBody
    public Object getpageZWLforH5() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            //用户信息
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            String dwmc = trimToEmpty(param.get("DWMC"));
            List<Map<String, Object>> dwList = new ArrayList<>();
            //dwmc 为null，全查
            if (StringUtils.isNotBlank(dwmc)) {
                dwList = DBHelper.queryForList("select t.id,t.deptname,deptno from ss_dept t where t.isunit=1 and t.id=?", dwmc);
            } else {
                Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());
                //根据实际权级展示对应部职级数据
                if ((trimToEmpty(usermap.get("JOB")).indexOf("站长") > -1) || (trimToEmpty(usermap.get("JOB")).indexOf("指导员") > -1)) {
                    dwList = DBHelper.queryForList("select t.id,t.deptname,deptno from ss_dept t where t.isunit=1 and t.id=?", trimToEmpty(usermap.get("DWMC_TRUE")));
                } else if ((trimToEmpty(usermap.get("JOB")).indexOf("大队长") > -1) || (trimToEmpty(usermap.get("JOB")).indexOf("教导员") > -1)) {
                    dwList = DBHelper.queryForList("select t.id,t.deptname,deptno from ss_dept t where t.isunit=1 and t.parentid=?", trimToEmpty(usermap.get("DWMC_TRUE")));
                } else if ((trimToEmpty(usermap.get("JOB")).indexOf("队务科") > -1) || (trimToEmpty(usermap.get("JOB")).indexOf("政治部主任") > -1)) {
                    dwList = DBHelper.queryForList("select t.id,t.deptname,deptno from ss_dept t where t.isunit=1 ");
                }
            }

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


    //计算同一天情况下，结束时间是否超过18点，超过1，不超0，不是同一天1
    public int getDayDifferForActiviti(Date startDate, Date endDate) throws ParseException {
        int days = 1;
        //判断是否跨年
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat hourFormat = new SimpleDateFormat("HHmm");
        String startYear = yearFormat.format(startDate);
        String endYear = yearFormat.format(endDate);
        String endhour = hourFormat.format(endDate);
        //战备状态
        String zbzt = zbztService.query();//3正常 2二级战备 1一级战备
        if (zbzt.equals("2")) {//战备状态为2时，请假均走复杂流程
            return 1;
        }
        if (startYear.equals(endYear)) {
            /*  使用Calendar跨年的情况会出现问题    */
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            int startDay = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTime(endDate);
            int endDay = calendar.get(Calendar.DAY_OF_YEAR);
            if (trimToEmpty(startDay).equals(trimToEmpty(endDay))) {
                if (trimToNum(endhour) > 1800) {
                    days = 1;
                } else {
                    days = 0;
                }
            } else {
                days = 1;
            }
        } else {
            days = 1;
        }

        return days;
    }

    //计算当年中，排除掉节假日的请假天数
    public static int getDaysByHoliday(Date startDate, Date endDate, int allDays) throws ParseException {


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        Date startDate1 = dateFormat.parse(dateFormat.format(startDate));
        Date endDate1 = dateFormat.parse(dateFormat.format(endDate));

        Calendar tempStart = Calendar.getInstance();

        tempStart.setTime(startDate1);

        Calendar tempEnd = Calendar.getInstance();
        tempEnd.setTime(endDate1);
        while (tempStart.before(tempEnd) || tempStart.equals(tempEnd)) {
            String count = DBHelper.queryForScalar("select count(1) from business_dict t where t.type_code='jjr' and value=? and status=1 ", String.class, dateFormat.format(tempStart.getTime()));
            if ("1".equals(count)) {
                allDays--;
            }
            tempStart.add(Calendar.DAY_OF_YEAR, 1);

        }


        return allDays;
    }

    //点击在位置率，查看详情
    @RequestMapping(value = "/user/getpageZWLXQforH5")
    @ResponseBody
    @Transactional(value = "primaryTransactionManager")
    public Object getpageZWLXQforH5(){
        ResultEntity r = new ResultEntity(1);
        try{
            Map<String, String> param = RequestUtils.toMap(request);
            List<Map<String, Object>> maps = leaveService.zwlxqList(param);
            r.setDataset(maps);
            r.setMsg("操作成功");
        }catch (JSONException ex){
            r.setCode(-1);
            r.setMsg(ex.getMessage());
        }catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }



    //我的销假 列表
    @RequestMapping(value = "/user/getmyreport")
    @ResponseBody
    public Object getPageReport() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            //用户信息
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            //分页
            int rowcount = trimToNum(param.get("ROWCOUNT"));
            int current = trimToNum(param.get("CURRENT"));
            Limit limit = new Limit(param);
            if(rowcount==0 && current==0){
                rowcount = limit.getSize();
                current = limit.getStart();
            }
            //分页查询（直接查数据库leave表）
            Page<?> page = leaveService.getPageReport(trimToEmpty(userEntity.getUser_id()), current, rowcount);

            r.setDataset(page);
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




    /*要事登记接口------------------------------------------------------------------------------------*/

    /**
     * 获取业务字段(要事类型)
     *
     * @return
     */
    @ApiOperation(value = "字典表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "type_code", value = "type_code", required = true)

    })
    @PostMapping(value = "/user/dict/business")
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

    /**
     * 手机保存、更新要事登记记录DESCRIBE
     */
    @RequestMapping(value = "/user/saveOrUpdate", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Object saveOrUpdate() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            Integer result = importantService.saveOrUpdate(param);
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
     * 根据用户tonken查找职务
     */
    @RequestMapping(value = "/user/findJobByUserID", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Object findJobByUserID() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            Integer result = importantService.findJobByUserID(param);
            List<Integer> resultList = new ArrayList<>();
            resultList.add(result);
            r.setDataset(resultList);
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
     * 要事登记列表
     *
     * @return
     */
    @RequestMapping(value = "/user/list")
    @ResponseBody
    public Object list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String, Object> user = UserUtils.getUserByToken(param.get("userId"));
        String userId = user.get("SS_ID").toString();
        param.put("userId", userId);
        Page<?> page;
        try {
            page = importantService.findImportantPageByStatus(param);
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
     * 要事登记归档列表
     *
     * @return
     */
    @RequestMapping(value = "/user/gDlist")
    @ResponseBody
    public Object gDlist() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = importantService.gDlist(param);
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
     * 要事登记删除接口/(逻辑删除)
     */
    @RequestMapping(value = "/user/del")
    @ResponseBody
    public Object delImportant() {
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
     * 手机端要事登记详情
     */
    @RequestMapping(value = "/user/findImportantById")
    @ResponseBody
    public Object findImportantById() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            Map<String, Object> important = importantService.findImportantById(param);
            List<Object> result = new ArrayList<>();
            result.add(important);
            r.setDataset(result);
            r.setMsg("加载成功");
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
     * 手机端审核、归档接口
     */
    @RequestMapping(value = "/user/auditImportantById")
    @ResponseBody
    public Object auditImportant() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        try {
            Integer result = importantService.auditImportantById(param);
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


    //上级查看 请假
    @RequestMapping(value = "/user/getpageleaveHistoryforH5")
    @ResponseBody
    public Object getpageleaveHistoryforH5() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);

        try {
            Map<String, String> param = RequestUtils.toMap(request);
            //查询人
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            //被查询人的请假
            String businessid = trimToEmpty(param.get("BUSINESSID"));

            Map<String, Object> map = leaveService.getTaskByBussinessId(businessid);

            //用户信息
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }

            //分页
            int rowcount = trimToNum(param.get("ROWCOUNT"));
            int current = trimToNum(param.get("CURRENT"));
            Limit limit = new Limit(param);
            if(rowcount==0 && current==0){
                rowcount = limit.getSize();
                current = limit.getStart();
            }
            Page<?> page = leaveService.getpageleaveHistoryforH5(trimToEmpty(map.get("user_id")), current, rowcount);
            r.setDataset(page);
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

    //-----------自动打卡接口---------

    /**
     * 获取时间段
     *
     * @return
     */

    @RequestMapping(value = "/user/zddk")
    @ResponseBody
    public Object zddk() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            List<Object> args = new ArrayList<>();
            StringBuilder sql = new StringBuilder();
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());
            sql.append(" select sbsj,xbsj,5 jgsj from TB_KQPZ where bm=? ");
            args.add(usermap.get("DWMC_TRUE"));
            List<Map<String, Object>> list = DBHelper.queryForList(sql.toString(), args.toArray());
            r.setDataset(list);
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


    //*******************短信验证码*********************

    @RequestMapping(value = "/user/smscode")
    @ResponseBody
    public Object smsCode() {
        ResultEntity r = new ResultEntity(1);
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            List<Object> args = new ArrayList<>();
//            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));
            String phone = param.get("newphone");
//            Map<String, Object> usermap = getUserInfo(userEntity.getUser_id());
            String code = getNonce_str();
            System.out.println("code:"+code);
//            //给手机换号绑定
//            redisService.set(userEntity.getUser_id(),code);
//            redisService.expire(userEntity.getUser_id(),60);
            //密码重置
            redisService.set(phone,code);
            redisService.expire(phone,600);
            HttpUtil.sendSms(trimToEmpty(phone),"短信验证码："+code+",手机尾号为"+ phone.substring(7,11)+",您正在操作手机换号绑定或密码重置,请勿转发此条消息！");
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



    @RequestMapping(value = "/user/passwordbind")
    @ResponseBody
    public Object PassWordBind() {
        ResultEntity r = new ResultEntity(1);


        try {
            Map<String, String> param = RequestUtils.toMap(request);
            String phone = trimToEmpty(param.get("phone"));
            String code = trimToEmpty(param.get("code"));
            String password = trimToEmpty(param.get("password"));
            String rediscode = trimToEmpty(redisService.get(phone));

            if (code.equals(rediscode)&&code!=""){
                String salt=trimToEmpty(DBHelper.queryForScalar("select Salt from account t where t.loginname=?",String.class,phone));
                if (!"".equals(salt)){
                    String npwd = CryptUtils.md5(password, salt);
                    DBHelper.update("UPDATE ACCOUNT SET PASSWORD=? WHERE loginname=? ", npwd, phone);
                    r.setMsg("操作成功");
                }else{
                    r.setCode(-1);
                    r.setMsg("操作失败，账号不存在");
                }
            }else{
                r.setCode(-1);
                r.setMsg("操作失败,验证码失败");
            }
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }




    public  UserEntity findById(String userId) {
        String sql = "    SELECT A.LOGINNAME," +
                "  FROM ACCOUNT A " +
                " INNER JOIN ACCOUNT_USER AU ON AU.ACCOUNT_ID=A.ID " +
                " INNER JOIN SS_USER SU ON SU.ID=AU.USER_ID " +
                " INNER JOIN SS_DEPT SD ON SD.ID=SU.DEPT_ID" +
                "   WHERE su.id = ?  AND A.STATUS=?  ";
        UserEntity u = DBHelper.queryForBean(sql, UserEntity.class, userId, Constants.ACCOUNT_ENABLED);
        return u;
    }






    /**
     * 获取长度为 6 的随机数字
     *
     * @return 随机数字
     * @date 修改日志
     */
    public static String getNonce_str() {

        // 如果需要4位，那 new char[4] 即可，其他位数同理可得
        char[] nonceChars = new char[6];

        for (int index = 0; index < nonceChars.length; ++index) {
            nonceChars[index] = SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length()));
        }

        return new String(nonceChars);

    }

    /**
     * 用户登录接口
     *
     * @return
     */
    @ApiOperation(value = "虚假登录")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "username", value = "username", required = true)
    })
    @PostMapping(value = "/user/loginfake")
    @ResponseBody
    public Object userLoginFake() {
        ResultEntity r = new ResultEntity();
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            String username = param.get("username");
            String password = param.get("password");
            Map<String, Object> map = new HashMap<>();

            UserEntity u = accountService.findByloginname(username.trim());
            if (u == null) {
                r.setCode(-1);
                r.setMsg("登录失败，账号不存在或未关联系统用户！");
                return r;
            }

/*            String oldpwd = CryptUtils.md5(password, u.getSalt());
            if (!StringUtils.equals(u.getPassword(), oldpwd)) {
                r.setCode(-1);
                r.setMsg("登录失败，密码不正确！");
                return r;
            }*/

            loginLog(u.getId(), u.getLoginname(), u.getUsername(), 0, 1, request.getRemoteAddr(), param.get("deviceId"));
            String userId = jwtTokenHelper.generateToken(u.getUser_id(), jwtTokenHelper.getRandomKey(4));
            List<Map<String, Object>> roles = DBHelper.queryForList(" SELECT R.ID AS ROLEID,R.ROLENAME,R.JSBS FROM  ACCOUNT_ROLE AR,SS_ROLE  R  WHERE AR.ROLE_ID = R.ID AND AR.ACCOUNT_ID = ?", u.getId());
            System.out.println(u.getId());
            if (roles.size() == 0 || roles == null) {
                r.setCode(-1);
                r.setMsg("登录失败，请分配角色！");
                return r;
            }

            String user_id = DBHelper.queryForScalar("select t.user_id from ACCOUNT_USER t where t.account_id = ?", String.class, u.getId());
//            System.out.println(u.getId());
            if (user_id == null || StringUtils.isBlank(user_id)) {
                r.setCode(-1);
                r.setMsg("登录失败，用户不存在！");
                return r;
            } else {
                map = DBHelper.queryForMap("select * from ss_user where id = ?", user_id);
                if (map == null || map.size() == 0) {
                    r.setCode(-1);
                    r.setMsg("登录失败，用户不存在！");
                    return r;
                }
            }

            //绑定设备
//            accountService.saveAccountDevice(u,param);

            Map<String, Object> user = new HashMap<>();
            user.put("user_id", userId);
            user.put("id", u.getId());
            user.put("ss_id", trimToEmpty(map.get("ID")));
            user.put("username", u.getUsername());
            user.put("deptname", u.getDeptname());
            user.put("loginname", u.getLoginname());
            if(trimToEmpty(u.getJob()).equals("")||trimToEmpty(u.getJob()).equals("4")||trimToEmpty(u.getJob()).equals("5")||trimToEmpty(u.getJob()).equals("6")){
                user.put("job", "一线消防员");
            }else if("0".equals(trimToEmpty(u.getJob()))){
                user.put("job", "队务科长");
            }else{
                user.put("job", "管理员");

            }
            user.put("jobname", u.getJobname());
//            user.put("roles",roles);
            //若有初始密码相同则提示
            String initPwd = CryptUtils.md5(Constants.INIT_PASSWORD, u.getSalt());
            if (initPwd.equals(u.getPassword())) {
                user.put("updatepwd", "1");
            } else {
                user.put("updatepwd", "0");
            }
            DBHelper.execute(" UPDATE ACCOUNT SET LAST_LOGIN_TIME=SYSDATE WHERE LOGINNAME=? ", username);
            List<Object> l = new ArrayList<Object>();
            l.add(user);
            r.setCode(1);
            r.setDataset(l);
            r.setMsg("登陆成功！");
        } catch (JSONException ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
        } catch (Exception exception) {
            logger.error(exception.getMessage(), exception);
            r.setMsg("登录失败，发生未知错误：");
            r.setCode(-1);
        } finally {
            return r;
        }
    }

    @RequestMapping(value = "/download/xiaojia")
    @ResponseBody
    public void xiaojia() {
        leaveService.leaveMessageTask();
    }

    /**
     * 用户当年已经请假天数
     *
     * @return
     */

    @RequestMapping(value = "/user/get/leavehistorycountforyour")
    @ResponseBody
    public Object getLeaveHistoryCountForYour() {
        ResultEntity r = new ResultEntity(1);
        List<Map<String, Object>> leavehistorycount = new ArrayList<>();
        try {
            Map<String, String> param = RequestUtils.toMap(request);

            String userId = trimToEmpty(param.get("userId"));
            String ssId = trimToEmpty(param.get("ssId"));
            UserEntity userEntity = CustomRealm.GetLoginUserByuserId(ssId);
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            String ss_id=ssId;
            //如果传入businessid，则查请假人的剩余天数
            if (!trimToEmpty(param.get("BUSINESSID")).equals("")){
                Map<String,Object> leaveMap = leaveService.getTaskByBussinessId(param.get("BUSINESSID"));
                ss_id = trimToEmpty(leaveMap.get("SS_ID"));
            }

            //用户信息
            Map<String, Object> usermap = getUserInfo(ss_id);
            int allcount = 0;
            if ("1".equals(trimToEmpty(trimToNum(usermap.get("TQXJTSSFJB"))))) {
                allcount = (trimToNum(usermap.get("NXJTS")) + trimToNum(usermap.get("TQXJTS"))) / 2;
            } else {
                allcount = trimToNum(usermap.get("NXJTS")) + trimToNum(usermap.get("TQXJTS"));
            }
            //剩余
            int surplus = 0;


            BigDecimal b1 = new BigDecimal("0");
            if (usermap.size() > 1) {


                //探亲假
                List<Map<String, Object>> leavehistory2 = leaveService.selectLeaveHistory(userEntity, "2");
                if (leavehistory2.size() != 0) {
                    for (Map<String, Object> leave : leavehistory2) {
                        BigDecimal b3 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                        b1 = b1.add(b3);
                    }
                }


                //年假
//                if ((trimToEmpty(usermap.get("POLICERANK_NAME"))).indexOf("消防长")>-1) {
                List<Map<String, Object>> leavehistory3 = leaveService.selectLeaveHistory(userEntity, "3");
                if (leavehistory3.size() != 0) {
                    for (Map<String, Object> leave : leavehistory3) {
                        BigDecimal b3 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                        b1 = b1.add(b3);
                    }
                }
//                }


                //事假，一天以上
                List<Map<String, Object>> leavehistory1 = leaveService.selectLeaveHistory(userEntity, "1");
                //计算总天数
                if (leavehistory1.size() != 0) {
                    BigDecimal b2 = new BigDecimal("1");
                    for (Map<String, Object> leave : leavehistory1) {


                        BigDecimal b3 = new BigDecimal(trimToNum(leave.get("leave_days_true")));
                        //大于一天,计算入内(减去一天)
                        if (b3.compareTo(b2) > 0) {
                            BigDecimal b4 = new BigDecimal((trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))) <= 0 ? 0 : (trimToNum(leave.get("leave_days_true")) - trimToNum(leave.get("leave_days_reward"))));
                            if (b4.intValue() < 1) {
                                b4 = new BigDecimal("1");
                            }
                            b1 = b1.add(b4).subtract(b2);
                        }
                    }
                }


                //计算还剩多少假期（探亲，年假）
                surplus = allcount - b1.intValue();
                if (surplus < 0) {
                    surplus = 0;
                }
/*
                //离队住宿
                if ((trimToEmpty(usermap.get("SFKYLDZS"))).equals("1")){
                    List<Map<String, Object>> leavehistory7 = leaveService.selectLeaveHistory(userEntity,"7");
                    if (leavehistory7.size()!=0){
                        for (Map<String, Object> leave:leavehistory7) {
                            BigDecimal b3 = new BigDecimal(trimToEmpty(leave.get("leave_days_true"))) ;
                            b1 = b1.add(b3);
                        }
                    }
                }*/
            }
            Map<String, Object> map = new HashMap<>();
            map.put("leavehistorycount", "已休" + b1.intValue() + "天探亲年假，剩余" + surplus + "天");
            map.put("username", trimToEmpty(usermap.get("name")));
            map.put("already", b1.intValue());
            map.put("notyet", surplus);
            leavehistorycount.add(map);
            r.setDataset(leavehistorycount);
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
     *请假查询
     *
     * @return
     */

/*    @ApiOperation(value = "请假查询list查询")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "string", name = "leavetype", value = "请假类型 ", required = false),
            @ApiImplicitParam(dataType = "string", name = "dwmc", value = "单位", required = false),
            @ApiImplicitParam(dataType = "string", name = "name", value = "姓名", required = false),
            @ApiImplicitParam(dataType = "string", name = "starttime", value = "开始休假时间", required = false),
            @ApiImplicitParam(dataType = "string", name = "endtime", value = "结束休假时间", required = false),
            @ApiImplicitParam(dataType = "string", name = "limit", value = "一页条数", required = true),
            @ApiImplicitParam(dataType = "string", name = "page", value = "当前页", required = true),
            @ApiImplicitParam(dataType = "string", name = "userId", value = "userId", required = true)

    })*/
    @RequestMapping(value = "/user/getallleaves")
    @ResponseBody
    public Object getAllLeaves() {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultEntity r = new ResultEntity(1);
        Page<?> page;
        try {
            Map<String, String> param = RequestUtils.toMap(request);
            UserEntity userEntity = CustomRealm.GetLoginUser(param.get("userId"));

            //用户信息
            if (userEntity == null) {
                r.setCode(-1);
                r.setMsg("用户不存在！");
                return r;
            }
            page =leaveService.loadListForApp(param);
            //增加剩余可用天数计算
            ((List<Map<String, Object>>)page.getRows()).stream().forEach(item -> {
                item.put("SYTS",outLeftDays(trimToEmpty(item.get("user_id"))));
            });

            r.setDataset(page);
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