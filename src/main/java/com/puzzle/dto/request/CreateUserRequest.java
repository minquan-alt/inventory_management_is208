package com.puzzle.dto.request;

import com.puzzle.entity.User.Role;

import lombok.Data;

@Data
public class CreateUserRequest {
    String username;
    String password;
    String name;
    Role role;
}
