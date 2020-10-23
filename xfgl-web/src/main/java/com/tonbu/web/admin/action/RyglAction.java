package com.tonbu.web.admin.action;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.dao.support.Page;
import com.tonbu.framework.data.ResultEntity;
import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.exception.ActionException;
import com.tonbu.framework.exception.JSONException;
import com.tonbu.framework.util.RequestUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import com.tonbu.support.shiro.CustomRealm;
import com.tonbu.web.admin.common.IDCardUtils;
import com.tonbu.web.admin.common.ImportExcelUtil;
import com.tonbu.web.admin.service.RyglService;
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
import java.util.Date;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/rygl")
public class RyglAction extends BaseAction{
    Logger logger = LogManager.getLogger(RyglAction.class.getName());

    @Autowired
    HttpServletRequest request;

    @Autowired
    RyglService ryglService;


    private String downloadUrl="D:/files/download";

    @RequestMapping(value = "/")
    public ModelAndView home() {
        ModelAndView view = new ModelAndView("admin/manage/rygl/list");
        return view;
    }


    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list() {
        ResultEntity r = new ResultEntity(1);
        //整合参数
        Map<String, String> param = RequestUtils.toMap(request);
        Page<?> page;
        try {
            page =ryglService.loadList(param);
            //增加剩余可用天数计算
            ((List<Map<String, Object>>)page.getRows()).stream().forEach(item -> {
                item.put("SYTS",outLeftDays(trimToEmpty(item.get("SS_ID"))));
            });
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

    @RequestMapping(value = "/share")
    public ModelAndView toShare() {
        ModelAndView view = new ModelAndView("admin/manage/rygl/list");
        return view;
    }

    /**
     * 获取添加页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = {"/add/{id}", "/add/"})
    public ModelAndView add(@PathVariable(required = false) String id) {
        ModelAndView view = new ModelAndView("admin/manage/rygl/add");
        if (StringUtils.isEmpty(id)) {
            return view;
        }
        try {

            Map<String, Object> m = DBHelper.queryForMap("select  t.id,t.phone,t.NXJTS,t.TQXJTS," +
                    "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'sf'  AND BD.VALUE = t.TQXJTSSFJB)TQXJTSSFJB_NAME,TQXJTSSFJB ," +
                    "(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'sf'  AND BD.VALUE = t.SFKYLDZS)SFKYLDZS_NAME,SFKYLDZS,email,idcard,name," +
                    " avatar,acc_status,(SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'zw'  AND BD.VALUE = t.JOB)JOB_NAME,t.JOB," +
                    " (SELECT NAME FROM BUSINESS_DICT BD WHERE BD.TYPE_CODE = 'jx'  AND BD.VALUE = t.POLICERANK)POLICERANK_NAME,t.POLICERANK," +
                    " to_char(t.CREATETIME,'YYYY-MM-DD HH24:MI:SS')CREATETIME,to_char(t.UPDATETIME,'YYYY-MM-DD HH24:MI:SS')UPDATETIME," +
                    " t.rwny,t.SS_ID,d.DEPTNAME as DWMC,DWMC as DWMC_TRUE  from   tb_user t  LEFT JOIN SS_DEPT D  ON D.id = t.DWMC  where t.id=? ", id);
            view.addObject("RYGL", m);
            view.addObject("id", id);
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
        try {
            Map<String, String> param = RequestUtils.toMap(request);
//             String count =ryglService.select(param);
//
//            if("0".equals(count)){
//
//            }else {
//                r.setCode(-1);
//                r.setMsg("手机号已经存在！");
//            }
            ryglService.save(param);
            r.setMsg("操作成功");
        } catch (JSONException e){
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


    /**
     * 删除数据，真删除
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
            ryglService.del(param);
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




    @RequestMapping(value = "/import")
    public ModelAndView toImport() {
        ModelAndView view = new ModelAndView("admin/manage/rygl/Import");
        return view;
    }

    /**
     * 上传文件
     *
     * @param response
     */
    @RequestMapping(value = "/importData", method = RequestMethod.POST)
    @ResponseBody
    public Object importData(HttpServletResponse response, HttpServletRequest request) throws Exception {
        ResultEntity r = new ResultEntity(1);
        String[] properties = {"name","phone","idcard","email","job","policerank","dwmc","rwny","tqxjts","nxjts","tqxjtssfjb","sfkyldzs"};
        List<Map<String,String>> dbList = new ArrayList<>();//入库数据
        List<Map<String,String>> unList = new ArrayList<>();//不合规数据

        try {
            List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
            for (int i = 0; i < files.size(); i++) {
                MultipartFile multipartfile = files.get(i);
                if (multipartfile == null) {
                    continue;
                }
                List<Map<String,String>> listData = ImportExcelUtil.excelToList(multipartfile, properties, 0, 1);

                for (Map<String,String> o: listData
                ) {
                    //警衔 、 单位名称 、 上级单位名

                    String job =DBHelper.queryForScalar("select  value from business_dict t where  t.type_code='zw' and t.name =?",String.class,o.get("job"));
                    String jx =DBHelper.queryForScalar("select  value from business_dict t where  t.type_code='jx' and t.name =?",String.class,o.get("policerank"));
                    String dwmc =DBHelper.queryForScalar("select  id from SS_DEPT t where   t.deptname=?",String.class,o.get("dwmc"));

                    if(!"".equals(trimToEmpty(job))){
                        o.put("job",job);
                    }else {
                        o.put("job","");
                    }
                    if(!"".equals(trimToEmpty(dwmc))){
                        o.put("dwmc",dwmc);
                    }else {

                        o.put("dwmc","");
                    }
                    if(!"".equals(trimToEmpty(jx))){
                        o.put("policerank",jx);
                    }else {
                        o.put("policerank","");
                    }

                    o.put("tqxjtssfjb",o.get("tqxjtssfjb")=="是" ? "1" :"0" ) ;
                    o.put("sfkyldzs",o.get("sfkyldzs")=="是" ? "1" :"0" ) ;
                    o.put("acc_status","0" ) ;

                    String idCard=o.get("idcard").trim();

                    if (idCard.indexOf("'")>-1){
                        int idCard_l = o.get("idcard").length();
                        o.put("idcard",o.get("idcard").substring(1,idCard_l));
                    }

//                    //校验身份证号码有效性
/*                    String IDCardFlagidcard = IDCardUtils.IDCardValidate(idCard);
                    if (!"YES".equals(IDCardFlagidcard)){
                        unList.add(o);
                        continue;
                    }*/
                  /*  //效验手机号
                    boolean IDCardFlagphone = IDCardUtils.isMobilePhone(o.get("phone"));
                    if (!IDCardFlagphone){
                        unList.add(o);
                        continue;
                    }*/
                    //判断手机号是否存在
                    int countph = DBHelper.queryForScalar("select count(*) from tb_user where phone=?",Integer.class,o.get("phone"));
                    if(countph>0){
                        unList.add(o);
                        continue;
                    }
                    //效验入伍年月
//                    boolean IDCardFlagrwny = IDCardUtils.isDaterw(o.get("rwny"));
//                    if (!IDCardFlagrwny){
                        o.put("rwny",timeForExecl(o.get("rwny")));
//                    }

                    ryglService.save(o);
                }
            }
            r.setDataset(unList);
        } catch (Exception e) {
            System.out.println("导入数据异常："+e);
        }
        return  r;
    }

    public static String timeForExecl(String timestr){
        if (timestr.indexOf(".")>-1){
            timestr=timestr.substring(0,5);
        }
        Calendar cal = Calendar.getInstance();
        cal.set(1900, 0, 1);
        cal.add(Calendar.DAY_OF_MONTH, trimToNum(timestr));

        System.out.print(cal.get(Calendar.YEAR) + "年");
        System.out.print(cal.get(Calendar.MONTH) + 1 + "月");
        return cal.get(Calendar.YEAR)+"/"+cal.get(Calendar.MONTH);


    }
    public static int trimToNum(final Object str) {
        return ((str == null)||str.equals("")) ? 0 : Integer.parseInt ((str+"").trim());
    }


    /**
     *
     * 政策助学-下载模板
     *
     * @param response
     */
    @RequestMapping(value = "/download")
    @ResponseBody
    public void download(HttpServletResponse response) throws IOException {
        String fileName = downloadUrl + "/人员信息_模板下载.xls";
        File file = new File(fileName);
        if (file.exists()) {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;fileName=model.xls");
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
                } catch (IOException ex) {
                    ex.printStackTrace();
                    logger.error(ex.getMessage());
                }
            }
        }
    }

}
