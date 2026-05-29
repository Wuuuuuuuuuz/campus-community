package com.campus.community.exception;

import com.campus.community.enums.ResultCode;

public class ForbiddenException extends BusinessException {
    public ForbiddenException() {
        super(ResultCode.FORBIDDEN);
    }

    public ForbiddenException(String message) {
        super(ResultCode.FORBIDDEN, message);
    }
}
