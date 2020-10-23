package com.tonbu.support.util;


import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: jinlei
 * @date: 2019/3/6
 * @description: 发短信的工具类，使用的是阿里云短信
 * 错误吗查看：https://error-center.aliyun.com/status/product/Dysmsapi?spm=a2c4g.11186623.2.18.3f53202axrCXAs
 */
public class MessageUtils {

    /**
    * @description: 调用阿里云的发短信Api
    * @author: jinlei
    * @date: 2019/3/6 3:12 PM
    * @param: [phoneNumbers, signName, templateCode, templateParam]
    * @return: java.lang.String
    */
    public static String sendMsgByAli(String phoneNumber, String signName,String templateCode,String templateParam) {

        DefaultProfile profile = DefaultProfile.getProfile("default", "<accessKeyId>", "<accessSecret>");
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        //request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setAction("SendSms");
        request.putQueryParameter("PhoneNumbers", phoneNumber);
        request.putQueryParameter("SignName", "江苏同步软件");
        request.putQueryParameter("TemplateCode", "SMS_139229478");
        request.putQueryParameter("TemplateParam", "{\"code\":\"1111\"}");
        String r="";
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
            r=response.getData();
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return r;
    }



}
