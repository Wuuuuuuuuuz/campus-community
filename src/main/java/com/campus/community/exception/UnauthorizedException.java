package com.campus.community.exception;

import com.campus.community.enums.ResultCode;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException() {
        super(ResultCode.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(ResultCode.UNAUTHORIZED, message);
    }
}
