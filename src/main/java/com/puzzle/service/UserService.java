package com.puzzle.service;

import java.util.List;
import java.util.stream.Collectors;

import com.puzzle.dto.response.UserResponse;
import com.puzzle.entity.User;
import com.puzzle.exception.AppException;
import com.puzzle.exception.ErrorCode;
import com.puzzle.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public static UserResponse mapUserResponse(User user) {
        // UserResponse userResponse = new UserResponse();

        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }

    public List<UserResponse> getUsers() {
        List<User> users = userRepository.findAll();
        if(users.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        List<UserResponse> userResponses = users.stream()
            .map(user -> mapUserResponse(user))
            .collect(Collectors.toList());
        return userResponses;
    }

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });
        return mapUserResponse(user);
    }


    public UserResponse createUser(User user) {
        if(userRepository.existsByUsername(user.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        User new_user = new User();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        new_user.setUsername(user.getUsername());
        new_user.setPassword(passwordEncoder.encode(user.getPassword()));
        new_user.setRole(user.getRole());
        new_user.setName(user.getName());
        User result = userRepository.save(new_user);
        
        return mapUserResponse(result);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

}
