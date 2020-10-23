package com.tonbu.framework.exception;

public class JSONException extends RuntimeException {

    private static final long serialVersionUID = -2379410751311933239L;

    public JSONException(String message) {
        super(message);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSONException(Throwable cause) {
        super(cause);
    }

}
