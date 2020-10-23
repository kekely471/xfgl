package com.tonbu.framework.exception;

import com.tonbu.framework.data.ResultEntity;

public class AppException extends RuntimeException {

    private Exception exception;
    private ResultEntity resultEntity;

    public AppException(ResultEntity resultEntity){
        this.resultEntity = resultEntity;
    }

    public AppException(ResultEntity resultEntity, Exception exception){
        this.resultEntity = resultEntity;
        this.exception = exception;
    }



    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public ResultEntity getResultEntity() {
        return resultEntity;
    }

    public void setReturnData(ResultEntity resultEntity) {
        this.resultEntity = resultEntity;
    }
}
