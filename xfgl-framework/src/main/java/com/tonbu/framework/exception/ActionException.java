package com.tonbu.framework.exception;

public class ActionException extends RuntimeException {

    private static final long serialVersionUID = -2379410751311933239L;

    public ActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActionException(Throwable cause) {
        super(cause);
    }

    public ActionException(String message) {
        super(message);
    }
}
