package com.grain.system.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(0, "操作成功"),
    FAIL(500, "操作失败"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "权限不足，无法访问"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(400, "请求参数错误"),
    DUPLICATE_ERROR(409, "数据已存在，不可重复"),
    BUSINESS_ERROR(422, "业务规则校验失败");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
