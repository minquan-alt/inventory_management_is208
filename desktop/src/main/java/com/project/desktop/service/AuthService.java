package com.project.desktop.service;

public class AuthService {
    public boolean authenticate(String username) {
        if (username != null && !username.isEmpty()) {
            return true;
        }
        return false;
    }
}
