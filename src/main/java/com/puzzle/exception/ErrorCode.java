package com.puzzle.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIED_EXCEPTION(9999, "Uncategoried error"),
    INVALID_KEY(1001, "Invalid key"),
    PASSWORD_REQUIRED(1002, "Password is required"),
    USER_NOT_EXISTED(1003, "User not existed"),
    USER_NOT_FOUND(1004, "User not found"),
    USERNAME_EXISTED(1005, "Username existed"),
    PASSWORD_IS_WRONG(1006, "Password is wrong"),
    NOT_AUTHENTICATED(1007, "You must to log in")
    
    ;
    ErrorCode(int code, String message){
        this.code = code;
        this.message = message;
    }
    private int code;
    private String message;
}
