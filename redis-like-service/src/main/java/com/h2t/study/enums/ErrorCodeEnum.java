package com.h2t.study.enums;

/**
 * 错误代码枚举类
 *
 * @author hetiantian
 * @version 1.0
 * @Date 2019/08/01 16:51
 */
public enum ErrorCodeEnum {
    /**
     * 错误码
     */
    Param_does_not_exist("0001", "查找参数不存在"),
    Param_does_not_correct("0002", "所传参数格式不正确"),
    Param_can_not_null("0003", "传参不为null"),
    Object_can_not_found("0004", "查找对象不存在"),
    Unlike_article_not_exist("0005", "该文章未被当前用户点赞"),
    Like_article_is_exist("0006", "该文章已被当前用户点赞，重复点赞");

    ErrorCodeEnum(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMsg;

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
}
