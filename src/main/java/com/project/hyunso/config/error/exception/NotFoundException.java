package com.project.hyunso.config.error.exception;


import com.project.hyunso.config.error.ErrorCode;

public class NotFoundException extends BusinessBaseException {

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage(), errorCode);
    }
    public NotFoundException() {
        super(ErrorCode.NOT_FOUND);
    }

}
