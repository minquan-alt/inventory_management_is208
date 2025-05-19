package com.puzzle.controller.client_controller;

import java.io.IOException;

import com.puzzle.dto.response.UserResponse;
import com.puzzle.entity.User;
import com.puzzle.service.AuthenticationService;
import com.puzzle.service.UserService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


@Controller // Đánh dấu là Spring Bean
public class LoginController {
    @Autowired 
    private AuthenticationService authService;

    @Autowired
    private UserService userService;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private void initialize() {
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            
            try {
                // Gọi thẳng service
                User user = authService.authenticate(username, password);
                UserResponse userResponse = userService.mapUserResponse(user);
                
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
                        Parent root = loader.load();
                        
                        MainController mainController = loader.getController();
                        mainController.initData(userResponse);
                        
                        Stage stage = (Stage) loginButton.getScene().getWindow();
                        stage.setScene(new Scene(root));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
}