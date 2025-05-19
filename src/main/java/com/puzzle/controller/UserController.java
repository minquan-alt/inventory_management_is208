package com.puzzle.controller;

import java.util.List;

import com.puzzle.dto.response.ApiResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.entity.User;
import com.puzzle.exception.AppException;
import com.puzzle.exception.ErrorCode;
import com.puzzle.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("api/users")
public class UserController {
    @Autowired
    private UserService userService;
    
    @GetMapping("")
    ApiResponse<List<User>> getUsers() {
        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getUsers());
        return apiResponse;
    }

    @GetMapping("/{id}")
    ApiResponse<User> getUser(@PathVariable("id") Long id) {
        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getUser(id));
        return apiResponse;
    }

    @PostMapping("")
    ApiResponse<User> createUser(@RequestBody User user) {
        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(user));
        return apiResponse;
    }

    @DeleteMapping("/{id}")
    ApiResponse<User> deleteUser(@PathVariable("id") Long id) {
        ApiResponse apiResponse = new ApiResponse<>();
        userService.deleteUser(id);
        apiResponse.setResult("User is deleted");
        return apiResponse;
    }

    @GetMapping("/current_user")
    ApiResponse<UserResponse> getCurrentUser(HttpSession session) {
        UserResponse currentUser = (UserResponse) session.getAttribute("currentUser");
        if(currentUser == null) {
            throw new AppException(ErrorCode.NOT_AUTHENTICATED);
        }
        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setResult(currentUser);
        return apiResponse;
    }
    
}
