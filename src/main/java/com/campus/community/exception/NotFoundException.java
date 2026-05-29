package com.campus.community.exception;

import com.campus.community.enums.ResultCode;

public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super(ResultCode.NOT_FOUND, message);
    }
}
