package com.tonbu.framework.util;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClassUtils {

    protected static Logger logger=LogManager.getLogger(ClassUtils.class.getName());

    public ClassUtils() {
    }

    public static <T> List<Class<T>> findClasses(String pkg, Class<T> clz) {
        ArrayList names = new ArrayList();

        try {
            getClasses(pkg, names);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        List<Class<T>> classes = new ArrayList();
        filterClass(names, classes, clz);
        return classes;
    }

    private static void getClasses(String path, List<String> classes) throws Exception {
        Enumeration<URL> urls = ClassUtils.class.getClassLoader().getResources(path);
        if (!urls.hasMoreElements()) {
            urls = ClassUtils.class.getClassLoader().getResources("/" + path);
        }

        while(true) {
            URL url;
            String protocol;
            label54:
            do {
                while(urls.hasMoreElements()) {
                    url = (URL)urls.nextElement();
                    protocol = url.getProtocol();
                    if (!protocol.equalsIgnoreCase("file") && !protocol.equalsIgnoreCase("vfsfile")) {
                        continue label54;
                    }

                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    File file = new File(filePath);
                    File[] ff = file.listFiles();
                    File[] var11 = ff;
                    int var10 = ff.length;

                    for(int var9 = 0; var9 < var10; ++var9) {
                        File f = var11[var9];
                        String name = f.getName();
                        if (f.isDirectory()) {
                            getClasses(path + "/" + name, classes);
                        } else if (name.endsWith(".class")) {
                            classes.add(path + "/" + name);
                        }
                    }
                }

                return;
            } while(!protocol.equalsIgnoreCase("jar") && !protocol.equalsIgnoreCase("zip") && !protocol.equalsIgnoreCase("wsjar"));

            JarFile jar = ((JarURLConnection)url.openConnection()).getJarFile();
            Enumeration entries = jar.entries();

            while(entries.hasMoreElements()) {
                JarEntry entry = (JarEntry)entries.nextElement();
                String name = entry.getName();
                if (name.startsWith("/")) {
                    name = name.substring(1);
                }

                if (name.startsWith(path) && name.endsWith(".class")) {
                    classes.add(name);
                }
            }
        }
    }

    private static <T> void filterClass(List<String> names, List<Class<T>> classes, Class<T> clz) {
        Iterator var4 = names.iterator();

        while(var4.hasNext()) {
            String cp = (String)var4.next();
            String className =StringUtils.replace(StringUtils.replace(cp, "/", "."), ".class", "");

            try {
                Class<?> clazz = Class.forName(className);
                if (clz == null || clz.isAssignableFrom(clazz)) {
                    classes.add((Class<T>) clazz);
                }
            } catch (ClassNotFoundException var7) {
                var7.printStackTrace();
            }
        }


    }

    public static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static Object invoke(Object target, Method method, Object... args) {
        if (target != null && method != null) {
            try {
                return method.invoke(target, args);
            } catch (Exception var6) {
                if (var6 instanceof InvocationTargetException) {
                    InvocationTargetException targetEx = (InvocationTargetException)var6;
                    Throwable t = targetEx.getTargetException();
                    throw new RuntimeException(t.getMessage(), t);
                } else {
                    throw new RuntimeException(var6);
                }
            }
        } else {
            return null;
        }
    }

    public static <T> T newInstance(Class<T> t) {
        if (t == null) {
            return null;
        } else {
            try {
                return t.newInstance();
            } catch (InstantiationException var2) {
                throw new RuntimeException(var2);
            } catch (IllegalAccessException var3) {
                throw new RuntimeException(var3);
            }
        }
    }

    public static <T> T newInstance(String classname) {
        Class<T> clz = (Class<T>) forName(classname);
        return newInstance(clz);
    }




    public static void main(String[] args) {


        Object o = ClassUtils.forName("com.tonbu.framework.dao.dialect.OracleDialect");
        logger.error("123");

    }
}
