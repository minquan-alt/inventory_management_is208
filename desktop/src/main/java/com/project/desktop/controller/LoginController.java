package com.project.desktop.controller;

import com.project.desktop.DesktopApplication;
import com.project.desktop.service.AuthService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private Button loginButton;

    private AuthService authService;

    @FXML
    private void initialize() {
        authService = new AuthService();
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            if (authService.authenticate(username)) {
            try {
                DesktopApplication app = (DesktopApplication)
                    loginButton.getScene().getWindow().getUserData();
                app.showMainView();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        });
    }
}