package com.puzzle.dto.request;

import com.puzzle.entity.User.Role;

import lombok.Data;

@Data
public class UpdateUserRequest {
    String name;
    String username;
    Boolean status;
    Role role;
}
