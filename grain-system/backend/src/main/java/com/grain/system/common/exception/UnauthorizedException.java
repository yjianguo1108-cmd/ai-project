package com.grain.system.common.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() { super("未登录或Token已过期"); }
    public UnauthorizedException(String message) { super(message); }
}
