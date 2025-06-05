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
    NOT_AUTHENTICATED(1007, "You must to log in"),
    NO_PERMISSION(1008, "You have no permission to access"),

    CAN_NOT_EXECUTE_STORE_PROCEDURE(2001, "Store Procedure met error"),
    ERROR_IN_CREATE_STOCK_OUT_REQUEST_PROCESS(2002, "Stock out request process met error"),
    STOCK_OUT_REQUEST_NOT_FOUND(2003, "Stock out request not found "),
    STOCK_IN_REQUEST_NOT_FOUND(2004, "Stock in request not found "),
    ERROR_IN_CREATE_STOCK_IN_REQUEST_PROCESS(2005, "Stock in request process met error"),

    HAVE_NON_EXIST_PRODUCT_ID(3001, "There is at least 1 id not existed"),
    PRODUCT_NOT_FOUND(1009, "Product not found"),
    PRODUCT_NOT_EXISTED(1010, "Product not existed"),
    PRODUCT_NAME_EXISTED(1011, "Product name not existed"),
    INVENTORY_NOT_FOUND(1010, "Inventory check not existed"),
    ;
    ErrorCode(int code, String message){
        this.code = code;
        this.message = message;
    }
    private int code;
    private String message;
}
