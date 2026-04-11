package com.grain.system.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;
    private Long total;

    private R() {}

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.msg = ResultCode.SUCCESS.getMsg();
        return r;
    }

    public static <T> R<T> ok(T data) {
        R<T> r = ok();
        r.data = data;
        return r;
    }

    public static <T> R<T> ok(T data, Long total) {
        R<T> r = ok(data);
        r.total = total;
        return r;
    }

    public static <T> R<T> fail(String msg) {
        R<T> r = new R<>();
        r.code = ResultCode.FAIL.getCode();
        r.msg = msg;
        return r;
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        R<T> r = new R<>();
        r.code = resultCode.getCode();
        r.msg = resultCode.getMsg();
        return r;
    }

    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.code = code;
        r.msg = msg;
        return r;
    }
}
