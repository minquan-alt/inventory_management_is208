package com.puzzle.controller;

import com.puzzle.exception.AppException;
import com.puzzle.exception.ErrorCode;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloworldController {
    @GetMapping("/api/hello")
    public String HelloWorld() {
        return "Hello World!";
    }

    @GetMapping("/test/global_handler")
    public String GlobalHandler() {
        throw new RuntimeException("Bo may test loi toan cuc");
        // return "Hello World!";
    }
    @GetMapping("/test/app_exception")
    public String AppException() {
        throw new AppException(ErrorCode.PASSWORD_REQUIRED);
        // return "Hello World!";
    }
}
