package com.puzzle.service;


import com.puzzle.entity.User;
import com.puzzle.exception.AppException;
import com.puzzle.exception.ErrorCode;
import com.puzzle.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;


    public User authenticate(String username, String password, HttpSession session) {
        User user = userRepository.findByUsername(username.trim())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_IS_WRONG);
        }

        session.setAttribute("userId", user.getId());
        return user;
    }   
}
/*
 1. Lay user-id tu session -> Kiem tra ton tai nguoi dung, 
                              Kiem tra quyen cua nguoi dung
 2. Lay List productIds -> Kiem tra toan bo productIds co trong db, 
 neu k tra ve AppException
 3. Loop productId : productIds
    + Lay system_quantity tu quantity trong bang inventory
    + Lay actual_quantity tu dto cua InventoryCheckDetailsRequest
    + Check_id nam trong dto cua InventoryCheckRequest
    + tinh adjustment
    + note lay tu dto InventoryCheckDetailsRequest (nho la xu ly null)
    + Inventory


 */