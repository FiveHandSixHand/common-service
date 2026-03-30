package com.fhsh.daitda.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessException extends RuntimeException {

    private ErrorCode errorCode;
    private String message;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
        this.message = errorCode.getDescription();
    }
}
