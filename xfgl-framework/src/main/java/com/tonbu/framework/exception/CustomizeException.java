package com.tonbu.framework.exception;

/**
 * 自定义异常
 */
public class CustomizeException extends RuntimeException{
    private static final long serialVersionUID = 2305653807231756702L;
    //异常信息
    private String message;

    public CustomizeException(String message){
        super(message);
        this.message=message;
    }
    @Override
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
