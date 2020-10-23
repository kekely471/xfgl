package com.tonbu.framework.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class RenderUtils {

    public RenderUtils() {
    }

    public static void renderText(HttpServletResponse res, String txt) {
        render(res, txt, "text/plain;charset=UTF-8");
    }

    public static void renderJson(HttpServletResponse res, Object object) {
        render(res, JsonUtils.toJson(object), "application/json;charset=UTF-8");
    }

    public static void renderXml(HttpServletResponse res, String xml) {
        render(res, xml, "text/xml;charset=UTF-8");
    }

    public static void renderHtml(HttpServletResponse res, String html) {
        render(res, html, "text/html;charset=UTF-8");
    }

    public static void renderScript(HttpServletResponse res, String scriptxt) {
        render(res, "<script type='text/javascript'>" + scriptxt + "</script>", "text/html;charset=UTF-8");
    }

    public static void renderBinary(HttpServletResponse res, String name, String contextType, String disposition, boolean target, InputStream is) {
        byte[] binaryBytes = null;

        try {
            try {
                binaryBytes = IOUtils.toByteArray(is);
            } catch (IOException var11) {
                var11.printStackTrace();
            }

            if (binaryBytes == null) {
                return;
            }

            renderBinary(res, name, contextType, disposition, target, binaryBytes);
        } finally {
            IOUtils.closeQuietly(is);
        }

    }

    public static void renderBinary(HttpServletResponse res, String name, String contextType, String disposition, boolean target, File file) {
        byte[] binaryBytes = null;

        try {
            binaryBytes = FileUtils.readFileToByteArray(file);
        } catch (IOException var8) {
            var8.printStackTrace();
        }

        if (binaryBytes != null) {
            renderBinary(res, name, contextType, disposition, target, binaryBytes);
        }
    }

    public static void renderBinary(HttpServletResponse res, String name, String contextType, String disposition, boolean target, byte[] binaryBytes) {
        String filename = name;

        try {
            filename = new String(name.getBytes("GBK"), "ISO8859_1");
        } catch (UnsupportedEncodingException var15) {
            var15.printStackTrace();
        }

        if (StringUtils.isBlank(disposition)) {
            disposition = "attachment";
        }

        res.setContentType(contextType);
        res.setContentLength(binaryBytes.length);
        if (target) {
            res.setHeader("Content-Disposition", disposition + ";filename=" + filename);
        }

        ServletOutputStream out = null;

        try {
            out = res.getOutputStream();
            IOUtils.write(binaryBytes, out);
        } catch (IOException var13) {
            ;
        } finally {
            IOUtils.closeQuietly(out);
        }

    }


    protected static void render(HttpServletResponse res, String text, String contentType) {
        try {
            res.setContentType(contentType);
            res.getWriter().write(text);
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }
}
