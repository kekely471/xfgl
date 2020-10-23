package com.tonbu.framework.exception;

public enum ExceptionEnum {

    //数据库异常
    CannotGetJdbcConnectionException("数据库连接错误","100001"),
    SQLException("操作数据库异常","100002"),
    BadSqlGrammarException("数据库SQL错误","100003"),
    //
    IndexOutOfBoundsException("下标越界异常","200001"),
    ArrayIndexOutOfBoundsException("数组索引越界异常","200002"),
    NullPointerException("空指针异常","200003"),
    ArithmeticException("算术条件异常","200004"),
    ClassNotFoundException("找不到类异常","200005"),
    NegativeArraySizeException  ("数组长度为负异常","200006"),
    ArrayStoreException("数组中包含不兼容的值抛出的异常","200007"),
    SecurityException ("安全性异常","200008"),
    IllegalArgumentException  ("非法参数异常","200009"),
    NoSuchMetodException("找不到方法异常","200010"),
    ArithmeticExecption("算术异常","200010"),
    NegativeArrayException("数组负下标异常","200011"),
    IndexOutOfBoundsExecption("下标越界异常","200012"),
    ZeroException("参数不能小于0异常","200013"),
    //输入输出流
    IOException("操作输入流和输出流时可能出现的异常","300001"),
    EOFException("文件已结束异常","300002"),
    FileNotFoundException("文件未找到异常","300003"),
    //
    ClassCastException("类型转换异常类","400001"),
    NoSuchFieldException("字段未找到异常","400002"),
    NoSuchMethodException("方法未找到抛出的异常","400003"),
    NumberFormatException("字符串转换为数字抛出的异常","400004"),
    StringIndexOutOfBoundsException("字符串索引超出范围抛出的异常","400005"),
    IllegalAccessException("不允许访问某类异常","400006"),
    InstantiationException("指定的类对象无法被实例化","400007"),
    SecturityException("违背安全原则异常","400008"),
    SystemException("系统异常","400009"),
    UnsupportedOperationException("不支持的操作异常","400010"),
    InvocationTargetException("反射异常","400011"),
    HttpHostConnectException("网络请求异常","400012"),
    IllegalStateException("请求状态异常","400013"),

    //自定义异常
    CustomizeException("系统自定义异常","888888"),
    Exception("其他错误", "999999");

    public String value;
    public String code;

    ExceptionEnum(String value, String code){
        this.value = value;
        this.code = code;
    }
}
