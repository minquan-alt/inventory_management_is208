package com.puzzle.exception;


import com.puzzle.dto.response.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    // lỗi vô định - thường là sai tên của ErrorCode
    @ExceptionHandler(value = Exception.class)
    ResponseEntity <ApiResponse> handlingRuntimeException(RuntimeException exception) {
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(ErrorCode.UNCATEGORIED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIED_EXCEPTION.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // bắt lỗi của app
    @ExceptionHandler(value = AppException.class)
    ResponseEntity <ApiResponse> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // bắt lỗi @Valid từ DTO Request
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity <ApiResponse> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        try {
            errorCode = errorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            // TODO: handle exception
        }
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }
}
