package com.tonbu.framework.action;

import com.tonbu.framework.data.Plupload;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.service.FileService;
import com.tonbu.framework.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import lombok.Data;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;

@Controller
@RequestMapping(value = "/file")
public class FileAction {

    Logger logger = LogManager.getLogger(FileAction.class.getName());

//    @Autowired
//    HttpServletRequest request;

    @Autowired
    private FileService fileService;

    @Value("${server.file.root}")
    private String rootPath; //root 名称


    @Value("${file.filter.url}")
    private String fileFilterUrl;

    @Value("${file.filter.ip}")
    private String iP;

    /**
     * 文件上传
     * 适用于Plupload 控件，文件分包上传
     *
     * @return ResultEntity
     **/
    @RequestMapping(value = "/uploadEX", method = RequestMethod.POST)
    @ResponseBody
    public Object uploadEX(Plupload plupload, HttpServletRequest request) throws IOException {
        plupload.setRequest(request);
        String mode = request.getParameter("mode");
        String callback = "upload_iframe_callback";
        String root = rootPath;
        Map<String, Object> retMap = new HashMap<String, Object>();
        if (root == null) {
            logger.warn("未取到文件上传的根目录信息,请先检查配置");

            retMap.put("code", "-1");
            retMap.put("msg", "未取到文件上传的根目录信息,请先检查配置");
            return retMap;
        }
        String guid = RandomUtils.getUUID();
        String ym = DateUtils.format(new Date(), "yyyy") + File.separator + DateUtils.format(new Date(), "MM") + File.separator;
        String target = ym;
        File file = new File(root + target);

        PluploadUtils.upload(plupload, file, guid);
        //判断文件是否上传成功（被分成块的文件是否全部上传完成）

        if (PluploadUtils.isUploadFinish(plupload)) {
            logger.info("文件上传成功：" + plupload.getMultipartFile().getOriginalFilename() + "(" + plupload.getChunks() + ")");
        }
        String real_file_name = plupload.getMultipartFile().getOriginalFilename();
        Map<String, String> filemap = new HashMap<String, String>();
        filemap.put("id", guid);
        filemap.put("name", FilenameUtils.getBaseName(real_file_name));
        filemap.put("ext", FilenameUtils.getExtension(real_file_name));
        filemap.put("path", target + guid + "." + FilenameUtils.getExtension(real_file_name));
        filemap.put("ctype", plupload.getMultipartFile().getContentType());
        filemap.put("size", String.valueOf(plupload.getMultipartFile().getSize()));


        ResultEntity entity = new ResultEntity(1);
        try {
            fileService.Upload(filemap);
        } catch (RuntimeException ex) {
            entity.setCode(-1);
            entity.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
        }
        if ("kindeditor".equals(mode)) {
            retMap.put("error", entity.getCode() > 0 ? 0 : -1);
            retMap.put("url", request.getContextPath() + "file/download?id=" + guid);
        } else {
            retMap.put("code", entity.getCode());
            retMap.put("msg", entity.getMsg());
            retMap.put("ids", guid);
        }

        String retString = JsonUtils.toJson(retMap);

        if ("iframe".equals(mode)) {
            return "<script>if(parent && parent." + callback + "){parent." + callback + "(" + retString + ");}</script>";
        }

        return retString;
    }


    /**
     * 文件上传
     *
     * @return ResultEntity
     **/
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
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
        String ym = DateUtils.format(new Date(), "yyyy") + File.separator + DateUtils.format(new Date(), "MM") + File.separator;
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
     * 文件上传 wangdong
     *
     * @return ResultEntity
     **/
    @RequestMapping(value = "/upload_tp", method = RequestMethod.POST)
    @ResponseBody
    public Object fileUpload(HttpServletRequest request) {
        String root = rootPath;
        ResultEntity entity = new ResultEntity(1);
        try{
            StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
            MultipartHttpServletRequest multipartRequest = resolver.resolveMultipart(request);
            String type = multipartRequest.getParameter("type");
            List<MultipartFile> files = multipartRequest.getFiles("file");
            Map<String, Object> retMap = new HashMap<String, Object>();
            if (root == null) {
                logger.warn("未取到文件上传的根目录信息,请先检查配置");
                retMap.put("code", "-1");
                retMap.put("msg", "未取到文件上传的根目录信息,请先检查配置");
                return retMap;
            }
            //String ym = DateUtils.format(new Date(), "yyyy") + File.separator + DateUtils.format(new Date(), "MM") + File.separator;

            List<Map<String, String>> filelist = new ArrayList<Map<String, String>>();
            //String target = ym;
            String xspUUID = "" ;
            String[] fileguids = RandomUtils.getUUID(files.size());
            for (int i = 0; i < files.size(); i++) {
                MultipartFile multipartfile = files.get(i);
                if (multipartfile == null) {
                    continue;
                }
                Map<String, String> filemap = new HashMap<String, String>();
                String hz =  FilenameUtils.getExtension(multipartfile.getOriginalFilename());//文件格式后缀

                float filesize = multipartfile.getSize();
                String size = new DecimalFormat("#.00").format(filesize / 1024 / 1024);
                //yjt/jjt
                try {
                    String guid = fileguids[i];
//                     target = ym + guid + "." + hz;
                    String target = "XFGL"  + "/" +guid + "." + hz;
                    //   FileUtils.copyInputStreamToFile(new FileInputStream(new File(root, target)), new File(root, target));
                    FileUtils.copyInputStreamToFile(multipartfile.getInputStream(), new File(root, target));
                   /* //地图截图、全景图、细节图
                    String smallpath="";
                    String fileTypes = DBHelper.queryForScalar("SELECT  WM_CONCAT(to_char(EXT1)) AS FILETYPE FROM BUSINESS_DICT WHERE TYPE_CODE = 'fileType'",String.class);
                    List<String> listFileType = Arrays.asList(fileTypes.split(","));
                    if(listFileType.contains(type)){
                        smallpath = ym + guid + "_small"+ "." + hz;
                        ImageUtils.reduceImg(root + target, root + smallpath, 200, 200, 0.3f);
                    }*/
                    filemap.put("id", guid);
                    filemap.put("name", FilenameUtils.getBaseName(multipartfile.getOriginalFilename()));
                    filemap.put("ext", FilenameUtils.getExtension(multipartfile.getOriginalFilename()));
                    filemap.put("path", target);
                    // filemap.put("smallpath", smallpath);
                    filemap.put("ctype", multipartfile.getContentType());
                    filemap.put("size", size);
                    filelist.add(filemap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Map<String, Object> param = new HashMap<String, Object>();

            param.put("filelist", filelist);

            fileService.UploadBatch(filelist);
            entity.setDataset(filelist);

        }catch (JSONException ex) {
            entity.setCode(-1);
            entity.setMsg(ex.getMessage());
        } catch(RuntimeException ex){
            entity.setCode(-1);
            entity.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
        }
        return entity;
    }


    /**
     * 文件上传 信息发布，缩略图
     *
     * @return ResultEntity
     **/
    @RequestMapping(value = "/upload_xxfb", method = RequestMethod.POST)
    @ResponseBody
    public Object fileUploadXxfb(HttpServletRequest request)throws Exception {
        String root = rootPath;
        ResultEntity entity = new ResultEntity(1);
        try{
            StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
            MultipartHttpServletRequest multipartRequest = resolver.resolveMultipart(request);
            String type = multipartRequest.getParameter("type");
            List<MultipartFile> files = multipartRequest.getFiles("file");
            Map<String, Object> retMap = new HashMap<String, Object>();
            if (root == null) {
                logger.warn("未取到文件上传的根目录信息,请先检查配置");
                retMap.put("code", "-1");
                retMap.put("msg", "未取到文件上传的根目录信息,请先检查配置");
                return retMap;
            }

            List<Map<String, String>> filelist = new ArrayList<Map<String, String>>();
            String[] fileguids = RandomUtils.getUUID(files.size());
            for (int i = 0; i < files.size(); i++) {
                MultipartFile multipartfile = files.get(i);
                if (multipartfile == null) {
                    continue;
                }

                BufferedImage bufferedImage = ImageIO.read(multipartFileToFile(files.get(i)));
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();

                double height_d=height,width_d=width;
                int c=(int)(height_d/width_d*10);
                String message="上传成功";
                if (c!=3){
                    message= "尺寸不符合，请尽量挑选长宽比为3比1的图片！";
                }
                //返回的是字节长度,1M=1024k=1048576字节 也就是if(fileSize<5*1048576)
                if(files.get(i).getSize()>(1048576*5)){message= "文件太大，请上传小于5MB的！";}
                String suffix = files.get(i).getOriginalFilename().substring(files.get(i).getOriginalFilename().lastIndexOf("."));
                if(StringUtils.isBlank(suffix)){message= "上传文件没有后缀，无法识别！";}


                Map<String, String> filemap = new HashMap<String, String>();
                String hz =  FilenameUtils.getExtension(multipartfile.getOriginalFilename());//文件格式后缀

                float filesize = multipartfile.getSize();
                String size = new DecimalFormat("#.00").format(filesize / 1024 / 1024);
                try {
                    String guid = fileguids[i];
                    String target = "XXFB"  + "/" +guid + "." + hz;
                    FileUtils.copyInputStreamToFile(multipartfile.getInputStream(), new File(root, target));
                    filemap.put("id", guid);
                    filemap.put("name", FilenameUtils.getBaseName(multipartfile.getOriginalFilename()));
                    filemap.put("ext", FilenameUtils.getExtension(multipartfile.getOriginalFilename()));
                    filemap.put("path", target);
                    filemap.put("ctype", multipartfile.getContentType());
                    filemap.put("size", size);
                    filemap.put("message", message);
                    filelist.add(filemap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Map<String, Object> param = new HashMap<String, Object>();

            param.put("filelist", filelist);

            fileService.UploadBatch(filelist);
            entity.setDataset(filelist);

        }catch (JSONException ex) {
            entity.setCode(-1);
            entity.setMsg(ex.getMessage());
        } catch(RuntimeException ex){
            entity.setCode(-1);
            entity.setMsg("网络异常");
            logger.error(ex.getMessage(), ex);
        }
        return entity;
    }

    /**
     * 文件上传 信息发布，wangeditor 富文本图片
     *
     * @return ResultEntity
     **/
    @RequestMapping("/editor")
    @ResponseBody
    public Object editor(@RequestParam("file") MultipartFile file) {
        ResultEntity r = new ResultEntity(1);
        String fileName ="";
        if(!file.isEmpty()){
            //返回的是字节长度,1M=1024k=1048576字节 也就是if(fileSize<5*1048576)
            if(file.getSize()>(1048576*5)){return "文件太大，请上传小于5MB的";}
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            if(StringUtils.isBlank(suffix)){return "上传文件没有后缀，无法识别";}

            fileName = System.currentTimeMillis()+suffix;
            String saveFileName = rootPath+"/editor/"+fileName;
            System.out.println(saveFileName);
            File dest = new File(saveFileName);
            System.out.println(dest.getParentFile().getPath());
            if(!dest.getParentFile().exists()){ //判断文件父目录是否存在
                dest.getParentFile().mkdir();
            }
            try {
                file.transferTo(dest); //保存文件
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String imgUrl="http://"+iP+":8088/xfgl"+fileFilterUrl+"editor/"+fileName;

        r.setMsg(imgUrl);

        return r;
    }



    /**
     * 文件下载
     **/
    @RequestMapping(value = "/download")
    public void download(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");
        String disposition = request.getParameter("disposition");

        if (StringUtils.isBlank(id)) {
            return;
        }

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("id", id);
        ResultEntity entity = new ResultEntity(1);

        try {
            List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
            Map<String, Object> m = fileService.download(id);
            l.add(m);
            entity.setDataset(l);
            entity.setMsg("文件下载成功");
        } catch (RuntimeException ex) {
            entity.setCode(-1);
            entity.setMsg(ex.getMessage());
            logger.error(ex.getMessage());
            throw ex;
        }

        if (entity.getCode() < 0) {
            return;
        }
        String path = (String) entity.get("PATH");
        String name = (String) entity.get("FILE_NAME");
        String ext = (String) entity.get("EXT");
        String attr = (String) entity.get("ATTRS");
        String contenttype = (String) entity.get("CONTENT_TYPE");

        if (StringUtils.isBlank(path)) { // 路径为空
            return;
        }

        String root = rootPath;

        byte[] filebyte = null;

        try {
            File file = new File(root, path);
            filebyte = FileUtils.readFileToByteArray(file);

            // 临时文件,下载后即删除信息
            if (StringUtils.equals(attr, "0")) {
                FileUtils.deleteQuietly(file);
            }

            if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
                name = new String(name.getBytes("UTF-8"), "ISO8859-1");
            } else {
                name = URLEncoder.encode(name, "UTF-8");
            }

            RenderUtils.renderBinary(response, StringUtils.trim(name) + "." + ext, contenttype, disposition, true, filebyte);

        } catch (IOException e) {
            logger.info(path + "  =====>  文件不存在");
        }
    }


    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(HttpServletRequest request) {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        List<?> l;
        try {
            l = fileService.loadFileList(param);
            r.setDataset(l);
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
     * 获取上传文件详情
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getFileDetail")
    @ResponseBody
    public Map<String, Object> getFileInfo(HttpServletRequest request) {
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Map<String, Object> l = new HashMap<>();
        try {
            l = fileService.getFileInfo(param);
        } catch (RuntimeException ex) {
            logger.error(ex.getMessage());
            throw ex;
        } finally {
            return l;
        }
    }


    @RequestMapping(value = {"/uploadBytes"}, method = {RequestMethod.POST})
    @ResponseBody
    public Object uploadBytes(HttpServletRequest request) throws IOException {
        String mode = request.getParameter("mode");
        String callback = "upload_iframe_callback";
        List<MultipartFile> files = ((MultipartHttpServletRequest)request).getFiles("file");
        Map<String, Object> retMap = new HashMap();
        String[] fileguids = RandomUtils.getUUID(files.size());
        List<Map<String, Object>> filelist = new ArrayList();

        for(int i = 0; i < files.size(); ++i) {
            MultipartFile multipartfile = files.get(i);
            if (multipartfile != null) {
                Map<String, Object> filemap = new HashMap();
                String guid = fileguids[i];
                float filesize = (float)multipartfile.getSize();
                String size = (new DecimalFormat("#.00")).format((double)(filesize / 1024.0F / 1024.0F));

                filemap.put("id", guid);
                filemap.put("name", FilenameUtils.getBaseName(multipartfile.getOriginalFilename()));
                filemap.put("ext", FilenameUtils.getExtension(multipartfile.getOriginalFilename()));
                filemap.put("content", FileUtil.file2byte(multipartfile.getInputStream()));
                filemap.put("ctype", multipartfile.getContentType());
                filemap.put("size", size);
                filelist.add(filemap);
            }
        }

        Map<String, Object> param = new HashMap();
        param.put("filelist", filelist);
        ResultEntity entity = new ResultEntity(1);

        try {
            this.fileService.UploadBatchBytes(filelist);
            entity.setDataset(filelist);
        } catch (RuntimeException var18) {
            entity.setCode(-1);
            entity.setMsg(var18.getMessage());
            this.logger.error(var18.getMessage(),var18);
        }

        if ("kindeditor".equals(mode)) {
            retMap.put("error", entity.getCode() > 0 ? 0 : -1);
            retMap.put("url", request.getContextPath() + "file/downloadBytes?id=" + fileguids[0]);
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

    @RequestMapping({"/downloadBytes"})
    public void downloadBytes(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");
        String disposition = request.getParameter("disposition");
        if (!StringUtils.isBlank(id)) {
            Map<String, Object> param = new HashMap();
            param.put("id", id);
            ResultEntity entity = new ResultEntity(1);

            try {
                List<Map<String, Object>> l = new ArrayList();
                Map<String, Object> m = this.fileService.downloadBytes(id);
                l.add(m);
                entity.setDataset(l);
                entity.setMsg("文件下载成功");
            } catch (Exception var16) {
                entity.setCode(-1);
                entity.setMsg(var16.getMessage());
                this.logger.error(var16.getMessage(), var16);
            }

            if (entity.getCode() >= 0) {
                String name = (String) entity.get("FILE_NAME");
                String ext = (String) entity.get("EXT");
                String contenttype = (String) entity.get("CONTENT_TYPE");
                Object content = entity.get("CONTENT");
                try {
                    byte[] filebyte = (byte[]) content;
                    if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
                        name = new String(name.getBytes("UTF-8"), "ISO8859-1");
                    } else {
                        name = URLEncoder.encode(name, "UTF-8");
                    }
                    RenderUtils.renderBinary(response, StringUtils.trim(name) + "." + ext, contenttype, disposition, true, filebyte);
                } catch (Exception var15) {
                    this.logger.error(var15.getMessage(), var15);
                }
            }
        }
    }


    /**
     * MultipartFile 转 File
     *
     * @param file
     * @throws Exception
     */
    public static File multipartFileToFile(MultipartFile file) throws Exception {

        File toFile = null;
        if (file.equals("") || file.getSize() <= 0) {
            file = null;
        } else {
            InputStream ins = null;
            ins = file.getInputStream();
            toFile = new File(file.getOriginalFilename());
            inputStreamToFile(ins, toFile);
            ins.close();
        }
        return toFile;
    }

    //获取流文件
    private static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
