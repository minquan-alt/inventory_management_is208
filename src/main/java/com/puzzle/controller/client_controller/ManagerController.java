package com.puzzle.controller.client_controller;

import com.puzzle.dto.response.UserResponse;

import org.springframework.stereotype.Controller;

@Controller
public class ManagerController {
    public void initData(UserResponse user) {
        // Hiển thị thông tin user trên MainView
        System.out.println("MainView - User: " + user.getUsername());
        // Cập nhật UI với dữ liệu user
    }
}