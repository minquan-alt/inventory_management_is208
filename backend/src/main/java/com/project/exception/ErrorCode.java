package com.project.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIED_EXCEPTION(9999, "Uncategoried error"),
    INVALID_KEY(1001, "Invalid key"),
    PASSWORD_REQUIRED(1002, "Password is required")
    
    ;
    ErrorCode(int code, String message){
        this.code = code;
        this.message = message;
    }
    private int code;
    private String message;
}
