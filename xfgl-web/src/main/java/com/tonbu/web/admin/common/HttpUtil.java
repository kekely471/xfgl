package com.tonbu.web.admin.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tonbu.framework.exception.CustomizeException;
import com.tonbu.web.admin.pojo.Submit;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Base64Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import java.util.Map;


public class HttpUtil {


//    @Value("${sms.ecName}")
//    private static String ecName;
//    @Value("${sms.apId}")
//    private static String apId;
//    @Value("${sms.secretKey}")
//    private static String secretKey;
//    @Value("${sms.sign}")
//    private static String sign;
//    @Value("${sms.url}")
//    private static String url;

    private static String ecName="常州市公安消防支队";
    private static String apId="czxfgl";
    private static String secretKey="czxfgl@xfgl";
    private static String sign="fF3puczfd";
    private static String url="http://112.35.1.155:1992/sms/norsubmit";

    public static void main(String[] args) throws Exception {

//        sendSms("17721710856","测试通知1");
//        System.out.println(encryptToMD5("常州市公安消防支队czxfglczxfgl@xfgl17721710856您有一条请假消息！   曹杰锋的请假消息，需要您及时处理fF3puczfd"));
    }




    /**
     * 发送sms消息
     */
    public static Map<String, String> sendSms(String mobile,String Content) {
        Map<String, String> result = new HashMap<>();
        JSONObject jsStr = null;
        // 获取连接客户端工具
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String entityStr = null;
        CloseableHttpResponse response = null;
        try {



            // 创建POST请求对象
            HttpPost httpPost = new HttpPost(url);
 /*           // 创建请求参数 拼装
            List<NameValuePair> list = new LinkedList<>();

            BasicNameValuePair param1 = new BasicNameValuePair("", GetSmsParam( mobile, Content));

            list.add(param1);

            // 使用URL实体转换工具
            UrlEncodedFormEntity entityParam = new UrlEncodedFormEntity(list, "UTF-8");
            httpPost.setEntity(entityParam);*/

            //添加请求头信息
            // 浏览器表示
            httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.6)");
            // 传输的类型
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");



//            StringEntity postingString = new StringEntity(GetSmsParam( mobile, Content));// json传递 &&!"13775103400".equals(mobile)&&!"19951591953".equals(mobile)&&!"15295178425".equals(mobile)
/*
            if(!"13625108736".equals(mobile)){
                mobile="18015808538";
            }
*/

            StringEntity postingString = new StringEntity(GetSmsParam( mobile, Content));// json传递

            httpPost.setEntity(postingString);
            // 执行请求
            response = httpClient.execute(httpPost);
            // 获得响应的实体对象
            HttpEntity entity = response.getEntity();
            // 使用Apache提供的工具类进行转换成字符串
            entityStr = EntityUtils.toString(entity, "UTF-8");


            jsStr = JSONObject.parseObject(entityStr); //将字符串转json对象
//            System.out.println("返回结果集："+jsStr.toString());

            if (!(boolean)jsStr.get("success")){
                throw new CustomizeException(jsStr.get("rspcod").toString());
            }else{
                System.out.println("通过");
            }


        } catch (ClientProtocolException e) {
            System.err.println("Http协议出现问题");
            e.printStackTrace();
            throw new CustomizeException("Http协议出现问题");
        } catch (
                ParseException e) {
            System.err.println("解析错误");
            e.printStackTrace();
            throw new CustomizeException("解析错误");
        } catch (Exception e) {
            System.err.println("IO异常");
            e.printStackTrace();
            throw new CustomizeException("IO异常");
        } finally {
            // 释放连接
            if (null != response) {
                try {
                    response.close();
                    httpClient.close();
                } catch (IOException e) {
                    System.err.println("释放连接出错");
                    e.printStackTrace();
                }
            }
        }

        return result;
    }



    public static String GetSmsParam(String mobile,String Content) throws Exception {
        Submit submit = new Submit( );
        submit.setEcName (ecName) ;
        submit.setApId(apId);
        submit.setSecretKey(secretKey);
        submit.setMobiles(mobile);
        submit.setContent(Content);
        submit.setSign(sign);
        submit.setAddSerial("");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(submit.getEcName());
        stringBuffer.append(submit.getApId());
        stringBuffer.append(submit.getSecretKey());
        stringBuffer.append(submit.getMobiles());
        stringBuffer.append(submit.getContent());
        stringBuffer.append(submit.getSign());
        stringBuffer.append(submit.getAddSerial());
//        System.out.println("stringBuffer:"+stringBuffer.toString());
        String selfMac = encryptToMD5(stringBuffer.toString());


//        System.out.println("selfMac :"+selfMac);
        submit.setMAC(selfMac );
        String param = JSON.toJSONString(submit);
//        System.out.println( "param: "+param);
        //Base64加密
        String encode = Base64Utils.encodeToString(param.getBytes("UTF8"));
//        System.out.println("encode:"+encode);
        return encode ;







    }

    public static String encryptToMD5(String password) throws UnsupportedEncodingException {

        String result = "";

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(password.getBytes("UTF8"));
        byte b[] = md.digest();
        int i;
        StringBuffer buf = new StringBuffer("");
        for (int offset = 0; offset < b.length; offset++) {
            i = b[offset];
            if (i < 0)
                i += 256;
            if (i < 16)
                buf.append("0");
            buf.append(Integer.toHexString(i));
        }
        result = buf.toString();
//        System.out.println("MD5(" + password + ",32小写) = " + result);

        return result;


    }

}
