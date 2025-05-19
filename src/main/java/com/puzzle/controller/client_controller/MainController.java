package com.puzzle.controller.client_controller;

import com.puzzle.dto.response.UserResponse;

public class MainController {
    public void initData(UserResponse user) {
        // Hiển thị thông tin user trên MainView
        System.out.println("MainView - User: " + user.getUsername());
        // Cập nhật UI với dữ liệu user
    }
}