package com.tonbu.support.util;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.tonbu.framework.util.ClassUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

public class CryptUtils {
    protected static final byte[] BYTES_KEY;

    protected static Logger logger = LogManager.getLogger(CryptUtils.class.getName());

    static {
        Security.addProvider((Provider) ClassUtils.newInstance("com.sun.crypto.provider.SunJCE"));
        BYTES_KEY = new byte[]{-99, 118, 97, -105, -51, -17, 81, 14};
    }

    public CryptUtils() {
    }

    public static String encrypt(String strEncrypt) {
        if (strEncrypt == null) {
            strEncrypt = "";
        }

        try {
            byte[] b = strEncrypt.getBytes("UTF8");
            SecretKey key = new SecretKeySpec(BYTES_KEY, "DES");
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding", "SunJCE");
            cipher.init(1, key);
            b = cipher.doFinal(b);
            strEncrypt = encodeBase64(b);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(strEncrypt.getBytes("UTF8"));
            b = md.digest();
            strEncrypt = encodeBase64(b);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return strEncrypt;
    }


    public static String md5(String credentials, String saltSource) {
        ByteSource salt = new Md5Hash(saltSource);
        return new SimpleHash("MD5", credentials, salt, 1024).toString();
    }


    public static String getRandomSalt(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static String encodeBase64(byte[] binaryData) {
        return Base64.encodeBase64String(binaryData);
    }

    public static byte[] decodeBase64(String base64String) {
        return Base64.decodeBase64(base64String);
    }

    public static String md5(String strEncrypt) {
        return DigestUtils.md5Hex(strEncrypt);
    }

    public static String sha256(String strEncrypt) {
        return DigestUtils.sha256Hex(strEncrypt);
    }

    public static String encodeBase64(String binaryData) {
        byte[] b = null;

        try {
            b = binaryData.getBytes("UTF-8");
        } catch (UnsupportedEncodingException var3) {
            var3.printStackTrace();
        }

        return Base64.encodeBase64String(b);
    }


    public static void main(String[] args) {


        logger.error(md5("123"));


    }
}
