package com.tonbu.web;
 
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
 
/**
 * @author Created by Chimomo
 */
public class CloneUtil {
 
    private CloneUtil() {
        throw new AssertionError();
    }

    /**
     * Clone.
     * 调用ByteArrayInputStream或ByteArrayOutputStream对象的close方法没有任何意义，
     * 这两个基于内存的流只要垃圾回收器清理对象时就能够释放资源，这一点不同于对外部资源（如文件流）的释放。
     *
     * @param obj The object.
     * @param <T> The type.
     * @return The cloned object.
     * @throws Exception The exception.
     */
    public static <T> T clone(T obj) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(obj);
 
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return (T) objectInputStream.readObject();

    }
}