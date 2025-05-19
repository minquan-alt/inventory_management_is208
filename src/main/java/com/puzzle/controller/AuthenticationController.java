package com.puzzle.controller;

import com.puzzle.dto.request.AuthenticationRequest;
import com.puzzle.dto.response.ApiResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.entity.User;
import com.puzzle.service.AuthenticationService;
import com.puzzle.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    @PostMapping("/log-in")
    ApiResponse<UserResponse> authenticate(@RequestBody AuthenticationRequest req, HttpSession session) {
        User user = authenticationService.authenticate(req.getUsername(), req.getPassword());
        UserResponse userResponse = userService.mapUserResponse(user);

        session.setAttribute("currentUser", userResponse);

        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setResult(userResponse);
        return apiResponse;
    }
    

}
