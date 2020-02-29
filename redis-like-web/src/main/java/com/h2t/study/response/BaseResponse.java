package com.h2t.study.response;

/**
 * 统一返回结果
 *
 * @author hetiantian
 * @version 1.0
 * @Date 2019/08/01 16:45
 */
public class BaseResponse<T> {
    /**
     * 返回的data
     */
    private T data;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 是否成功
     */
    private boolean success = false;

    /**
     * 出现异常的构造函数
     */
    public BaseResponse(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    /**
     * 成功返回的结果
     */
    public BaseResponse(T data) {
        success = true;
        this.data = data;
    }

    public static <T> BaseResponse success(T data) {
        return new BaseResponse(data);
    }

    public static BaseResponse fail(String errorCode, String errorMsg) {
        return new BaseResponse(errorCode, errorMsg);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "data=" + data +
                ", errorCode='" + errorCode + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", success=" + success +
                '}';
    }
}
