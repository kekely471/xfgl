package com.tonbu.support.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import com.tonbu.framework.exception.JSONException;
import org.apache.commons.codec.digest.DigestUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import com.google.gson.Gson;



/**
 *
 * @author Administrator
 */

public final class CryptoHelper {
	private static final Gson gson =  new Gson();

   // DES加密的私钥，必须是8位长的字符串
    private static final byte[] DESkey = "12345678".getBytes();//AppConstant.DES_KEY.getBytes();// 设置密钥

	private static final byte[] DESIV = "12345678".getBytes();//AppConstant.DES_IV.getBytes();// 设置向量

	static AlgorithmParameterSpec iv = null;// 加密算法的参数接口，IvParameterSpec是它的一个实现
	private static Key key = null;

	static {
		try {
			DESKeySpec keySpec = null;// 设置密钥参数
			keySpec = new DESKeySpec(DESkey);
			iv = new IvParameterSpec(DESIV);// 设置向量
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");// 获得密钥工厂
			key = keyFactory.generateSecret(keySpec);// 得到密钥对象
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}


	public static String encode(String data) throws Exception {
		Cipher enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");// 得到加密对象Cipher
		enCipher.init(Cipher.ENCRYPT_MODE, key, iv);// 设置工作模式为加密模式，给出密钥和向量
		byte[] pasByte = enCipher.doFinal(data.getBytes("utf-8"));
		BASE64Encoder base64Encoder = new BASE64Encoder();
		return (base64Encoder.encode(pasByte)).replaceAll("\\+","%2B");
	}
	public static String decode(String data) throws JSONException {
		try{
			Cipher deCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			deCipher.init(Cipher.DECRYPT_MODE, key, iv);
			BASE64Decoder base64Decoder = new BASE64Decoder();
			//byte[] pasByte = deCipher.doFinal(base64Decoder.decodeBuffer(URLDecoder.decode(data,"UTF-8")));
			byte[] pasByte = deCipher.doFinal(base64Decoder.decodeBuffer(data.replaceAll("%2B","\\+")));
			return new String(pasByte, "UTF-8");
		}catch (Exception e){
			throw new JSONException(e.getMessage());
		}

	}

	
	
	public static Map<String, String> decodeToMap(String requestdata) throws JSONException {
		return gson.fromJson(decode(requestdata), HashMap.class);
	}
	
	public static Map<String, Object> toMap(String requestdata){
		return gson.fromJson(requestdata, HashMap.class);
	}

	/**
	 * MD5方法
	 *
	 * @param text 明文
	 * @param key 密钥
	 * @return 密文
	 * @throws Exception
	 */
	public static String md5(String text, String key){
		return DigestUtils.md5Hex(text + key);
	}

	/**
	 * MD5验证方法
	 * @param text 明文
	 * @param key 密钥
	 * @param md5 密文
	 * @return true/false
	 * @throws Exception
	 */
	public static boolean verify(String text, String key, String md5) {
		//根据传入的密钥进行验证
		String md5Text = md5(text, key);
		if(md5Text.equalsIgnoreCase(md5))
		{
			System.out.println("MD5验证通过");
			return true;
		}
		return false;
	}


	public static void main(String[] args) {
		try {
			/*String token = decode("L0mFLX0KNFvn2ZNohVTadSEOVlABiGUvsl06uC5lLkoit%2BX9SudMVfJnguNpg2Cb8rFiCByhxB8F\n" +
					"L6Y8X%2B1sqT0dtqBKdVn/zZwtHTY%2BwSgZQ21Ych4PhOEZLDwONyNHQpAkmSkC9yPWYZqULuRjYA==");*/
      		//String data ="{\"dwxl\":\"5DD8213F62A14628AF7A93B12056050D\",\"token\":\"ADB2226ACD54456EA23B4FBA91F1599E\"}";
      		String result = "[{'jcxxl':'034234DE3E39463CA90663EE2A243D1A','jcjg':'检查项三的检查结果','fj1':'CCBF33BDA6044E27925803B67D257161','fj2':'','fj3':'','bz':'这是备注','zt':'0','zgdqsj':'2019-04-01','zgyy':'没原因'},";
			result+="{'jcxxl':'2269341B92B54FC3BE8808AFB638518F','jcjg':'检查项一的检查结果','fj1':'','fj2':'','fj3':'','bz':'这是备注','zt':'1','zgdqsj':'2019-04-01','zgyy':'原因'},";
			result+="{'jcxxl':'604B177340AF42AAAE29691C800CBFAA','jcjg':'检查项四的检查结果','fj1':'CCBF33BDA6044E27925803B67D257161','fj2':'','fj3':'','bz':'这是备注','zt':'0','zgdqsj':'2019-04-01','zgyy':'没原因'},";
			result+="{'jcxxl':'E500767C9539451EA9B241B7A8A45D3E','jcjg':'检查项二的检查结果','fj1':'','fj2':'','fj3':'','bz':'这是备注','zt':'1','zgdqsj':'2019-04-01','zgyy':'原因'}]";
           	String data ="{\"11111token\":\"533ECC4A7B2A426F86ABDDC2A639D5D5\",\"lx\":\"2\",\"type\":\"5\"}";
           	String data2 = "{'lxdh':'15961467582','mm':'123456','sjch':'12345678','long':'123.123562','lat':'623.2321','address':'常州市公安局'}";
			System.out.println(encode(data).replaceAll("\r|\n",""));
			//System.out.println(decode("5dngqYZVAL273jRzKO8N408UcapWdQxVt8TkWe37wkO5kGXNeUBPhu9yWFNh1q3YeAZt8MvpmXs="));

		}  catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 本地图片转换成base64字符串
	 * @param imgFile	图片本地路径
	 * @return
	 *
	 * @author ZHANGJL
	 * @dateTime 2018-02-23 14:40:46
	 */
	public static String ImageToBase64ByLocal(String imgFile) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理


		InputStream in = null;
		byte[] data = null;

		// 读取图片字节数组
		try {
			in = new FileInputStream(imgFile);

			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 对字节数组Base64编码
		BASE64Encoder encoder = new BASE64Encoder();

		return encoder.encode(data);// 返回Base64编码过的字节数组字符串
	}

	public static String getImageStr(String imgFile) {
		InputStream inputStream = null;
		byte[] data = null;
		try {
			inputStream = new FileInputStream(imgFile);
			data = new byte[inputStream.available()];
			inputStream.read(data);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 加密
		BASE64Encoder encoder = new BASE64Encoder();
		return "data:image/jpeg;base64,"+encoder.encode(data).replaceAll("\r|\n","");
	}


}