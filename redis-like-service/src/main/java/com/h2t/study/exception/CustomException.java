package com.h2t.study.exception;

import com.h2t.study.enums.ErrorCodeEnum;

/**
 * 自定义异常类
 *
 * @author hetiantian
 * @version 1.0
 * @Date 2019/08/01 16:24
 */
public class CustomException extends RuntimeException {
    /**
     * 错误码
     */
    private ErrorCodeEnum errorCodeEnum;

    public CustomException(ErrorCodeEnum errorCodeEnum) {
        this.errorCodeEnum = errorCodeEnum;
    }

    public ErrorCodeEnum getErrorCodeEnum() {
        return errorCodeEnum;
    }

    public void setErrorCodeEnum(ErrorCodeEnum errorCodeEnum) {
        this.errorCodeEnum = errorCodeEnum;
    }

    @Override
    public String toString() {
        return "CustomException{" +
                "errorCodeEnum=" + errorCodeEnum +
                '}';
    }
}
