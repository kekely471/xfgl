package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.service.FileService;
import com.tonbu.framework.util.JsonUtils;
import com.tonbu.framework.util.RandomUtils;
import com.tonbu.framework.util.RequestUtils;

import com.tonbu.web.admin.service.VersionService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping(value = "/version")
@Controller
public class VersionAction {
    Logger logger = LogManager.getLogger(DictAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    private VersionService versionService;

    @Autowired
    private FileService fileService;

    @Value("${server.file.root}")
    private String rootPath;

    /*
     * 获取版本管理页面
     * */
    @RequestMapping(value = "/")
    public ModelAndView version() {
        ModelAndView view = new ModelAndView("admin/manage/version/list");
        return view;
    }



    /*
     * 获取版本管理列表
     * */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list() {
        ResultEntity obj = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page = versionService.loadList(param);
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
     * 获取添加页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/add/{isView}/{id}", "/add/{isView}"})
    public ModelAndView add(@PathVariable Boolean isView, @PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/version/add");
        view.addObject("isView", isView);
        Map<String, Object> p = new HashMap<>();
        if (!StringUtils.isEmpty(id)) {
            try {
                StringBuffer sql = new StringBuffer(" SELECT V.ID,V.VERSIONNAME,V.VERSIONCODE,V.FILESIZE,V.DOWNLOADURL,V.REMARK,to_char(V.CREATE_TIME,'yyyy-MM-dd HH24:mi:ss')CREATE_TIME,(SELECT U.USERNAME FROM SS_USER U WHERE U.ID = V.CREATE_ID) AS CREATE_ID,V.VERSIONSTATE,V.USEPLATFORM " +
                        " FROM B_VERSION V " +
                        " WHERE id=? ");
                Map<String, Object> m = DBHelper.queryForMap(sql.toString(), id);
                view.addObject("VERSION", m);
            } catch (ActionException ex) {
                throw ex;
            }
        }
        return view;
    }

    //删除（真删除）
    @RequestMapping(value = "/relDeleteByIdList")
    @ResponseBody
    public ResultEntity relDeleteByIdList() {
        Map<String, String> param = RequestUtils.toMap(request);
        ResultEntity r = new ResultEntity(1);
        try {
            r = versionService.relDeleteByIdList(param);
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    //新增
    @RequestMapping(value = "/addInfo")
    @ResponseBody
    public ResultEntity addInfo() {
        Map<String, String> param = RequestUtils.toMap(request);
        ResultEntity r = new ResultEntity(1);
        try {
            r = versionService.addInfo(param);
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    //修改
    @RequestMapping(value = "/updateInfo")
    @ResponseBody
    public ResultEntity updateInfo() {
        Map<String, String> param = RequestUtils.toMap(request);
        ResultEntity r = new ResultEntity(1);
        try {
            r = versionService.updateInfo(param);
        } catch (RuntimeException ex) {
            r.setCode(-1);
            r.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            return r;
        }
    }

    @RequestMapping(
            value = {"/upload"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public Object upload(HttpServletRequest request) throws IOException {
        String mode = request.getParameter("mode");
        String callback = "upload_iframe_callback";
        String root = this.rootPath;
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
        Map<String, Object> retMap = new HashMap();
        if (root == null) {
            this.logger.warn("未取到文件上传的根目录信息,请先检查配置");
            retMap.put("code", "-1");
            retMap.put("msg", "未取到文件上传的根目录信息,请先检查配置");
            return retMap;
        } else {
            String[] fileguids = RandomUtils.getUUID(files.size());
            String ym = "/apk" + File.separator;
            List<Map<String, String>> filelist = new ArrayList();

            for (int i = 0; i < files.size(); ++i) {
                MultipartFile multipartfile = (MultipartFile) files.get(i);
                if (multipartfile != null) {
                    Map<String, String> filemap = new HashMap();
                    String guid = fileguids[i];
                    String target = ym + guid + "." + FilenameUtils.getExtension(multipartfile.getOriginalFilename());
                    float filesize = (float) multipartfile.getSize();
                    String size = (new DecimalFormat("##########0.00")).format((double) (filesize / 1024.0F / 1024.0F));

                    try {
                        FileUtils.copyInputStreamToFile(multipartfile.getInputStream(), new File(root, target));
                        filemap.put("id", guid);
                        filemap.put("name", FilenameUtils.getBaseName(multipartfile.getOriginalFilename()));
                        filemap.put("ext", FilenameUtils.getExtension(multipartfile.getOriginalFilename()));
                        filemap.put("path", target);
                        filemap.put("ctype", multipartfile.getContentType());
                        filemap.put("size", size);
                        filelist.add(filemap);
                    } catch (IOException var19) {
                        var19.printStackTrace();
                    }
                }
            }

            Map<String, Object> param = new HashMap();
            param.put("filelist", filelist);
            ResultEntity entity = new ResultEntity(1);

            try {
                this.fileService.UploadBatch(filelist);
                entity.setDataset(filelist);
            } catch (RuntimeException var18) {
                entity.setCode(-1);
                entity.setMsg(var18.getMessage());
                this.logger.error(var18.getMessage());
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
            } else {
                return retString;
            }
        }
    }
}
