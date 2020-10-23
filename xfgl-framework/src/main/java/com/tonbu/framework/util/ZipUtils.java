package com.tonbu.framework.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

public class ZipUtils {


    protected static Logger logger=LogManager.getLogger(ZipUtils.class.getName());

    public ZipUtils() {
    }

    public static void zip(File srcFile, File zipFile) {
        ZipOutputStream zipos = null;

        try {
            if (!zipFile.exists()) {
                zipFile.createNewFile();
            }

            zipos = new ZipOutputStream(new FileOutputStream(zipFile));
            zip(srcFile, zipos, "");
            zipos.closeEntry();
        } catch (Exception var7) {
            var7.printStackTrace();
        } finally {
            IOUtils.closeQuietly(zipos);
        }

    }

    public static byte[] zip(File srcFile) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = null;

        try {
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(byteArrayOutputStream));
            zip(srcFile, zipOutputStream, "");
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            byte[] var5 = byteArrayOutputStream.toByteArray();
            return var5;
        } catch (Exception var8) {
            var8.printStackTrace();
        } finally {
            IOUtils.closeQuietly(zipOutputStream);
            IOUtils.closeQuietly(byteArrayOutputStream);
        }

        return null;
    }

    public static byte[] zip(byte[] data, String entryName) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = null;

        try {
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(byteArrayOutputStream));
            zipOutputStream.setEncoding("GBK");
            zipOutputStream.putNextEntry(new ZipEntry(entryName));
            IOUtils.write(data, zipOutputStream);
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            byte[] var6 = byteArrayOutputStream.toByteArray();
            return var6;
        } catch (Exception var9) {
            var9.printStackTrace();
        } finally {
            IOUtils.closeQuietly(zipOutputStream);
            IOUtils.closeQuietly(byteArrayOutputStream);
        }

        return null;
    }

    public static void zip(Map<String, byte[]> bytesMap, File zipFile) {
        FileOutputStream output = null;
        byte[] bytes = zip(bytesMap);

        try {
            if (!zipFile.exists()) {
                zipFile.createNewFile();
            }

            output = new FileOutputStream(zipFile);
            IOUtils.write(bytes, output);
        } catch (Exception var8) {
            var8.printStackTrace();
        } finally {
            IOUtils.closeQuietly(output);
        }

    }

    public static byte[] zip(Map<String, byte[]> bytesMap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = null;

        try {
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(byteArrayOutputStream));
            zipOutputStream.setEncoding("GBK");
            Iterator var4 = bytesMap.keySet().iterator();

            while(var4.hasNext()) {
                String key = (String)var4.next();
                byte[] data = (byte[])bytesMap.get(key);
                zipOutputStream.putNextEntry(new ZipEntry(key));
                IOUtils.write(data, zipOutputStream);
                zipOutputStream.closeEntry();
            }

            zipOutputStream.close();
            byte[] var7 = byteArrayOutputStream.toByteArray();
            return var7;
        } catch (Exception var10) {
            var10.printStackTrace();
        } finally {
            IOUtils.closeQuietly(zipOutputStream);
            IOUtils.closeQuietly(byteArrayOutputStream);
        }

        return null;
    }

    private static void zip(File srcFile, ZipOutputStream zipos, String base) {
        if (srcFile != null && srcFile.exists()) {
            FileInputStream in = null;

            try {
                zipos.setEncoding("GBK");
                if (srcFile.isDirectory()) {
                    zipos.putNextEntry(new ZipEntry(base + "/"));
                    base = base.length() == 0 ? "" : base + "/";
                    File[] var7;
                    int var6 = (var7 = srcFile.listFiles()).length;

                    for(int var5 = 0; var5 < var6; ++var5) {
                        File file = var7[var5];
                        zip(file, zipos, base + file.getName());
                    }
                } else {
                    in = new FileInputStream(srcFile);
                    zipos.putNextEntry(new ZipEntry(base));
                    IOUtils.copy(in, zipos);
                }
            } catch (IOException var11) {
                var11.printStackTrace();
            } finally {
                IOUtils.closeQuietly(in);
            }

        }
    }

    public static Map<String, Object> unzip(ZipInputStream zipInputStream) {
        LinkedHashMap map = new LinkedHashMap();

        try {
            java.util.zip.ZipEntry zipentry = null;

            while((zipentry = zipInputStream.getNextEntry()) != null) {
                ZipEntry entry = new ZipEntry(zipentry);
                if (entry.isDirectory()) {
                    map.put(entry.getName(), unzip(zipInputStream));
                } else {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    IOUtils.copy(zipInputStream, byteArrayOutputStream);
                    byteArrayOutputStream.flush();
                    map.put(entry.getName(), byteArrayOutputStream.toByteArray());
                    IOUtils.closeQuietly(byteArrayOutputStream);
                }
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return map;
    }

    public static String unzipAsString(byte[] zipdata) {
        Map<String, Object> map = unzip(zipdata);
        if (map != null && map.size() > 0) {
            Iterator var3 = map.keySet().iterator();
            if (var3.hasNext()) {
                String key = (String)var3.next();
                byte[] bytes = (byte[])map.get(key);

                try {
                    return new String(bytes, "gbk");
                } catch (UnsupportedEncodingException var6) {
                    var6.printStackTrace();
                }
            }
        }

        return null;
    }

    public static Map<String, Object> unzip(byte[] zipdata) {
        ByteArrayInputStream byteArrayInputStream = null;
        ZipInputStream zipInputStream = null;

        Map var4;
        try {
            byteArrayInputStream = new ByteArrayInputStream(zipdata);
            zipInputStream = new ZipInputStream(byteArrayInputStream);
            var4 = unzip(zipInputStream);
        } finally {
            IOUtils.closeQuietly(byteArrayInputStream);
            IOUtils.closeQuietly(zipInputStream);
        }

        return var4;
    }

    public static void unzip(byte[] zipdata, String destDir) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipdata);
        ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);

        try {
            java.util.zip.ZipEntry zipentry = null;

            while((zipentry = zipInputStream.getNextEntry()) != null) {
                ZipEntry entry = new ZipEntry(zipentry);
                if (entry.isDirectory()) {
                    (new File(destDir + entry.getName())).mkdirs();
                } else {
                    BufferedInputStream bis = new BufferedInputStream(zipInputStream);
                    File file = new File(destDir + entry.getName());
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    FileOutputStream fos = new FileOutputStream(file);
                    IOUtils.copy(bis, fos);
                    fos.flush();
                    IOUtils.closeQuietly(bis);
                    IOUtils.closeQuietly(fos);
                }
            }
        } catch (Exception var13) {
            var13.printStackTrace();
        } finally {
            IOUtils.closeQuietly(zipInputStream);
            IOUtils.closeQuietly(byteArrayInputStream);
        }

    }

    public static void unzip(File zipFile, String destDir) {
        try {
            ZipFile zFile = new ZipFile(zipFile);
            Enumeration e = zFile.getEntries();

            while(e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry)e.nextElement();
                if (entry.isDirectory()) {
                    (new File(destDir + entry.getName())).mkdirs();
                } else {
                    BufferedInputStream bis = new BufferedInputStream(zFile.getInputStream(entry));
                    File file = new File(destDir + entry.getName());
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    FileOutputStream fos = new FileOutputStream(file);
                    IOUtils.copy(bis, fos);
                    fos.flush();
                    IOUtils.closeQuietly(bis);
                    IOUtils.closeQuietly(fos);
                }
            }

            zFile.close();
        } catch (Exception var9) {
            var9.printStackTrace();
        }

    }

    public static void main(String[] args) {
        unzip(new File("D:/tmp/sms.zip"), "d:/");
    }
}
