package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.*;
import com.tonbu.web.admin.service.CommonService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/common")
public class CommonAction {

    Logger logger = LogManager.getLogger(CommonAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    CommonService commonService;


    // @Value("${server.file.root}")
    private String rootPath = "C:/files"; //root 名称


    @RequestMapping(value = "/dict/system")
    @ResponseBody
    public Object systemDictList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.loadSSDictList(param);
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



    @RequestMapping(value = "/dict/xfy")
    @ResponseBody
    public Object xfyList(@PathVariable(required = false) String id) {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        List<?> list;
        try {

            List<Map<String, Object>> m = DBHelper.queryForList("select ID,NAME,SS_ID from tb_user " +
                    "where dwmc=" +
                    "(select dwmc from tb_user where ss_id=?) and job!='7'and job !='3'", u.getUser_id());
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



    @RequestMapping(value = "/dept")
    @ResponseBody
    public Object deptDictList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.loadDeptDictList(param);
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

    @RequestMapping(value = "/job")
    @ResponseBody
    public Object jobDictList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.loadJobDictList(param);
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

    @RequestMapping(value = "/kqdept")
    @ResponseBody
    public Object kqdeptDictList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.loadkqDeptDictList(param);
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

    @RequestMapping(value = "/informationtype")
    @ResponseBody
    public Object informationType() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.informationTypeList(param);
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

    @RequestMapping(value = "/zbzttypelist")
    @ResponseBody
    public Object zbztTypeList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.zbztTypeList(param);
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

    @RequestMapping(value = "/list/role")
    @ResponseBody
    public Object roleList() {
        ResultEntity r = new ResultEntity(1);
        List<?> list;
        try {
            list = commonService.loadRoleList();
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

    @RequestMapping(value = "/list/dept")
    @ResponseBody
    public Object deptList() {
        ResultEntity r = new ResultEntity(1);
        List<?> list;
        try {
            list = commonService.loadDeptList();
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

    @RequestMapping(value = "/list/dictType")
    @ResponseBody
    public Object dictTypeList() {
        ResultEntity r = new ResultEntity(1);
        Map<String, String> param = RequestUtils.toMap(request);
        String type = param.get("type");
        List<?> list;
        try {
            list = commonService.loadDictTypeList(type);
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

    @RequestMapping(value = "/list/menu")
    @ResponseBody
    public Object menuList() {
        ResultEntity r = new ResultEntity(1);
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.loadMenuList();
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

    @RequestMapping(value = "/list/user")
    @ResponseBody
    public Object userList() {
        ResultEntity r = new ResultEntity(1);
        List<?> list;
        try {
            list = commonService.loadUserList();
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

    @RequestMapping(value = "/list/serviceDirectory")
    @ResponseBody
    public Object serviceDirectoryList() {
        ResultEntity r = new ResultEntity(1);
        List<?> list;
        try {
            list = commonService.loadServiceDirectoryList();
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

    @RequestMapping(value = "/list/sla")
    @ResponseBody
    public Object slaList() {
        ResultEntity r = new ResultEntity(1);
        List<?> list;
        try {
            list = commonService.loadSlaList();
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


    @RequestMapping(value = "/list/contract")
    @ResponseBody
    public Object contractList() {
        ResultEntity r = new ResultEntity(1);
        Map<String, String> param = RequestUtils.toMap(request);
        String dept_id = param.get("dept_id");
        List<?> list;
        try {
            list = commonService.loadContractList(dept_id);
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

    @RequestMapping(value = "/list/flow")
    @ResponseBody
    public Object flowList() {
        ResultEntity r = new ResultEntity(1);
        List<?> list;
        try {
            list = commonService.loadFlowList();
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

    @RequestMapping(value = "/list/step")
    @ResponseBody
    public Object stepList() {
        ResultEntity r = new ResultEntity(1);
        Map<String, String> param = RequestUtils.toMap(request);
        String flow_id = param.get("flow_id");
        List<?> list;
        try {
            list = commonService.loadStepList(flow_id);
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

    @RequestMapping(value = "/tree")
    @ResponseBody
    public Object deptTree() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.loadTree(param);
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

    @RequestMapping(value = "/treeList")
    @ResponseBody
    public Object deptTreeList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.getTreeList(param);
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

    @RequestMapping(value = "/zTree/list")
    @ResponseBody
    public Object zTreeList() {
        ResultEntity r = new ResultEntity(1);
        //参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.loadzTreeList(param);
            r.setDataset(list);
            r.setMsg("");
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

    @RequestMapping(value = "/tree/list")
    @ResponseBody
    public Object treeList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.loadTreeList(param);
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

    @RequestMapping(value = "/tree/flowinfo")
    @ResponseBody
    public Object flowITree() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.loadFlowInfoTree(param);
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


    @RequestMapping(value = "/ztree/menu")
    @ResponseBody
    public Object menuZTree() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.loadMenuZTree(param);
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

    @RequestMapping(value = "/export")
    @ResponseBody
    public Object export(HttpServletResponse response) throws Exception {
        Map<String, String> param = RequestUtils.toMap(request);

        Map<String, Object> map = new HashMap<>();
        ResultEntity r = new ResultEntity(1);
        String action = param.get("action");
        String method = param.get("method");
        String fields = param.get("fields");

        List<Map<String, String>> fieldList = JsonUtils.fromJson(fields, List.class);
        List<?> list;
        String fileName = "";
        File dir = null;

        try {
            dir = new File(rootPath + "/excel");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            fileName = RandomUtils.getUUID() + ".xls";

            File file = new File(dir + File.separator + fileName);

            if (!file.exists()) {
                file.createNewFile();
            }

            Object obj = ContainerUtils.getBean(action);
            r = (ResultEntity) obj.getClass().getDeclaredMethod(method).invoke(obj);

            //获取数据
            list = r.getDataset().getRows();
            //导出数据时转化字符信息----状态
            param.put("type_code", "data_status");
            List<Map<String, Object>> statusList = commonService.loadSSDictList(param);
            for (Map<String, String> m : (List<Map<String, String>>) list) {
                if (m.get("STATUS") != null) {
                    for (Map<String, Object> s : statusList) {
                        if (m.get("STATUS").equals(s.get("VALUE"))) {
                            m.put("STATUS", s.get("NAME").toString());
                        }
                    }

                }
            }
            //调用excel工具类导出excel
            XlsUtils.gridExportToExcel(fieldList, (List<Map<String, Object>>) list, new FileOutputStream(file));

        } catch (Exception ex) {
            r.setCode(-1);
            r.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
            throw ex;
        } finally {
            map.put("r", r);
            map.put("fileName", dir + File.separator + fileName);
            return map;
        }
    }

    @RequestMapping(value = "/download")
    @ResponseBody
    public void download(HttpServletResponse response) {
        Map<String, String> param = RequestUtils.toMap(request);

        String fileName = param.get("fileName");
        String realName = fileName.substring(fileName.lastIndexOf("\\") + 1);
        File file = new File(fileName);
        if (file.exists()) {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;fileName=" + realName);

            byte[] buffer = new byte[1024];
            FileInputStream fis = null; //文件输入流
            BufferedInputStream bis = null;

            OutputStream os = null; //输出流
            try {
                os = response.getOutputStream();
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer);
                    i = bis.read(buffer);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(ex.getMessage());
            } finally {
                try {
                    bis.close();
                    fis.close();
                    forceDelete(file);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    logger.error(ex.getMessage());
                }
            }
        }
    }

    public static boolean forceDelete(File f) {
        boolean result = false;
        int tryCount = 0;
        while (!result && tryCount++ < 10) {
            System.gc();
            result = f.delete();
        }
        return result;
    }

    @RequestMapping(value = "/dict/typeList")
    @ResponseBody
    public Object getDictTypeList() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
//        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.getDictTypeList();
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

    @RequestMapping(value = "/findDataByTable")
    @ResponseBody
    public Object findDataByTable() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> list;
        try {
            list = commonService.getDataByTable(param);
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
